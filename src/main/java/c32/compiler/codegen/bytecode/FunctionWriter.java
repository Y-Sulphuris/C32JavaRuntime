package c32.compiler.codegen.bytecode;

import c32.compiler.Location;
import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.*;
import c32.compiler.logical.tree.statement.*;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;

import static c32.compiler.codegen.bytecode.ASMUtils.*;
import static org.objectweb.asm.Opcodes.*;

@Getter
public final class FunctionWriter {
	private final FunctionImplementationInfo func;
	private final MethodVisitor mv;

	private int localVariableIndex = 0;
	private final HashMap<LabelStatement, Label> labels = new HashMap<>();
	private final HashMap<LabelStatement,Label> usedlabels = new HashMap<>();
	private Label NEXT_LABEL = null;
	private final int stackFrameSize;

	public FunctionWriter(FunctionImplementationInfo func, MethodVisitor mv) {
		this.func = func;
		this.mv = mv;
		stackFrameSize = ASMUtils.calcStackFrameSize(func);
	}

	public void write() {
		localVariableIndex++;//skip (ptr size = 8)
		for (VariableInfo arg : func.getArgs()) {
			//0 - stack pointer (always)
			if (!arg.is_register()) addRegisterVariableHandle(arg, ++localVariableIndex);
			if (arg.getTypeRef().getType().sizeof() > 4) ++localVariableIndex;
		}
		writeBlockStatement(func.getImplementation());

		//args will be ignored
		mv.visitMaxs(1,1);
	}

	private void writeStatement(Statement statement) {
		Label label = null;
		if (ASMUtils.needSeparateLabel(statement)) {
			label = NEXT_LABEL;
			if (label == null) {
				label = writeLabel(statement.getLocation());
			} else {
				NEXT_LABEL = null;
			}
		}
		if (statement instanceof BlockStatement) {
			writeBlockStatement((BlockStatement)statement);
		} else if (statement instanceof VariableDeclarationStatement) {
			writeVariableDeclarationStatement((VariableDeclarationStatement) statement);
		} else if (statement instanceof ExpressionStatement) {
			TypeInfo retType = ((ExpressionStatement) statement).getExpression().getReturnType();
			loadExpression(((ExpressionStatement) statement).getExpression(), retType);
			if (retType != TypeInfo.PrimitiveTypeInfo.VOID) {
				mv.visitInsn(retType.sizeof() > 4 ? POP2 : POP);
			}
		} else if (statement instanceof ReturnStatement) {
			writeReturn((ReturnStatement)statement);
		} else if (statement instanceof LabelStatement) {
			writeLabel((LabelStatement)statement, label);
		} else if (statement instanceof GotoStatement) {
			writeGoto((GotoStatement)statement);
		} else if (statement instanceof IfStatement) {
			writeIfStatement((IfStatement)statement);
		} else if (statement instanceof WhileStatement) {
			writeWhileStatement((WhileStatement)statement, label);
		} else if (statement instanceof NopStatement) {
			if (((NopStatement) statement).isExplicit()) mv.visitInsn(NOP);
		}
		else {
			throw new UnsupportedOperationException(statement.getClass().getName());
		}
	}

	private void writeBlockStatement(BlockStatement statement) {
		for (Statement state : statement.getStatements()) {
			writeStatement(state);
		}
	}

	private void writeVariableDeclarationStatement(VariableDeclarationStatement statement) {
		for (VariableInfo variable : statement.getVariables()) {
			loadExpression(variable.getInitializer(), variable.getTypeRef().getType());
			storeNewVariable(variable);
		}
	}

	private void writeLabel(LabelStatement statement, Label old) {
		Label l = usedlabels.get(statement);
		if (l == null) {
			l = old;
		} else {
			writeLabel(l,statement.getLocation());
		}
		labels.put(statement,l);
	}

	private void writeGoto(GotoStatement statement) {
		Label l = labels.get(statement.getLabel());
		if (l == null) {
			l = new Label();
			usedlabels.put(statement.getLabel(),l);
		}
		mv.visitJumpInsn(GOTO,l);
	}

	private void writeWhileStatement(WhileStatement statement, Label START_LABEL) {
		Label END_LABEL = new Label();

		loadExpression(statement.getCondition());
		mv.visitJumpInsn(IFEQ,END_LABEL);

		writeStatement(statement.getStatement());
		mv.visitJumpInsn(GOTO,START_LABEL);

		writeLabel(END_LABEL,statement.getStatement().getLocation());
	}

	private void writeIfStatement(IfStatement statement) {
		Expression cond = statement.getCondition();
		Runnable ifTrue = () -> {
			writeStatement(statement.getStatement());
		};

		Runnable ifFalse = null;
		if (statement.getElseStatement() != null) {
			ifFalse = () -> {
				writeStatement(statement.getElseStatement());
			};
		}
		Label NEXT_LABEL = new Label();
		if (cond instanceof BinaryExpression) {
			BinaryOperator op = ((BinaryExpression) cond).getOperator();
			switch (op.getOp()) {
				case "==":
				case "!=":
				case ">":
				case "<":
				case ">=":
				case "<=":
					loadExpression(((BinaryExpression) cond).getLhs(),((BinaryExpression) cond).getOperator().getLeftType());
					loadExpression(((BinaryExpression) cond).getRhs(),((BinaryExpression) cond).getOperator().getRightType());
					applyCompare(op.getLeftType(), op, ifTrue, ifFalse, NEXT_LABEL);
					this.NEXT_LABEL = NEXT_LABEL;
					return;
				default:
			}
		}
		loadExpression(cond);
		applyCompareEquals(cond.getReturnType(),ifTrue,ifFalse);
		this.NEXT_LABEL = NEXT_LABEL;
	}


	private void writeReturn(ReturnStatement statement) {
		if (statement.getExpr() == null) {
			mv.visitInsn(RETURN);
		} else {
			TypeInfo type = statement.getFunction().getReturnType();
			loadExpression(statement.getExpr(), type);
			Class<?> jtype = asJavaPrimitive(type);
			int opcode;
			if (jtype == double.class) {
				opcode = DRETURN;
			} else if (jtype == float.class) {
				opcode = FRETURN;
			} else if (jtype == long.class) {
				opcode = LRETURN;
			} else if (jtype == byte.class || jtype == short.class || jtype == int.class || jtype == char.class) {
				opcode = IRETURN;
			} else {
				throw new UnsupportedOperationException("cannot return " + type.getCanonicalName());
			}
			mv.visitInsn(opcode);
		}
		mv.visitLabel(new Label());
	}



	//region #store



	private void storeExpression(Expression expression) {
		if (expression instanceof VariableRefExpression) {
			storeVariable(((VariableRefExpression) expression).getVariable());
		} else if (expression instanceof UnaryPrefixExpression) {
			Expression pointer = ((UnaryPrefixExpression) expression).getExpr();
			storeToAddress(pointer,null);
		} else if (expression instanceof IndexExpression) {
			Expression pointer = ((IndexExpression) expression).getArray();
			if (pointer.getReturnType() instanceof TypePointerInfo) {
				Expression index = ((IndexExpression) expression).getArgs().get(0);
				storeToAddress(pointer, index);
			} else {
				throw new UnsupportedOperationException();
			}
		}
		else {
			throw new UnsupportedOperationException(expression.getClass().getName());
		}
	}


	private void storeNewVariable(VariableInfo variable) {
		if (ASMUtils.canBePresentAsJavaPrimitive(variable.getTypeRef().getType())) {
			Class<?> cls = ASMUtils.asJavaPrimitive(variable.getTypeRef().getType());
			store_register(cls,variable);
		} else {
			throw new UnsupportedOperationException(variable.getTypeRef().getType().toString());
		}
	}
	private void storeVariable(VariableInfo variable) {
		VariableHandle handle = getHandle(variable);
		handle.storeToMe(mv);
	}


	private void storeToAddress(Expression pointer, Expression offset) {
		//stack: {value}
		loadPointerExpression(pointer, offset); //stack: {value, address}
		if (((TypePointerInfo)pointer.getReturnType()).getTargetType().getType().sizeof() > 4) {//если на стеке лежит long/double
			//swap long and int
			mv.visitInsn(DUP2_X2);
			mv.visitInsn(POP2); //stack: {address, value}
			//mv.visitInsn(SWAP);
		} else {
			//swap long and int
			mv.visitInsn(DUP2_X1);
			mv.visitInsn(POP2); //stack: {address, value}
		}
		TypeInfo p_type = ((TypePointerInfo) pointer.getReturnType()).getTargetType().getType();
		Class<?> primitive = asJavaPrimitive(p_type);
		char[] name = primitive.getCanonicalName().toCharArray();
		name[0] = Character.toUpperCase(name[0]);
		String methodName = "put" + String.valueOf(name);
		String descriptor = "(J" + ASMUtils.asDescriptor(p_type) + ")V";
		mv.visitMethodInsn(INVOKESTATIC,"c32/extern/Memory", methodName, descriptor, false);
	}

	private void store_register(Class<?> type, @Nullable VariableInfo var) {
		int index = ++localVariableIndex;
		if (type == long.class || type == double.class)
			++localVariableIndex;

		int opcode;
		if (type == float.class) {
			opcode = FSTORE;
		} else if (type == long.class) {
			opcode = LSTORE;
		} else if (type == double.class) {
			opcode = DSTORE;
		} else {
			if (type == int.class || type == byte.class || type == short.class || type == char.class || type == boolean.class) {
				opcode = ISTORE;
			} else {
				throw new IllegalArgumentException(type.getName());
			}
		}
		//store X
		mv.visitVarInsn(opcode,index);
		if (var != null)
			addRegisterVariableHandle(var,index);
	}

	//endregion

	//region #load

	void loadExpression(Expression expr) {
		loadExpression(expr,null);
	}

	void loadExpression(Expression expr, TypeInfo expectedType) {
		if (expr instanceof CallExpression) {
			loadCallExpression((CallExpression)expr);
		} else if (expr instanceof NumericLiteralExpression) {
			loadNumericLiteralExpression((NumericLiteralExpression)expr);
		} else if (expr instanceof VariableRefExpression) {
			loadVariableExpression((VariableRefExpression)expr);
		} else if (expr instanceof AssignExpression) {
			loadAssignExpression((AssignExpression)expr);
		} else if (expr instanceof BinaryExpression) {
			loadBinaryExpression((BinaryExpression)expr);
		} else if (expr instanceof UnaryPrefixExpression) {
			loadUnaryPrefixExpression((UnaryPrefixExpression)expr);
		} else if (expr instanceof IndexExpression) {
			loadIndexExpression((IndexExpression)expr);
		} else if (expr instanceof BooleanLiteralExpression) {
			mv.visitInsn(((BooleanLiteralExpression) expr).isValue() ? ICONST_1 : ICONST_0);
		} else if (expr instanceof ExplicitCastExpression) {
			loadExpression(((ExplicitCastExpression) expr).getExpression(),((ExplicitCastExpression) expr).getTargetType());
		} else if (expr instanceof NullLiteralExpression) {
			mv.visitLdcInsn(0L);
		}
		else {
			throw new UnsupportedOperationException(expr.getClass().getName());
		}
		if (expectedType != null && expr.getReturnType() != expectedType)
			applyCast(expr.getReturnType(),expectedType);
	}


	private void loadIndexExpression(IndexExpression expr) {
		Expression array = expr.getArray();
		if (array.getReturnType() instanceof TypePointerInfo) {
			loadFromAddress(array, expr.getArgs().get(0));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private void loadVariableExpression(VariableRefExpression expr) {
		VariableHandle handle = ASMUtils.getHandle(expr.getVariable());
		if (handle == null) {
			System.err.println(expr.getLocation().getStartLine());
			throw new NullPointerException("ASMUtils.getHandle(" + expr.getVariable().getCanonicalName() + ")");
		}
		handle.loadMe(mv);
	}

	private void loadNumericLiteralExpression(NumericLiteralExpression expr) {
		if (expr.getReturnType().sizeof() < 4 ||
				(expr.getReturnType() instanceof TypeInfo.IntegerPrimitiveTypeInfo && expr.getReturnType().sizeof() == 4)) {
			loadIntValue(expr.getNumber().intValue());
		} else {
			Class<?> type = ASMUtils.asJavaPrimitive(expr.getReturnType());
			if (type == float.class) {
				mv.visitLdcInsn(expr.getNumber().floatValue());
			} else if (type == long.class) {
				mv.visitLdcInsn(expr.getNumber().longValue());
			} else if (type == double.class) {
				mv.visitLdcInsn(expr.getNumber().doubleValue());
			} else {
				throw new UnsupportedOperationException(type.toString() + " - " + expr.getReturnType());
			}
		}
	}

	private void loadBoolValue(boolean value) {
		mv.visitInsn(value ? ICONST_1 : ICONST_0);
	}

	private void loadIntValue(int value) {
		if (value >= -1 && value <= 5) {
			mv.visitInsn(ICONST_0 + value);
		} else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			mv.visitIntInsn(BIPUSH,value);
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			mv.visitIntInsn(SIPUSH,value);
		} else {
			mv.visitLdcInsn(value);
		}
	}

	private void loadStackPointer() {
		mv.visitVarInsn(LLOAD,0);
		if (stackFrameSize != 0) {
			mv.visitLdcInsn(stackFrameSize);
			mv.visitInsn(LADD);
		}
	}


	private void loadCallExpression(CallExpression expr) {
		FunctionInfo func = expr.getFunction();
		if (func.is_extern()) {
			IntrinsicFunctionsTable.loadExternCall(this,func,expr);
			return;
		}

		if (ASMUtils.functionNeedsStackPointer(func)) {
			loadStackPointer();
		}

		int i = 0;
		for (Expression arg : expr.getArgs()) {
			loadExpression(arg, expr.getFunction().getArgs().get(i++).getTypeRef().getType());
		}
		mv.visitMethodInsn(INVOKESTATIC,
				ASMUtils.asClassName(func.getParent()),
				ASMUtils.asFunctionName(func),
				ASMUtils.asJavaFunctionDescriptor(func),
				false);
	}



	private void loadPointerExpression(Expression pointer, Expression offset) {
		if (!(pointer.getReturnType() instanceof TypePointerInfo)) throw new IllegalArgumentException();
		loadExpression(pointer); //push pointer
		if (offset != null) {
			loadExpression(offset); //push offset
			if (offset.getReturnType() != pointer.getReturnType()) {
				applyCast(offset.getReturnType(),pointer.getReturnType());
			}
			mv.visitLdcInsn(((TypePointerInfo) pointer.getReturnType()).getTargetType().getType().sizeof()); //push size
			mv.visitInsn(LMUL); //mul absoffset offset * size
			mv.visitInsn(LADD); //add pointer + absoffset
		}
	}


	private void loadFromAddress(Expression pointer, Expression offset) {
		loadPointerExpression(pointer, offset);
		TypeInfo p_type = ((TypePointerInfo) pointer.getReturnType()).getTargetType().getType();
		applyDereferensing(p_type);
	}


	private void loadUnaryPrefixExpression(UnaryPrefixExpression expr) {
		UnaryPrefixOperator op = expr.getOperator();
		loadExpression(expr.getExpr(),op.getTargetType().getType());

		switch (op.getOp()) {
			case "*":
				applyDereferensing(op.getReturnType());
				break;
			case "!":
				applyBoolNot();
				break;
			case "++":
			case "--":
				int ADD = 0;
				if (op.getOp().equals("--")) ADD += 4;
				if (op.getReturnType() == TypeInfo.PrimitiveTypeInfo.LONG) {
					mv.visitInsn(LCONST_1);
					mv.visitInsn(LADD + ADD);
					mv.visitInsn(DUP2);
				} else if (op.getReturnType() == TypeInfo.PrimitiveTypeInfo.DOUBLE) {
					mv.visitInsn(DCONST_1);
					mv.visitInsn(DADD + ADD);
					mv.visitInsn(DUP2);
				} else if (op.getReturnType() == TypeInfo.PrimitiveTypeInfo.FLOAT) {
					mv.visitInsn(FCONST_1);
					mv.visitInsn(FADD + ADD);
					mv.visitInsn(DUP);
				} else {
					mv.visitInsn(ICONST_1);
					mv.visitInsn(IADD + ADD);
					mv.visitInsn(DUP);
				}
				storeExpression(expr.getExpr());
				break;
			default:
				throw new UnsupportedOperationException();
		}
	}

	private void loadBinaryExpression(BinaryExpression expr) {
		BinaryOperator op = expr.getOperator();
		loadExpression(expr.getLhs(),op.getLeftType());
		loadExpression(expr.getRhs(),op.getRightType());

		if (expr.getLhs().getReturnType() instanceof TypePointerInfo) {
			switch (op.getOp()) {
				case "+":
				case "-":
				{
					long size = ((TypePointerInfo) expr.getLhs().getReturnType()).getTargetType().getType().sizeof();
					mv.visitLdcInsn(size);  //push size
					mv.visitInsn(LMUL);     //mul rhs * size
				}
				default:
					throw new AssertionError(op.getOp() + " with pointer type");
			}
		}

		if (applyCompare(op.getLeftType(),op,() -> loadBoolValue(true),() -> loadBoolValue(false))) {
			return;
		}

		mv.visitInsn(ASMUtils.binaryOpcode(op));
	}

	private boolean applyCompare(TypeInfo type, BinaryOperator op, Runnable ifTrue, Runnable ifFalse) {
		Label NEXT = new Label();
		boolean result = applyCompare(type,op,ifTrue,ifFalse,NEXT);
		mv.visitLabel(NEXT);
		return result;
	}
	private boolean applyCompare(TypeInfo type, BinaryOperator op, Runnable ifTrue, Runnable ifFalse, Label NEXT) {
		switch (op.getOp()) {
			case "==":
				applyCompareEquals(type,ifTrue,ifFalse);
				return true;
			case "!=":
				applyCompareNotEquals(type,ifTrue,ifFalse);
				return true;
			case ">":
				applyCompareGreater(type,ifTrue,ifFalse);
				return true;
			case ">=":
				applyCompareGreaterOrEquals(type,ifTrue,ifFalse);
				return true;
			case "<":
				applyCompareSmaller(type,ifTrue,ifFalse);
				return true;
			case "<=":
				applyCompareSmallerOrEquals(type,ifTrue,ifFalse);
				return true;
			default:
				return false;
		}
/*
		int cmp = 0;

		int cmpType = getCmpType(type);
		switch (op.getOp()) {
			case "==":
				cmp = IFNE;
				break;
			case "!=":
				cmp = IFEQ;
				break;
			case "<":
				cmp = IFGE;
				break;
			case "<=":
				cmp = IFGT;
				break;
			case ">":
				cmp = IFLE;
				break;
			case ">=":
				cmp = IFLT;
				break;
			default:
		}
		if (cmp == 0) {
			throw new IllegalArgumentException(type.getCanonicalName());
		} else {
			Label FALSE = new Label();
			if (cmpType != 0) {
				mv.visitInsn(cmpType);          // DCMPL?
			} else {
				cmp += 6;
			}
			applyComparator(cmp, ifTrue, FALSE, ifFalse, NEXT);
		}*/
	}

	private void applyCmpAs(TypeInfo type, int cmp_base, Runnable ifTrue, Runnable ifFalse) {
		int cmp = cmp_base;
		if (type != TypeInfo.PrimitiveTypeInfo.FLOAT && type.sizeof() <= 4) {
			if (type != TypeInfo.PrimitiveTypeInfo.BOOL) cmp += 6;
		} else {
			mv.visitInsn(getCmpType(type));
		}
		applyCompare(cmp,ifTrue,ifFalse);
	}
	private void applyCompareSmaller(TypeInfo type, Runnable ifTrue, Runnable ifFalse) {
		applyCmpAs(type,IFGE,ifTrue,ifFalse);
	}
	private void applyCompareSmallerOrEquals(TypeInfo type, Runnable ifTrue, Runnable ifFalse) {
		applyCmpAs(type,IFGT,ifTrue,ifFalse);
	}
	private void applyCompareGreater(TypeInfo type, Runnable ifTrue, Runnable ifFalse) {
		applyCmpAs(type,IFLE,ifTrue,ifFalse);
	}
	private void applyCompareGreaterOrEquals(TypeInfo type, Runnable ifTrue, Runnable ifFalse) {
		applyCmpAs(type,IFLT,ifTrue,ifFalse);
	}
	private void applyCompareNotEquals(TypeInfo type, Runnable ifTrue, Runnable ifFalse) {
		applyCmpAs(type,IFEQ,ifTrue,ifFalse);
	}
	private void applyCompareEquals(TypeInfo type, Runnable ifTrue, Runnable ifFalse) {
		applyCmpAs(type,IFNE,ifTrue,ifFalse);
	}

	private void applyCompare(int cmp, Runnable ifTrue, Runnable ifFalse) {
		Label NEXT = new Label();
		applyCompare(cmp,ifTrue,new Label(),ifFalse,NEXT);
		mv.visitLabel(NEXT);                // STORE: (return)
	}

	private void applyCompare(int cmp, Runnable ifTrue, Label FALSE, Runnable ifFalse, Label NEXT) {
		mv.visitJumpInsn(cmp, ifFalse == null ? NEXT : FALSE);       //  if false goto FALSE;
		ifTrue.run();                       //  set true
		mv.visitJumpInsn(GOTO,NEXT);        //  goto STORE;

		if (ifFalse != null) {
			mv.visitLabel(FALSE);           //  FALSE:
			ifFalse.run();                  //  set false
		}
	}

	private void loadAssignExpression(AssignExpression expr) {
		Expression lv = expr.getLvalue();
		if (expr.getParentOperator() != null)
			throw new UnsupportedOperationException(expr.getParentOperator().getOp());
		loadExpression(expr.getRvalue(),lv.getReturnType());
		storeExpression(lv);
		loadExpression(lv);//assign expr must returns lvalue
	}

	//endregion

	//region #apply

	private void applyCast(TypeInfo type, TypeInfo expected) {
		if (type instanceof TypePointerInfo && expected instanceof TypePointerInfo) return;

		if (type == TypeInfo.PrimitiveTypeInfo.INT) {
			if (expected == TypeInfo.PrimitiveTypeInfo.LONG) {
				mv.visitInsn(I2L);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.ULONG || expected instanceof TypePointerInfo) {
				mv.visitMethodInsn(INVOKESTATIC,"c32/extern/Runtime","I2UL","(I)J",false);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.DOUBLE) {
				mv.visitInsn(I2D);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.FLOAT) {
				mv.visitInsn(I2F);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.SHORT) {
				mv.visitInsn(I2S);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.BYTE) {
				mv.visitInsn(I2B);
				return;
			}
		} else if (type == TypeInfo.PrimitiveTypeInfo.LONG) {
			if (expected == TypeInfo.PrimitiveTypeInfo.ULONG || expected instanceof TypePointerInfo) {
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.INT) {
				mv.visitInsn(L2I);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.DOUBLE) {
				mv.visitInsn(L2D);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.FLOAT) {
				mv.visitInsn(L2F);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.SHORT) {
				mv.visitInsn(L2I);
				mv.visitInsn(I2S);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.BYTE) {
				mv.visitInsn(L2I);
				mv.visitInsn(I2B);
				return;
			}
		}

		throw new UnsupportedOperationException(type.getCanonicalName() + " and " + expected.getCanonicalName());
	}


	private void applyBoolNot() {
		Label FALSE = new Label();
		Label RET = new Label();
		mv.visitJumpInsn(IFNE,FALSE);
		mv.visitInsn(ICONST_1);
		mv.visitJumpInsn(GOTO,RET);
		mv.visitLabel(FALSE);
		mv.visitInsn(ICONST_0);
		mv.visitLabel(RET);
	}

	private void applyDereferensing(TypeInfo returnType) {
		Class<?> primitive = asJavaPrimitive(returnType);
		char[] name = primitive.getCanonicalName().toCharArray();
		name[0] = Character.toUpperCase(name[0]);
		String methodName = "get" + String.valueOf(name);
		String descriptor = "(J)" + ASMUtils.asDescriptor(returnType);
		mv.visitMethodInsn(INVOKESTATIC,"c32/extern/Memory", methodName, descriptor, false);
	}

	//endregion



	private Label writeLabel(Location location) {
		return writeLabel(new Label(),location);
	}
	private Label writeLabel(Label l, Location location) {
		mv.visitLabel(l);
		mv.visitLineNumber(location == null ? -1 : location.getStartLine(),l);
		return l;
	}
}
