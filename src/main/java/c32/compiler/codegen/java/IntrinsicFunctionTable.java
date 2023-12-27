package c32.compiler.codegen.java;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.FunctionInfo;
import c32.compiler.logical.tree.VariableInfo;

import java.util.HashMap;

public final class IntrinsicFunctionTable {
	private static final HashMap<String,String> names = new HashMap<>();
	private static final HashMap<String,String> jRetTypes = new HashMap<>();
	static {
		setOverload("java.lang.System.out.println","void",
				"println",
				"println$byte",
				"println$bool",
				"println$short",
				"println$char",
				"println$int",
				"println$float",
				"println$long",
				"println$double",
				"println$__array__$char$");
		setOverload("java.lang.System.out.print","void",
				"print",
				"print$byte",
				"print$bool",
				"print$short",
				"print$char",
				"print$int",
				"print$float",
				"print$long",
				"print$double",
				"println$__array__$char$");

		setOverload("c32.Unsigned.println","void",
				"println$ubyte",
				"println$ushort",
				"println$uint",
				"println$ulong");
		setOverload("c32.Unsigned.print","void",
				"print$ubyte",
				"print$ushort",
				"print$uint",
				"print$ulong");
	}

	private static void setOverload(String target, String retType, String... variations) {
		for (String v : variations) {
			names.put(v,target);
			jRetTypes.put(v,retType);
		}
	}

	public static String get(FunctionInfo function, String fname) {
		String jRetType = JavaGenerator.getJavaTypeName(function.getReturnType());
		if (names.containsKey(fname)) {
			String ret = names.get(fname);
			if (jRetTypes.get(function.getName()).equals(jRetType))
				return ret;
		}
		throw new CompilerException(null, "Extern function implementation not found: " + fname);
	}

}
