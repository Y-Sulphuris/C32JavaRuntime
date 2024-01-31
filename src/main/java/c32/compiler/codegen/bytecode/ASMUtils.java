package c32.compiler.codegen.bytecode;

import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.BinaryOperator;
import c32.compiler.logical.tree.statement.BlockStatement;
import c32.compiler.logical.tree.statement.LabelStatement;
import c32.compiler.logical.tree.statement.NopStatement;
import c32.compiler.logical.tree.statement.Statement;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static c32.compiler.logical.tree.TypeInfo.PrimitiveTypeInfo.*;
import static c32.compiler.logical.tree.TypeInfo.PrimitiveTypeInfo.DOUBLE;
import static c32.compiler.logical.tree.TypeInfo.PrimitiveTypeInfo.FLOAT;
import static c32.compiler.logical.tree.TypeInfo.PrimitiveTypeInfo.LONG;
import static c32.compiler.logical.tree.TypeInfo.PrimitiveTypeInfo.ULONG;
import static org.objectweb.asm.Opcodes.*;

final class ASMUtils {

	private ASMUtils() {}

	public static String asDescriptor(TypeInfo type) {
		if (type instanceof TypeInfo.PrimitiveTypeInfo) {
			if (type == BOOL) return "Z";
			if (type == BYTE || type == UBYTE || type == CHAR8) return "B";
			if (type == SHORT || type == USHORT || type == HALF) return "S";
			if (type == CHAR) return "C";
			if (type == INT || type == UINT || type == CHAR32) return "I";
			if (type == LONG || type == ULONG) return "J";
			if (type == FLOAT) return "F";
			if (type == DOUBLE) return "D";
			if (type == VOID) return "V";
		} else if (type instanceof TypePointerInfo) {
			return "J";
		}
		return 'L'+asClassName((SpaceInfo)type) + ";";
	}

	private static final String PACKAGE_CLASSNAME = "/$package";

	@SuppressWarnings("ConstantValue")
	public static String asClassName(SpaceInfo space) {
		String name;
		if (space instanceof FunctionInfo) {
			FunctionInfo func = (FunctionInfo) space;
			name = func.getParent().getFullName();
			name += "." + asFunctionName(func);
		} else {
			name = space.getFullName();
		}
		if (name.isEmpty()) return PACKAGE_CLASSNAME.isEmpty() ? "c32/$root" : "c32" + PACKAGE_CLASSNAME;
		name = "c32/" + name.replace(".","/");
		if (space instanceof NamespaceInfo) {
			name += PACKAGE_CLASSNAME;
		}
		return name;
	}

	public static String asFunctionName(FunctionInfo function) {
		StringBuilder name = new StringBuilder(function.getName());
		List<VariableInfo> args = function.getArgs();
		if (args.isEmpty()) return name.toString();
		for (VariableInfo arg : args) {
			name.append("£").append(arg.getTypeRef().getType().getName());
		}
		return name.toString();
	}

	public static boolean functionNeedsStackPointer(FunctionInfo function) {
		return !function.is_extern();
	}

	public static String asJavaFunctionDescriptor(FunctionInfo function) {
		StringBuilder desc = new StringBuilder("(");
		if (functionNeedsStackPointer(function)) desc.append('J');
		//int i = 0;
		for (VariableInfo arg : function.getArgs()) {
			desc.append(asDescriptor(arg.getTypeRef().getType()));
			//if (i != function.getArgs().size() - 1) desc.append(';');
			//i++;
		}
		desc.append(')').append(asDescriptor(function.getReturnType()));
		return desc.toString();
	}


	/*public static void alloca_reg(MethodVisitor mv, Class<?>[] types, Expression[] initializers) {
		if (types.length != initializers.length)
			throw new IllegalArgumentException("types.length != initializers.length (" + types.length + "!=" + initializers.length + ")");

		for (int i = 0; i < types.length; i++) {
			alloca_reg(mv,types[i],initializers[i],null);
		}
	}*/

	private static final Map<VariableInfo,VariableHandle> varHandles = new HashMap<>();
	public static VariableHandle getHandle(VariableInfo variable) {
		return varHandles.get(variable);
	}



	@SuppressWarnings("RedundantIfStatement")
	public static boolean needSeparateLabel(Statement statement) {
		if (statement instanceof LabelStatement || statement instanceof NopStatement) return false;
		if (statement instanceof BlockStatement && !((BlockStatement) statement).getStatements().isEmpty()) return false;
		return true;
	}


	public static void addRegisterVariableHandle(VariableInfo var, int index) {
		varHandles.put(var, new IndexedVariableHandle(var,index));
	}
	public static void addLocalVariableHandle(VariableInfo var, long offset) {
		varHandles.put(var, new ShadowStackVariableHandle(var, offset));
	}

	public static Class<?> asJavaPrimitive(TypeInfo type) {
		if (type instanceof PrimitiveTypeInfo) {
			if (type == BOOL) return boolean.class;
			if (type == FLOAT) return float.class;
			if (type == DOUBLE) return double.class;

			if (type == CHAR || type == HALF) return char.class;
			if (type == SHORT || type == USHORT) return short.class;
			if (type == LONG || type == ULONG) return long.class;

			if (type == BYTE || type == UBYTE || type == CHAR8) return byte.class;
			if (type == INT || type == UINT || type == CHAR32) return int.class;
		}
		if (type instanceof TypePointerInfo) {
			return long.class;
		}
		return null;
	}

	public static boolean canBePresentAsJavaPrimitive(TypeInfo type) {
		return (type instanceof PrimitiveTypeInfo || type instanceof TypePointerInfo) && type.sizeof() <= 8;
	}

	public static int getLoadInstruction(TypeInfo type) {
		if (type == DOUBLE) {
			return DLOAD;
		}
		if (type == FLOAT) {
			return FLOAD;
		}
		if (type.sizeof() == 8) {
			return LLOAD;
		}
		if (type.sizeof() > 4) {
			throw new UnsupportedOperationException(type.getCanonicalName());
		}
		return ILOAD;
	}

	private static final int INT_BASE = 0, LONG_BASE = 1, FLOAT_BASE = 2, DOUBLE_BASE = 3;

	private static final int
			ADD_BASE = IADD,
			SUB_BASE = ISUB,
			MUL_BASE = IMUL,
			DIV_BASE = IDIV,
			MOD_BASE = IREM,
			SHL_BASE = ISHL,
			SHR_BASE = ISHR,
			USHR_BASE = IUSHR;

	private static final HashMap<String,Integer> opBase = new HashMap<>();
	static {
		opBase.put("+", ADD_BASE);
		opBase.put("-", SUB_BASE);
		opBase.put("*", MUL_BASE);
		opBase.put("/", DIV_BASE);
		opBase.put("%", MOD_BASE);
		opBase.put("<<",SHL_BASE);
		opBase.put("<<<",SHL_BASE);
		opBase.put(">>",SHR_BASE);
		opBase.put(">>>",USHR_BASE);
	}

	public static int binaryOpcode(BinaryOperator operator) {
		Class<?> type = asJavaPrimitive(operator.getReturnType());
		int typeBase;
		if (type == double.class) {
			typeBase = DOUBLE_BASE;
		} else if (type == float.class) {
			typeBase = FLOAT_BASE;
		} else if (type == long.class) {
			typeBase = LONG_BASE;
		} else {
			typeBase = INT_BASE;
		}//todo: bit shift for unsigned types always unsigned
		Integer op = opBase.get(operator.getOp());
		if (op == null)
			throw new UnsupportedOperationException(operator.getOp());
		return op + typeBase;
	}

	public static int getCmpType(TypeInfo type) {
		if (type == TypeInfo.PrimitiveTypeInfo.DOUBLE) {
			return DCMPL;
		} else if (type == TypeInfo.PrimitiveTypeInfo.FLOAT) {
			return FCMPL;
		} else if (type == TypeInfo.PrimitiveTypeInfo.LONG) {
			return LCMP;
		}
		return 0;
	}

	public static void generateMainFunction(ClassWriter cw) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC,"main","([Ljava/lang/String;)V",null,null);
		mv.visitLdcInsn(1L);
		mv.visitLdcInsn(1024L);
		mv.visitMethodInsn(INVOKESTATIC,"c32/extern/Memory","calloc","(JJ)J",false);
		//mv.visitVarInsn(LSTORE,1);
		mv.visitMethodInsn(INVOKESTATIC,"c32/test"+PACKAGE_CLASSNAME,"main","(J)V",false);
		/*mv.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");
		mv.visitLdcInsn("hello");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V",false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(2,1);*/
		mv.visitInsn(RETURN);
		mv.visitMaxs(4,3);
		mv.visitEnd();
	}

	public static long calcStackFrameSize(FunctionImplementationInfo function) {
		long size = 0;
		for (VariableInfo variable : function.getImplementation().collectLocalVariables()) {
			if (!variable.isRegister()) size += variable.getTypeRef().getType().sizeof();
		}
		return size;
	}
}
