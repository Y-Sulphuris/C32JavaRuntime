package c32.compiler.codegen.cs;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.FunctionInfo;

import java.util.HashMap;

public final class IntrinsicFunctionTable {
	private static final HashMap<String,String> names = new HashMap<>();
	private static final HashMap<String,String> jRetTypes = new HashMap<>();
	static {
		setOverload("System.Console.Out.WriteLine","void",
				"println",
				"println$byte",
				"println$bool",
				"println$short",
				"println$char",
				"println$int",
				"println$uint",
				"println$float",
				"println$long",
				"println$ulong",
				"println$double",
				"println$__array__$char$");
		setOverload("System.Console.Out.Write","void",
				"print",
				"print$byte",
				"print$ubyte",
				"print$bool",
				"print$short",
				"print$ushort",
				"print$char",
				"print$int",
				"print$uint",
				"print$float",
				"print$long",
				"print$ulong",
				"print$double",
				"print$__array__$char$");
	}
	private static void setOverload(String target,String retType, String... variations) {
		for (String v : variations) {
			names.put(v,target);
			jRetTypes.put(v,retType);
		}
	}

	public static String get(FunctionInfo function, String fname) {
		String jRetType = CSGenerator.getCSTypeName(function.getReturnType());
		if (names.containsKey(function.getName())) {
			String ret = names.get(fname);
			if (jRetTypes.get(function.getName()).equals(jRetType))
				return ret;
		}
		throw new CompilerException(null, "Extern function implementation not found: " + function);
	}
}
