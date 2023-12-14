package c32.compiler;

import c32.compiler.ast.FunctionDeclarationTree;
import com.squareup.javapoet.TypeName;

import java.lang.reflect.Type;
import java.util.HashMap;

public final class IntrinsicFunctionTable {
	private static final HashMap<String,String> names = new HashMap<>();
	private static final HashMap<String,TypeName> jRetTypes = new HashMap<>();
	static {
		setOverload("java.lang.System.out.println",TypeName.VOID,
				"println",
				"println_byte",
				"println_bool",
				"println_short",
				"println_char",
				"println_int",
				"println_float",
				"println_long",
				"println_double");
		setOverload("java.lang.System.out.print",TypeName.VOID,
				"print",
				"print_byte",
				"print_bool",
				"print_short",
				"print_char",
				"print_int",
				"print_float",
				"print_long",
				"print_double");

		setOverload("c32.Unsigned.println",TypeName.VOID,
				"println_ubyte",
				"println_ushort",
				"println_uint",
				"println_ulong");
		setOverload("c32.Unsigned.print",TypeName.VOID,
				"print_ubyte",
				"print_ushort",
				"print_uint",
				"print_ulong");
	}
	private static void setOverload(String target,TypeName jRetType, String... variations) {
		for (String v : variations) {
			names.put(v,target);
			jRetTypes.put(v,jRetType);
		}
	}

	public static String getReplacement(FunctionDeclarationTree function, String fname) {
		TypeName jRetType = function.getReturnType().getJavaType();
		if (names.containsKey(fname)) {
			String ret = names.get(fname);
			if (jRetTypes.get(fname) == jRetType)
				return ret;
		}
		throw new CompilerException("Extern function implementation not found: " + function);
	}
}
