package c32.compiler.codegen.bytecode;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.*;
import c32.compiler.logical.tree.statement.*;
import lombok.Getter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static c32.compiler.codegen.bytecode.ASMUtils.*;
import static org.objectweb.asm.Opcodes.*;

@Getter
public final class FunctionWriter {
	private final FunctionImplementationInfo func;
	private final MethodVisitor mv;

	private int localVariableIndex = 0;
	private long localVariableOffset = 0;
	private final HashMap<LabelStatement, Label> labels = new HashMap<>();
	private final HashMap<LabelStatement, Label> usedlabels = new HashMap<>();
	private Label NEXT_LABEL = null;
	private final long stackFrameSize;

	public FunctionWriter(MethodVisitor mv) {
		this.func = null;
		this.mv = mv;
		this.stackFrameSize = 0;
	}

	public void writeClinit(Collection<FieldInfo> fieldsToInit) {
		if (func != null)
			throw new IllegalStateException();
		for (FieldInfo field : fieldsToInit) {
			Label l = new Label();
			mv.visitLabel(l);
			mv.visitLineNumber(field.getLocation().getStartLine(), l);
			loadExpression(field.getInitializer());
			mv.visitFieldInsn(PUTSTATIC, asClassName(field.getContainer()), field.getName(), asDescriptor(field.getTypeRef().getType()));
		}
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
	}

	public FunctionWriter(FunctionImplementationInfo func, MethodVisitor mv) {
		this.func = func;
		this.mv = mv;
		stackFrameSize = ASMUtils.calcStackFrameSize(func);
	}

	public void write() {
		localVariableIndex++;//skip (ptr size = 8)
		for (VariableInfo arg : func.getArgs()) {
			//0 - stack pointer (always)
			if (arg.isRegister()) addRegisterVariableHandle(arg, ++localVariableIndex);
			else
				throw new UnsupportedOperationException(arg.getCanonicalName());//addLocalVariableHandle(arg, localVariableOffset += arg.getTypeRef().getType().sizeof());
			if (arg.getTypeRef().getType().sizeof() > 4) ++localVariableIndex;
		}
		writeBlockStatement(func.getImplementation());
		if (func.getReturnType() == TypeInfo.PrimitiveTypeInfo.VOID)
			mv.visitInsn(RETURN);
		//args will be ignored
		mv.visitMaxs(1, 1);
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
			writeBlockStatement((BlockStatement) statement);
		} else if (statement instanceof VariableDeclarationStatement) {
			writeVariableDeclarationStatement((VariableDeclarationStatement) statement);
		} else if (statement instanceof ExpressionStatement) {
			TypeInfo retType = ((ExpressionStatement) statement).getExpression().getReturnType();
			loadExpression(((ExpressionStatement) statement).getExpression(), retType);
			if (retType != TypeInfo.PrimitiveTypeInfo.VOID) {
				mv.visitInsn(retType.sizeof() > 4 ? POP2 : POP);
			}
		} else if (statement instanceof ReturnStatement) {
			writeReturn((ReturnStatement) statement);
		} else if (statement instanceof LabelStatement) {
			writeLabel((LabelStatement) statement, label);
		} else if (statement instanceof GotoStatement) {
			writeGoto((GotoStatement) statement);
		} else if (statement instanceof IfStatement) {
			writeIfStatement((IfStatement) statement);
		} else if (statement instanceof WhileStatement) {
			writeWhileStatement((WhileStatement) statement, label);
		} else if (statement instanceof NopStatement) {
			if (((NopStatement) statement).isExplicit()) mv.visitInsn(NOP);
		} else {
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
			if (variable.getInitializer() == null) {
				throw new CompilerException(variable.getLocation(), "initializer expected");
			}
			if (canBePresentAsJavaPrimitive(variable.getInitializer().getReturnType())) {
				loadExpression(variable.getInitializer(), variable.getTypeRef().getType());
				storeNewVariable(variable);
			} else if (variable.getInitializer().getReturnType() instanceof TypeArrayInfo) {
				long offset = alloca(variable);
				throw new UnsupportedOperationException("todo");
			} else {
				throw new UnsupportedOperationException(variable.getInitializer().getReturnType().getCanonicalName());
			}
		}
	}

	private void writeLabel(LabelStatement statement, Label old) {
		Label l = usedlabels.get(statement);
		if (l == null) {
			l = old;
		} else {
			writeLabel(l, statement.getLocation());
		}
		labels.put(statement, l);
	}

	private void writeGoto(GotoStatement statement) {
		Label l = labels.get(statement.getLabel());
		if (l == null) {
			l = new Label();
			usedlabels.put(statement.getLabel(), l);
		}
		mv.visitJumpInsn(GOTO, l);
	}

	private void writeWhileStatement(WhileStatement statement, Label START_LABEL) {
		Label END_LABEL = new Label();

		loadExpression(statement.getCondition());
		mv.visitJumpInsn(IFEQ, END_LABEL);

		writeStatement(statement.getStatement());
		mv.visitJumpInsn(GOTO, START_LABEL);

		writeLabel(END_LABEL, statement.getStatement().getLocation());
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
					loadExpression(((BinaryExpression) cond).getLhs(), ((BinaryExpression) cond).getOperator().getLeftType());
					loadExpression(((BinaryExpression) cond).getRhs(), ((BinaryExpression) cond).getOperator().getRightType());
					applyCompare(op.getLeftType(), op, ifTrue, ifFalse, NEXT_LABEL);
					this.NEXT_LABEL = NEXT_LABEL;
					return;
				default:
			}
		}
		loadExpression(cond);
		applyCompareEquals(cond.getReturnType(), ifTrue, ifFalse);
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
			storeToAddress(pointer, null);
		} else if (expression instanceof IndexExpression) {
			Expression pointer = ((IndexExpression) expression).getArray();
			if (pointer.getReturnType() instanceof TypePointerInfo) {
				Expression index = ((IndexExpression) expression).getArgs().get(0);
				storeToAddress(pointer, index);
			} else {
				throw new UnsupportedOperationException();
			}
		} else {
			throw new UnsupportedOperationException(expression.getClass().getName());
		}
	}


	private void storeNewVariable(VariableInfo variable) {
		if (variable.isRegister() && ASMUtils.canBePresentAsJavaPrimitive(variable.getTypeRef().getType())) {
			Class<?> cls = ASMUtils.asJavaPrimitive(variable.getTypeRef().getType());
			store_register(cls, variable);
		} else {
			store_local(variable);
			//throw new UnsupportedOperationException(variable.getTypeRef().getType().getCanonicalName() + " " + variable.getCanonicalName());
		}
	}


	private void store_register(Class<?> type, VariableInfo var) {
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
		mv.visitVarInsn(opcode, index);
		addRegisterVariableHandle(var, index);
	}

	private long alloca(VariableInfo variable) {
		long offset = localVariableOffset;
		localVariableOffset += variable.getTypeRef().getType().sizeof();
		addLocalVariableHandle(variable, offset);
		return offset;
	}

	private void store_local(VariableInfo variable) {
		long offset = alloca(variable);
		storeToLocalAddress(variable.getTypeRef().getType(), offset);
	}

	private void storeVariable(VariableInfo variable) {
		if (variable instanceof FieldInfo) {
			mv.visitFieldInsn(PUTSTATIC, asClassName(((FieldInfo) variable).getContainer()), variable.getName(), asDescriptor(variable.getTypeRef().getType()));
		} else {
			VariableHandle handle = getHandle(variable);
			handle.storeToMe(mv, this);
		}
	}


	private void storeToAddress(Expression pointer, Expression offset) {
		//stack: {value}
		loadPointerExpression(pointer, offset); //stack: {value, address}
		if (((TypePointerInfo) pointer.getReturnType()).getTargetType().getType().sizeof() > 4) {//если на стеке лежит long/double
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
		mv.visitMethodInsn(INVOKESTATIC, "c32/extern/Memory", methodName, descriptor, false);
	}

	private void loadStackPointerWithOffset(Expression offset) {
		loadStackBasePointer();
		loadExpression(offset);
		mv.visitInsn(LADD);
	}

	private void loadStackPointerWithOffset(long offset) {
		loadStackBasePointer();
		if (offset != 0) {
			mv.visitLdcInsn(offset);
			mv.visitInsn(LADD);
		}
	}

	void storeToLocalAddress(TypeInfo type, long offset) {
		if (type instanceof TypeInfo.PrimitiveTypeInfo || type instanceof TypePointerInfo) {
			//stack: {value}
			loadStackPointerWithOffset(offset); //stack: {value, address}
			if (type.sizeof() > 4) {//если на стеке лежит long/double
				//swap long and int
				mv.visitInsn(DUP2_X2);
				mv.visitInsn(POP2); //stack: {address, value}
				//mv.visitInsn(SWAP);
			} else {
				//swap long and int
				mv.visitInsn(DUP2_X1);
				mv.visitInsn(POP2); //stack: {address, value}
			}
			Class<?> primitive = asJavaPrimitive(type);
			char[] name = primitive.getCanonicalName().toCharArray();
			name[0] = Character.toUpperCase(name[0]);
			String methodName = "put" + String.valueOf(name);
			String descriptor = "(J" + ASMUtils.asDescriptor(type) + ")V";
			mv.visitMethodInsn(INVOKESTATIC, "c32/extern/Memory", methodName, descriptor, false);
		} else if (type instanceof TypeArrayInfo) {
			TypeArrayInfo array = (TypeArrayInfo) type;
			if (!array.isStaticArray()) throw new UnsupportedOperationException();
			for (int i = array.getStaticLength() - 1; i >= 0; i--) {
				storeToLocalAddress(array.getElementType().getType(), offset + array.getElementType().getType().sizeof() * i);
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}


	//endregion

	//region #load

	void loadExpression(Expression expr) {
		loadExpression(expr, null);
	}

	void loadExpression(Expression expr, TypeInfo expectedType) {
		if (expr instanceof CallExpression) {
			loadCallExpression((CallExpression) expr);
		} else if (expr instanceof NumericLiteralExpression) {
			loadNumericLiteralExpression((NumericLiteralExpression) expr);
		} else if (expr instanceof VariableRefExpression) {
			loadVariableExpression((VariableRefExpression) expr);
		} else if (expr instanceof AssignExpression) {
			loadAssignExpression((AssignExpression) expr);
		} else if (expr instanceof BinaryExpression) {
			loadBinaryExpression((BinaryExpression) expr);
		} else if (expr instanceof UnaryPrefixExpression) {
			loadUnaryPrefixExpression((UnaryPrefixExpression) expr);
		} else if (expr instanceof TernaryExpression) {
			loadTernaryExpression((TernaryExpression) expr);
		} else if (expr instanceof IndexExpression) {
			loadIndexExpression((IndexExpression) expr);
		} else if (expr instanceof BooleanLiteralExpression) {
			mv.visitInsn(((BooleanLiteralExpression) expr).isValue() ? ICONST_1 : ICONST_0);
		} else if (expr instanceof ExplicitCastExpression) {
			loadExpression(((ExplicitCastExpression) expr).getExpression(), ((ExplicitCastExpression) expr).getTargetType());
		} else if (expr instanceof NullLiteralExpression) {
			mv.visitLdcInsn(0L);
		} else if (expr instanceof CharLiteralExpression) {
			mv.visitIntInsn(SIPUSH, ((CharLiteralExpression) expr).getCh());
		} else if (expr instanceof InitializerListExpression) {
			loadInitializerListExpression((InitializerListExpression) expr, expectedType);
		} else if (expr instanceof StringLiteralExpression) {
			mv.visitLdcInsn(((StringLiteralExpression) expr).getString());
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
		} else {
			throw new UnsupportedOperationException(expr.getClass().getName());
		}
		if (expectedType != null && expr.getReturnType() != expectedType)
			applyCast(expr.getReturnType(), expectedType);
	}

	private void loadInitializerListExpression(InitializerListExpression expr, TypeInfo expectedType) {
		List<Expression> args = expr.getArgs();
		TypeInfo expected = null;
		if (expectedType instanceof TypeArrayInfo) {
			expected = ((TypeArrayInfo) expectedType).getElementType().getType();
		}
		expr.checkImplicitCastTo_mutable(expectedType);
		for (int i = 0; i < args.size(); i++) {
			loadExpression(args.get(i), expected);
		}
	}


	private void loadIndexExpression(IndexExpression expr) {
		Expression array = expr.getArray();
		if (array.getReturnType() instanceof TypePointerInfo) {
			loadFromAddress(array, expr.getArgs().get(0));
		} else if (array.getReturnType() instanceof TypeArrayInfo) {
			loadFromArray(array, expr.getArgs().get(0));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private void loadFromArray(Expression array, Expression index) {
		loadExpressionAddress(array);
		loadPointerOffset(null, ((TypeArrayInfo) array.getReturnType()).getElementType().getType(), index);
		applyDereferensing(((TypeArrayInfo) array.getReturnType()).getElementType().getType());
	}

	private void loadVariableExpression(VariableRefExpression expr) {
		if (expr.getVariable() instanceof FieldInfo) {
			FieldInfo field = (FieldInfo) expr.getVariable();
			mv.visitFieldInsn(GETSTATIC, asClassName(field.getContainer()), field.getName(), asDescriptor(field.getTypeRef().getType()));
		} else {
			VariableHandle handle = ASMUtils.getHandle(expr.getVariable());
			if (handle == null) {
				System.err.println(expr.getLocation().getStartLine());
				throw new NullPointerException("ASMUtils.getHandle(" + expr.getVariable().getCanonicalName() + ")");
			}
			handle.loadMe(mv, this);
		}
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
			mv.visitIntInsn(BIPUSH, value);
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			mv.visitIntInsn(SIPUSH, value);
		} else {
			mv.visitLdcInsn(value);
		}
	}

	private void loadStackBasePointer() {
		mv.visitVarInsn(LLOAD, 0);
	}

	private void loadStackPointer() {
		loadStackBasePointer();
		if (stackFrameSize != 0) {
			mv.visitLdcInsn(stackFrameSize);
			mv.visitInsn(LADD);
		}
	}


	private void loadCallExpression(CallExpression expr) {
		FunctionInfo func = expr.getFunction();
		if (func.is_extern()) {
			IntrinsicFunctionsTable.loadExternCall(this, func, expr);
			return;
		}

		if (ASMUtils.functionNeedsStackPointer(func)) {
			loadStackPointer();
		}

		int i = 0;
		for (Expression arg : expr.getArgs()) {
			VariableInfo param = func.getArgs().get(i++);
			if (param.isRegister()) loadExpression(arg, param.getTypeRef().getType());
			else throw new UnsupportedOperationException(param.getCanonicalName());
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
			loadPointerOffset(pointer.getReturnType(), ((TypePointerInfo) pointer.getReturnType()).getTargetType().getType(), offset);
		}
	}

	private void loadPointerOffset(TypeInfo type, TypeInfo targetType, Expression offset) {
		loadExpression(offset); //push offset
		if (type == null) {
			applyCast(offset.getReturnType(), TypeInfo.PrimitiveTypeInfo.LONG);
		} else if (offset.getReturnType() != type) {
			applyCast(offset.getReturnType(), type);
		}
		mv.visitLdcInsn(targetType.sizeof()); //push size
		mv.visitInsn(LMUL); //mul absoffset offset * size
		mv.visitInsn(LADD); //add pointer + absoffset
	}


	void loadFromLocalAddress(TypeInfo type, long offset) {
		loadStackPointerWithOffset(offset);
		applyDereferensing(type);
	}

	private void loadFromAddress(Expression pointer, Expression offset) {
		loadPointerExpression(pointer, offset);
		TypeInfo p_type = ((TypePointerInfo) pointer.getReturnType()).getTargetType().getType();
		applyDereferensing(p_type);
	}


	private void loadTernaryExpression(TernaryExpression expr) {
		Expression cond = expr.getCond();
		Runnable ifTrue = () -> {
			loadExpression(expr.getIfFalse());
		};

		Runnable ifFalse = () -> {
			loadExpression(expr.getIfTrue());
		};
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
					loadExpression(((BinaryExpression) cond).getLhs(), ((BinaryExpression) cond).getOperator().getLeftType());
					loadExpression(((BinaryExpression) cond).getRhs(), ((BinaryExpression) cond).getOperator().getRightType());
					applyCompare(op.getLeftType(), op, ifTrue, ifFalse, NEXT_LABEL);
					this.NEXT_LABEL = NEXT_LABEL;
					return;
				default:
			}
		}
		loadExpression(cond);
		applyCompareEquals(cond.getReturnType(), ifTrue, ifFalse);
		this.NEXT_LABEL = NEXT_LABEL;
	}

	private void loadUnaryPrefixExpression(UnaryPrefixExpression expr) {
		UnaryPrefixOperator op = expr.getOperator();
		if (!op.getOp().equals("&"))
			loadExpression(expr.getExpr(), op.getTargetType().getType());

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
			case "-":
				if (op.getReturnType() == TypeInfo.PrimitiveTypeInfo.LONG) {
					mv.visitInsn(LNEG);
				} else if (op.getReturnType() == TypeInfo.PrimitiveTypeInfo.DOUBLE) {
					mv.visitInsn(DNEG);
				} else if (op.getReturnType() == TypeInfo.PrimitiveTypeInfo.FLOAT) {
					mv.visitInsn(FNEG);
				} else {
					mv.visitInsn(INEG);
				}
				break;
			case "&":
				loadExpressionAddress(expr.getExpr());
				break;
			default:
				throw new UnsupportedOperationException(op.getOp());
		}
	}

	private void loadExpressionAddress(Expression expr) {
		if (expr instanceof VariableRefExpression) {
			loadVariableAddress((VariableRefExpression) expr);
		} else throw new UnsupportedOperationException();
	}

	private void loadVariableAddress(VariableRefExpression varRef) {
		loadStackPointerWithOffset(((ShadowStackVariableHandle) ASMUtils.getHandle(varRef.getVariable())).getOffset());
	}

	private void loadBinaryExpression(BinaryExpression expr) {
		BinaryOperator op = expr.getOperator();
		loadExpression(expr.getLhs(), op.getLeftType());
		loadExpression(expr.getRhs(), op.getRightType());

		if (expr.getLhs().getReturnType() instanceof TypePointerInfo) {
			switch (op.getOp()) {
				case "+":
				case "-": {
					long size = ((TypePointerInfo) expr.getLhs().getReturnType()).getTargetType().getType().sizeof();
					mv.visitLdcInsn(size);  //push size
					mv.visitInsn(LMUL);     //mul rhs * size
				}
				default:
					throw new AssertionError(op.getOp() + " with pointer type");
			}
		}

		if (applyCompare(op.getLeftType(), op, () -> loadBoolValue(true), () -> loadBoolValue(false))) {
			return;
		}

		mv.visitInsn(ASMUtils.binaryOpcode(op));
	}

	private boolean applyCompare(TypeInfo type, BinaryOperator op, Runnable ifTrue, Runnable ifFalse) {
		Label NEXT = new Label();
		boolean result = applyCompare(type, op, ifTrue, ifFalse, NEXT);
		mv.visitLabel(NEXT);
		return result;
	}

	private boolean applyCompare(TypeInfo type, BinaryOperator op, Runnable ifTrue, Runnable ifFalse, Label NEXT) {
		switch (op.getOp()) {
			case "==":
				applyCompareEquals(type, ifTrue, ifFalse);
				return true;
			case "!=":
				applyCompareNotEquals(type, ifTrue, ifFalse);
				return true;
			case ">":
				applyCompareGreater(type, ifTrue, ifFalse);
				return true;
			case ">=":
				applyCompareGreaterOrEquals(type, ifTrue, ifFalse);
				return true;
			case "<":
				applyCompareSmaller(type, ifTrue, ifFalse);
				return true;
			case "<=":
				applyCompareSmallerOrEquals(type, ifTrue, ifFalse);
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
		applyCompare(cmp, ifTrue, ifFalse);
	}

	private void applyCompareSmaller(TypeInfo type, Runnable ifTrue, Runnable ifFalse) {
		applyCmpAs(type, IFGE, ifTrue, ifFalse);
	}

	private void applyCompareSmallerOrEquals(TypeInfo type, Runnable ifTrue, Runnable ifFalse) {
		applyCmpAs(type, IFGT, ifTrue, ifFalse);
	}

	private void applyCompareGreater(TypeInfo type, Runnable ifTrue, Runnable ifFalse) {
		applyCmpAs(type, IFLE, ifTrue, ifFalse);
	}

	private void applyCompareGreaterOrEquals(TypeInfo type, Runnable ifTrue, Runnable ifFalse) {
		applyCmpAs(type, IFLT, ifTrue, ifFalse);
	}

	private void applyCompareNotEquals(TypeInfo type, Runnable ifTrue, Runnable ifFalse) {
		applyCmpAs(type, IFEQ, ifTrue, ifFalse);
	}

	private void applyCompareEquals(TypeInfo type, Runnable ifTrue, Runnable ifFalse) {
		applyCmpAs(type, IFNE, ifTrue, ifFalse);
	}

	private void applyCompare(int cmp, Runnable ifTrue, Runnable ifFalse) {
		Label NEXT = new Label();
		applyCompare(cmp, ifTrue, new Label(), ifFalse, NEXT);
		mv.visitLabel(NEXT);                // STORE: (return)
	}

	private void applyCompare(int cmp, Runnable ifTrue, Label FALSE, Runnable ifFalse, Label NEXT) {
		mv.visitJumpInsn(cmp, ifFalse == null ? NEXT : FALSE);       //  if false goto FALSE;
		ifTrue.run();                       //  set true
		mv.visitJumpInsn(GOTO, NEXT);        //  goto STORE;

		if (ifFalse != null) {
			mv.visitLabel(FALSE);           //  FALSE:
			ifFalse.run();                  //  set false
		}
	}

	private void loadAssignExpression(AssignExpression expr) {
		Expression lv = expr.getLvalue();
		if (expr.getParentOperator() != null)
			throw new UnsupportedOperationException(expr.getParentOperator().getOp());
		loadExpression(expr.getRvalue(), lv.getReturnType());
		storeExpression(lv);
		loadExpression(lv);//assign expr must returns lvalue
	}

	//endregion

	//region #apply


	private static final HashMap<TypeInfo, Map<TypeInfo, int[]>> castTable = new HashMap<>();

	static {
		registerCast(TypeInfo.PrimitiveTypeInfo.BYTE, TypeInfo.PrimitiveTypeInfo.LONG, I2L);
		registerCast(TypeInfo.PrimitiveTypeInfo.BYTE, TypeInfo.PrimitiveTypeInfo.INT);
		registerCast(TypeInfo.PrimitiveTypeInfo.BYTE, TypeInfo.PrimitiveTypeInfo.SHORT);
		registerCast(TypeInfo.PrimitiveTypeInfo.UBYTE, TypeInfo.PrimitiveTypeInfo.BYTE, I2B);

		registerCast(TypeInfo.PrimitiveTypeInfo.SHORT, TypeInfo.PrimitiveTypeInfo.LONG, I2L);
		registerCast(TypeInfo.PrimitiveTypeInfo.SHORT, TypeInfo.PrimitiveTypeInfo.INT);
		registerCast(TypeInfo.PrimitiveTypeInfo.USHORT, TypeInfo.PrimitiveTypeInfo.SHORT, I2S);
		registerCast(TypeInfo.PrimitiveTypeInfo.SHORT, TypeInfo.PrimitiveTypeInfo.BYTE, I2B);
		registerCast(TypeInfo.PrimitiveTypeInfo.SHORT, TypeInfo.PrimitiveTypeInfo.UBYTE, I2B);

		registerCast(TypeInfo.PrimitiveTypeInfo.INT, TypeInfo.PrimitiveTypeInfo.LONG, I2L);
		registerCast(TypeInfo.PrimitiveTypeInfo.INT, TypeInfo.PrimitiveTypeInfo.SHORT, I2S);
		registerCast(TypeInfo.PrimitiveTypeInfo.INT, TypeInfo.PrimitiveTypeInfo.USHORT, I2S);
		registerCast(TypeInfo.PrimitiveTypeInfo.INT, TypeInfo.PrimitiveTypeInfo.BYTE, I2B);
		registerCast(TypeInfo.PrimitiveTypeInfo.INT, TypeInfo.PrimitiveTypeInfo.UBYTE, I2B);

		registerCast(TypeInfo.PrimitiveTypeInfo.LONG, TypeInfo.PrimitiveTypeInfo.INT, L2I);
		registerCast(TypeInfo.PrimitiveTypeInfo.LONG, TypeInfo.PrimitiveTypeInfo.UINT, L2I);
		registerCast(TypeInfo.PrimitiveTypeInfo.LONG, TypeInfo.PrimitiveTypeInfo.SHORT, L2I, I2S);
		registerCast(TypeInfo.PrimitiveTypeInfo.LONG, TypeInfo.PrimitiveTypeInfo.USHORT, L2I, I2S);
		registerCast(TypeInfo.PrimitiveTypeInfo.LONG, TypeInfo.PrimitiveTypeInfo.BYTE, L2I, I2B);
		registerCast(TypeInfo.PrimitiveTypeInfo.LONG, TypeInfo.PrimitiveTypeInfo.UBYTE, L2I, I2B);

		//signed - unsigned
		registerCastBoth(TypeInfo.PrimitiveTypeInfo.ULONG, TypeInfo.PrimitiveTypeInfo.LONG);
		registerCastBoth(TypeInfo.PrimitiveTypeInfo.UINT, TypeInfo.PrimitiveTypeInfo.INT);
	}

	private static void registerCastBoth(TypeInfo from, TypeInfo to) {
		registerCast(from, to);
		registerCast(to, from);
	}

	@SuppressWarnings("Java8MapApi")
	private static void registerCast(TypeInfo from, TypeInfo to, int... opcodes) {
		Map<TypeInfo, int[]> fromMap = castTable.get(from);
		if (fromMap == null) {
			fromMap = new HashMap<>();
			castTable.put(from, fromMap);
		}
		fromMap.put(to, opcodes);
	}

	private static int[] getCastOpcodes(TypeInfo from, TypeInfo to) {
		Map<TypeInfo, int[]> fromMap = castTable.get(from);
		if (fromMap == null) return null;
		return fromMap.get(to);
	}

	private void applyCast(TypeInfo type, TypeInfo expected) {
		if (type instanceof TypePointerInfo) type = TypeInfo.PrimitiveTypeInfo.ULONG;
		if (expected instanceof TypePointerInfo) expected = TypeInfo.PrimitiveTypeInfo.ULONG;
		if (type == TypeInfo.PrimitiveTypeInfo.CHAR32) type = TypeInfo.PrimitiveTypeInfo.UINT;
		if (expected == TypeInfo.PrimitiveTypeInfo.CHAR32) expected = TypeInfo.PrimitiveTypeInfo.UINT;
		if (type == TypeInfo.PrimitiveTypeInfo.CHAR) type = TypeInfo.PrimitiveTypeInfo.USHORT;
		if (expected == TypeInfo.PrimitiveTypeInfo.CHAR) expected = TypeInfo.PrimitiveTypeInfo.USHORT;
		if (type == TypeInfo.PrimitiveTypeInfo.CHAR8) type = TypeInfo.PrimitiveTypeInfo.UBYTE;
		if (expected == TypeInfo.PrimitiveTypeInfo.CHAR8) expected = TypeInfo.PrimitiveTypeInfo.UBYTE;
		if (type.equals(expected)) return;


		int[] op = getCastOpcodes(type, expected);
		if (op == null) {
			mv.visitMethodInsn(INVOKESTATIC, "c32/extern/Cast",
					type.getName() + "2" + expected.getName(),
					"(" + asDescriptor(type) + ")" + asDescriptor(expected),
					false);
			return;
		} else {
			for (int opcode : op) {
				mv.visitInsn(opcode);
			}
			if (true) return;
		}

		//For removal
		if (type == TypeInfo.PrimitiveTypeInfo.BYTE) {
			if (expected == TypeInfo.PrimitiveTypeInfo.DOUBLE) {
				mv.visitInsn(I2D);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.FLOAT) {
				mv.visitInsn(I2F);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.INT) {
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.SHORT) {
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.USHORT) {
				mv.visitMethodInsn(INVOKESTATIC, "c32/extern/Cast", "B2US", "(B)S", false);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.BYTE) {
				return;
			}
		} else if (type == TypeInfo.PrimitiveTypeInfo.UBYTE) {
			if (expected == TypeInfo.PrimitiveTypeInfo.BYTE) {
				return;
			}
		} else if (type == TypeInfo.PrimitiveTypeInfo.UINT) {
			if (expected == TypeInfo.PrimitiveTypeInfo.LONG) {
				mv.visitMethodInsn(INVOKESTATIC, "c32/extern/Cast", "UI2L", "(B)J", false);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.ULONG || expected instanceof TypePointerInfo) {
				mv.visitMethodInsn(INVOKESTATIC, "c32/extern/Cast", "UI2UL", "(B)J", false);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.DOUBLE) {
				mv.visitMethodInsn(INVOKESTATIC, "c32/extern/Cast", "UI2D", "(B)D", false);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.FLOAT) {
				mv.visitMethodInsn(INVOKESTATIC, "c32/extern/Cast", "UI2F", "(B)F", false);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.INT) {
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.UINT) {
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.SHORT) {
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.USHORT) {
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.BYTE) {
				return;
			}
		} else if (type == TypeInfo.PrimitiveTypeInfo.INT) {
			if (expected == TypeInfo.PrimitiveTypeInfo.LONG) {
				mv.visitInsn(I2L);
				return;
			} else if (expected == TypeInfo.PrimitiveTypeInfo.ULONG || expected instanceof TypePointerInfo) {
				mv.visitMethodInsn(INVOKESTATIC, "c32/extern/Cast", "I2UL", "(I)J", false);
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
		mv.visitJumpInsn(IFNE, FALSE);
		mv.visitInsn(ICONST_1);
		mv.visitJumpInsn(GOTO, RET);
		mv.visitLabel(FALSE);
		mv.visitInsn(ICONST_0);
		mv.visitLabel(RET);
	}

	private void applyDereferensing(TypeInfo returnType) {
		Class<?> primitive = asJavaPrimitive(returnType);
		if (primitive != null) {
			char[] name = primitive.getCanonicalName().toCharArray();
			name[0] = Character.toUpperCase(name[0]);
			String methodName = "get" + String.valueOf(name);
			String descriptor = "(J)" + ASMUtils.asDescriptor(returnType);
			mv.visitMethodInsn(INVOKESTATIC, "c32/extern/Memory", methodName, descriptor, false);
		} else if (returnType instanceof TypeArrayInfo) {
			TypeArrayInfo arrayType = (TypeArrayInfo) returnType;
			if (!arrayType.isStaticArray()) throw new UnsupportedOperationException();
			for (int i = 0; i < arrayType.getStaticLength(); i++) {
				throw new UnsupportedOperationException("aaaaaa");
			}
			throw new UnsupportedOperationException("aaaaaaaaaaaaaaaa");
		} else
			throw new UnsupportedOperationException(returnType.getCanonicalName());
	}

	//endregion


	private Label writeLabel(Location location) {
		return writeLabel(new Label(), location);
	}

	private Label writeLabel(Label l, Location location) {
		mv.visitLabel(l);
		mv.visitLineNumber(location == null ? -1 : location.getStartLine(), l);
		return l;
	}
}
