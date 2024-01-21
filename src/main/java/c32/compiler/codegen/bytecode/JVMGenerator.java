package c32.compiler.codegen.bytecode;

import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.*;
import c32.compiler.logical.tree.statement.*;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static c32.compiler.codegen.bytecode.ASMUtils.*;
import static org.objectweb.asm.Opcodes.*;

public class JVMGenerator implements c32.compiler.codegen.Generator {

	private static final int[] classVersion = new int[22+1]; static {
		classVersion[ 0] = 45;
		classVersion[ 1] = 45;
		classVersion[ 2] = 46;
		classVersion[ 3] = 47;
		classVersion[ 4] = 48;
		classVersion[ 5] = 49;
		classVersion[ 6] = 50;
		classVersion[ 7] = 51;
		classVersion[ 8] = 52;
		classVersion[ 9] = 53;
		classVersion[10] = 54;
		classVersion[11] = 55;
		classVersion[12] = 56;
		classVersion[13] = 57;
		classVersion[14] = 58;
		classVersion[15] = 59;
		classVersion[16] = 60;
		classVersion[17] = 61;
		classVersion[18] = 62;
		classVersion[19] = 63;
		classVersion[20] = 64;
		classVersion[21] = 65;
		classVersion[22] = 66;
	}

	public static int javaVersion(int i) {
		return classVersion[i];
	}

	private final int version = 8;
	private int classVersion() {
		return javaVersion(version);
	}

	static int localVariableIndex = 0;
	static HashMap<LabelStatement, Label> labels = new HashMap<>();


	@Override
	public void generate(NamespaceInfo space) {
		try {
			//JarOutputStream jar = new JarOutputStream(Files.newOutputStream(new File("test.jar").toPath()));

			/*ClassWriter cw = new ClassWriter(0);
			cw.visit(classVersion(),ACC_PUBLIC + ACC_SUPER,"test/package",null,"java/lang/Object",null);
			cw.visitSource("file",null);

			{
				MethodVisitor method = cw.visitMethod(ACC_PUBLIC | ACC_STATIC,"main","([Ljava/lang/String;)V",null,null);
				method.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");
				method.visitLdcInsn("hello");
				method.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V",false);
				method.visitInsn(RETURN);
				method.visitMaxs(2,1);
				method.visitEnd();
			}
			cw.visitEnd();*/

			Map<SpaceInfo, ClassWriter> writers = writeAll(space);
			for (Map.Entry<SpaceInfo, ClassWriter> entry : writers.entrySet()) {
				String clName = asClassName(entry.getKey());
				File dir = new File("out/jvm/" + (clName.contains("/") ? clName.substring(0,clName.lastIndexOf('/')) : ""));
				dir.mkdirs();

				File clFile = new File("out/jvm/"+clName + ".class");
				System.out.println("writing " + clFile.getPath() + "...");
				clFile.createNewFile();

				OutputStream stream = Files.newOutputStream(clFile.toPath());
				stream.write(entry.getValue().toByteArray());
				stream.close();
			}


			//jar.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private HashMap<SpaceInfo, ClassWriter> writeAll(NamespaceInfo namespace) {
		HashMap<SpaceInfo, ClassWriter> writers = new HashMap<>();
		ClassWriter namespaceCv = writeNamespaceItself(namespace);
		writers.put(namespace, namespaceCv);
		for (NamespaceInfo namespaceNamespace : namespace.getNamespaces()) {
			writers.putAll(writeAll(namespaceNamespace));
		}

		return writers;
	}

	private void writeField(ClassWriter cv, FieldInfo field) {
		int mod = ACC_PUBLIC | ACC_STATIC;
		if (field.getVariable().getTypeRef().is_const()) mod |= ACC_FINAL;
		FieldVisitor fv = cv.visitField(mod,field.getName(),asDescriptor(field.getTypeRef().getType()),null,asFieldInitializerValue(field));
		fv.visitEnd();
	}

	private Object asFieldInitializerValue(FieldInfo fieldInfo) {
		/*Expression initializer = fieldInfo.getInitializer();
		if (initializer instanceof NumericLiteralExpression) {
			Number num = ((NumericLiteralExpression) initializer).getNumber();
			if (fieldInfo.getTypeRef().getType() == BYTE)
		}*/
		return null;//todo;
	}


	private void writeFunction(ClassWriter cv, FunctionInfo function) {
		if (function.is_extern()) return;
		int mod = ACC_PUBLIC | ACC_STATIC;
		if (function.is_native()) mod |= ACC_NATIVE;
		MethodVisitor mv = cv.visitMethod(mod, asFunctionName(function),asJavaFunctionDescriptor(function),null,null);

		localVariableIndex++;//skip (ptr size = 8)
		for (VariableInfo arg : function.getArgs()) {
			//0 - stack pointer (always)
			if (!arg.is_register()) storeRegisterVariable(arg, ++localVariableIndex);
			if (arg.getTypeRef().getType().sizeof() > 4) ++localVariableIndex;
		}
		if (function instanceof FunctionImplementationInfo) {
			FunctionImplementationInfo func = (FunctionImplementationInfo) function;
			writeStatement(mv,func,func.getImplementation());
		}

		//args will be ignored
		mv.visitMaxs(16,16);

		mv.visitEnd();
		localVariableIndex = 0;
		labels = new HashMap<>();
	}

	private void writeStatement(MethodVisitor mv, FunctionImplementationInfo func, Statement statement) {
		if (statement instanceof BlockStatement) {
			writeBlockStatement(mv,func,(BlockStatement)statement);
		} else if (statement instanceof VariableDeclarationStatement) {
			writeVariableDeclarationStatement(mv, (VariableDeclarationStatement) statement);
		} else if (statement instanceof ExpressionStatement) {
			ASMUtils.mkLabel(mv,statement.getLocation());
			TypeInfo retType = ((ExpressionStatement) statement).getExpression().getReturnType();
			pushExpression(mv, ((ExpressionStatement) statement).getExpression(), retType);
			if (retType != TypeInfo.PrimitiveTypeInfo.VOID) {
				mv.visitInsn(retType.sizeof() > 4 ? POP2 : POP);
			}
		} else if (statement instanceof ReturnStatement) {
			writeReturn(mv, (ReturnStatement)statement);
		} else if (statement instanceof LabelStatement) {
			writeLabel(mv, (LabelStatement)statement);
		} else if (statement instanceof GotoStatement) {
			writeGoto(mv, (GotoStatement)statement);
		}
		else {
			throw new UnsupportedOperationException(statement.getClass().getName());
		}
	}

	private void writeGoto(MethodVisitor mv, GotoStatement statement) {
		mv.visitJumpInsn(GOTO,labels.get(statement.getLabel()));
	}

	private void writeLabel(MethodVisitor mv, LabelStatement statement) {
		labels.put(statement,ASMUtils.mkLabel(mv,statement.getLocation()));
	}

	private void writeReturn(MethodVisitor mv, ReturnStatement statement) {
		if (statement.getExpr() == null) {
			mv.visitInsn(RETURN);
		} else {
			TypeInfo type = statement.getFunction().getReturnType();
			pushExpression(mv, statement.getExpr(), type);
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
	}


	@Deprecated
	static void pushExpression(MethodVisitor mv, Expression expr) {
		pushExpression(mv,expr,null);
	}
	static void pushExpression(MethodVisitor mv, Expression expr, TypeInfo expectedType) {
		if (expr instanceof CallExpression) {
			pushCallExpression(mv, (CallExpression)expr);
		} else if (expr instanceof NumericLiteralExpression) {
			pushNumericLiteralExpression(mv, (NumericLiteralExpression)expr);
		} else if (expr instanceof VariableRefExpression) {
			pushVariableRefExpression(mv, (VariableRefExpression)expr);
		} else if (expr instanceof AssignExpression) {
			pushAssignExpression(mv, (AssignExpression)expr);
		} else if (expr instanceof BinaryExpression) {
			pushBinaryExpression(mv, (BinaryExpression)expr);
		} else if (expr instanceof UnaryPrefixExpression) {
			pushUnaryPrefixExpression(mv, (UnaryPrefixExpression)expr);
		} else if (expr instanceof IndexExpression) {
			pushIndexExpression(mv, (IndexExpression)expr);
		}
		else {
			throw new UnsupportedOperationException(expr.getClass().getName());
		}
		if (expectedType != null && expr.getReturnType() != expectedType)
			pushCast(mv,expr.getReturnType(),expectedType);
	}

	private static void pushCast(MethodVisitor mv, TypeInfo type, TypeInfo expected) {
		if (type instanceof TypePointerInfo && expected instanceof TypePointerInfo) return;

		if (type == TypeInfo.PrimitiveTypeInfo.INT) {
			if (expected == TypeInfo.PrimitiveTypeInfo.LONG) {
				mv.visitInsn(I2L);
				return;
			}
			if (expected == TypeInfo.PrimitiveTypeInfo.ULONG || expected instanceof TypePointerInfo) {
				mv.visitMethodInsn(INVOKESTATIC,"c32/extern/Runtime","I2UL","(I)J",false);
				return;
			}
			if (expected == TypeInfo.PrimitiveTypeInfo.DOUBLE) {
				mv.visitInsn(I2D);
				return;
			}
			if (expected == TypeInfo.PrimitiveTypeInfo.FLOAT) {
				mv.visitInsn(I2F);
				return;
			}
			if (expected == TypeInfo.PrimitiveTypeInfo.SHORT) {
				mv.visitInsn(I2S);
				return;
			}
			if (expected == TypeInfo.PrimitiveTypeInfo.BYTE) {
				mv.visitInsn(I2B);
				return;
			}

		}

		throw new UnsupportedOperationException(type.getCanonicalName() + " and " + expected.getCanonicalName());
	}

	private static void pushIndexExpression(MethodVisitor mv, IndexExpression expr) {
		Expression array = expr.getArray();
		if (array.getReturnType() instanceof TypePointerInfo) {
			movabs(mv, array, expr.getArgs().get(0));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private static void pushUnaryPrefixExpression(MethodVisitor mv, UnaryPrefixExpression expr) {
		UnaryPrefixOperator op = expr.getOperator();
		pushExpression(mv,expr.getExpr(),op.getTargetType().getType());

		if (op.getOp().equals("*") && expr.getExpr().getReturnType() instanceof TypePointerInfo) {
			movabs(mv, expr.getExpr(),null);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private static void pushBinaryExpression(MethodVisitor mv, BinaryExpression expr) {
		BinaryOperator op = expr.getOperator();
		pushExpression(mv,expr.getLhs(),op.getLeftType());
		pushExpression(mv,expr.getRhs(),op.getReturnType());
		switch (op.getOp()) {
			case "+":
			case "-":
				if (expr.getLhs().getReturnType() instanceof TypePointerInfo) {
					long size = ((TypePointerInfo) expr.getLhs().getReturnType()).getTargetType().getType().sizeof();
					mv.visitLdcInsn(size);  //push size
					mv.visitInsn(LMUL);     //mul rhs * size
				} else {
					throw new AssertionError(op.getOp() + " with pointer type");
				}
		}

		mv.visitInsn(ASMUtils.binaryOpcode(op));
	}

	private static void pushAssignExpression(MethodVisitor mv, AssignExpression expr) {
		Expression lv = expr.getLvalue();
		if (expr.getParentOperator() != null) throw new UnsupportedOperationException(expr.getParentOperator().getOp());
		if (lv instanceof VariableRefExpression) {
			writeVariable(mv,((VariableRefExpression) lv).getVariable(),expr.getRvalue());
		} else if (lv instanceof UnaryPrefixExpression) {
			Expression pointer = ((UnaryPrefixExpression) lv).getExpr();
			movabs(mv, pointer,null,expr.getRvalue());
			movabs(mv, pointer, null);//assign expr must returns lvalue
		} else if (lv instanceof IndexExpression) {
			Expression pointer = ((IndexExpression) lv).getArray();
			if (pointer.getReturnType() instanceof TypePointerInfo) {
				Expression index = ((IndexExpression) lv).getArgs().get(0);
				movabs(mv, pointer, index, expr.getRvalue());
				movabs(mv, pointer, index);//assign expr must returns lvalue
			} else {
				throw new UnsupportedOperationException();
			}
		}
		else {
			throw new UnsupportedOperationException(lv.getClass().getName());
		}
	}


	private static void push_mov_address(MethodVisitor mv, Expression pointer, Expression offset) {
		if (!(pointer.getReturnType() instanceof TypePointerInfo)) throw new IllegalArgumentException();
		pushExpression(mv, pointer); //push pointer
		if (offset != null) {
			pushExpression(mv, offset); //push offset
			if (offset.getReturnType() != pointer.getReturnType()) {
				pushCast(mv,offset.getReturnType(),pointer.getReturnType());
			}
			mv.visitLdcInsn(((TypePointerInfo) pointer.getReturnType()).getTargetType().getType().sizeof()); //push size
			mv.visitInsn(LMUL); //mul absoffset offset * size
			mv.visitInsn(LADD); //add pointer + absoffset
		}
	}

	private static void movabs(MethodVisitor mv, Expression pointer, Expression offset) {
		push_mov_address(mv, pointer, offset);
		TypeInfo p_type = ((TypePointerInfo) pointer.getReturnType()).getTargetType().getType();
		Class<?> primitive = asJavaPrimitive(p_type);
		char[] name = primitive.getCanonicalName().toCharArray();
		name[0] = Character.toUpperCase(name[0]);
		String methodName = "get" + String.valueOf(name);
		String descriptor = "(J)" + ASMUtils.asDescriptor(p_type);
		mv.visitMethodInsn(INVOKESTATIC,"c32/extern/Memory", methodName, descriptor, false);
	}

	private static void movabs(MethodVisitor mv, Expression pointer, Expression offset, Expression NewValue) {
		push_mov_address(mv, pointer, offset);
		pushExpression(mv, NewValue); //push new_value
		TypeInfo p_type = ((TypePointerInfo) pointer.getReturnType()).getTargetType().getType();
		Class<?> primitive = asJavaPrimitive(p_type);
		char[] name = primitive.getCanonicalName().toCharArray();
		name[0] = Character.toUpperCase(name[0]);
		String methodName = "put" + String.valueOf(name);
		String descriptor = "(J" + ASMUtils.asDescriptor(p_type) + ")V";
		mv.visitMethodInsn(INVOKESTATIC,"c32/extern/Memory", methodName, descriptor, false);
	}


	private static void pushVariableRefExpression(MethodVisitor mv, VariableRefExpression expr) {
		VariableHandle handle = ASMUtils.getHandle(expr.getVariable());
		if (handle == null) System.err.println(expr.getLocation().getStartLine());
		handle.push(mv);
	}

	private static void pushNumericLiteralExpression(MethodVisitor mv, NumericLiteralExpression expr) {
		if (expr.getReturnType().sizeof() < 4 ||
				(expr.getReturnType() instanceof TypeInfo.IntegerPrimitiveTypeInfo && expr.getReturnType().sizeof() == 4))
			//bipush 0
			mv.visitIntInsn(BIPUSH,expr.getNumber().intValue());
		else {
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



	private static void pushCallExpression(MethodVisitor mv, CallExpression expr) {
		FunctionInfo func = expr.getFunction();
		if (ASMUtils.functionNeedsStackPointer(func)) {
			mv.visitVarInsn(LLOAD,0);
		}
		if (func.is_extern()) {
			ExternFunctionsTable.pushExternCall(mv,func,expr);
		} else {
			int i = 0;
			for (Expression arg : expr.getArgs()) {
				pushExpression(mv, arg, expr.getFunction().getArgs().get(i++).getTypeRef().getType());
			}
			mv.visitMethodInsn(INVOKESTATIC,
					ASMUtils.asClassName(func.getParent()),
					ASMUtils.asFunctionName(func),
					ASMUtils.asJavaFunctionDescriptor(func),
					false);
		}
	}

	private void writeVariableDeclarationStatement(MethodVisitor mv, VariableDeclarationStatement statement) {
		for (VariableInfo variable : statement.getVariables()) {
			writeVariable(mv,variable,variable.getInitializer());
		}
	}

	private static void writeVariable(MethodVisitor mv, VariableInfo variable, Expression initializer) {
		ASMUtils.mkLabel(mv,variable.getLocation());
		pushExpression(mv, initializer, variable.getTypeRef().getType());
		if (ASMUtils.canBePresentAsJavaPrimitive(variable.getTypeRef().getType())) {
			Class<?> cls = ASMUtils.asJavaPrimitive(variable.getTypeRef().getType());
			store_register(mv,cls,variable);
		} else {
			throw new UnsupportedOperationException(variable.getTypeRef().getType().toString());
		}
	}

	private void writeBlockStatement(MethodVisitor mv, FunctionImplementationInfo func, BlockStatement statement) {
		for (Statement state : statement.getStatements()) {
			writeStatement(mv,func,state);
		}
	}


	private ClassWriter writeNamespaceItself(NamespaceInfo namespace) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(classVersion(),
			ACC_PUBLIC | ACC_SUPER | ACC_FINAL,
			asClassName(namespace),
			null,
			"c32/extern/NamespaceSymbol",
			null);
		//cw.visitSource(namespace.getName(),null);

		for (FieldInfo field : namespace.getFields()) {
			writeField(cw,field);
		}
		for (FunctionInfo function : namespace.getFunctions()) {
			writeFunction(cw, function);
		}
		if (namespace.getParent() == null) {
			ASMUtils.generateMainFunction(cw);
		}

		cw.visitEnd();
		return cw;
	}


}
