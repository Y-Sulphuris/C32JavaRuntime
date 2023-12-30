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
				"std.println",
				"std.println$byte",
				"std.println$bool",
				"std.println$short",
				"std.println$char",
				"std.println$int",
				"std.println$float",
				"std.println$long",
				"std.println$double",
				"std.println$__array__$char$");
		setOverload("java.lang.System.out.print","void",
				"std.print",
				"std.print$byte",
				"std.print$bool",
				"std.print$short",
				"std.print$char",
				"std.print$int",
				"std.print$float",
				"std.print$long",
				"std.print$double",
				"std.print$__array__$char$");

		setOverload("c32.Unsigned.println","void",
				"std.println$ubyte",
				"std.println$ushort",
				"std.println$uint",
				"std.println$ulong");
		setOverload("c32.Unsigned.print","void",
				"std.print$ubyte",
				"std.print$ushort",
				"std.print$uint",
				"std.print$ulong");
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
			if (jRetTypes.get(fname).equals(jRetType))
				return ret;
		}
		throw new CompilerException(null, "Extern function implementation not found: " + fname);
	}

}
