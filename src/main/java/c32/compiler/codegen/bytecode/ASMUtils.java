package c32.compiler.codegen.bytecode;

import c32.compiler.Location;
import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.BinaryOperator;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
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
			if (type == BYTE || type == UBYTE) return "B";
			if (type == SHORT || type == USHORT) return "S";
			if (type == INT || type == UINT) return "I";
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
		String name = space.getFullName();
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

	public static Label mkLabel(MethodVisitor mv, Location location) {
		return mkLabel(mv,new Label(),location);
	}
	public static Label mkLabel(MethodVisitor mv, Label l, Location location) {
		mv.visitLabel(l);
		mv.visitLineNumber(location == null ? -1 : location.getStartLine(),l);
		return l;
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



	public static void store_register(MethodVisitor mv, Class<?> type, @Nullable VariableInfo var) {
		int index = ++JVMGenerator.localVariableIndex;
		if (type == long.class || type == double.class)
			++JVMGenerator.localVariableIndex;

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
			storeRegisterVariable(var,index);
	}
	public static void storeRegisterVariable(VariableInfo var, int index) {
		varHandles.put(var,new IndexedVariableHandle(var,index));
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
		Class<?> cls = asJavaPrimitive(type);
		if (cls == null)
			throw new UnsupportedOperationException(type.getCanonicalName());
		if (cls == double.class) {
			return DLOAD;
		}
		if (cls == float.class) {
			return FLOAD;
		}
		if (cls == long.class) {
			return LLOAD;
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
}
