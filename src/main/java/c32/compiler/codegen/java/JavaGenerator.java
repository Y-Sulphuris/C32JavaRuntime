package c32.compiler.codegen.java;

import c32.compiler.logical.tree.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class JavaGenerator {
	public void generate(SpaceInfo space) {
		File dir = new File("out");
		dir.mkdir();
		writeNamespace(space);

	}
	private void writeNamespace(SpaceInfo space) {
		String className = "$package";
		String packageName = getJavaPackageName(space);
		File dir = new File("out/"+packageName.replace('.','/')+'/');
		dir.mkdirs();
		File file = new File(dir,className+".java");
		for (SpaceInfo namespace : space.getNamespaces()) {
			writeNamespace(namespace);
		}
		try {
			file.createNewFile();
			PrintStream out = new PrintStream(file);
			if (!packageName.isEmpty()) out.println("package " + packageName + ";");
			out.println("public final class " + className + " {");
			out.println("\tprivate " + className + "() throws java.lang.InstantiationException { throw new java.lang.InstantiationException(); }");
			for (FunctionInfo function : space.getFunctions()) {
				writeFunctionBody(function,out);
				writeFunctionNamespace(function,out);
			}
			out.println('}');
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeFunctionNamespace(FunctionInfo function, PrintStream out) {

	}

	private String getJavaPackageName(SpaceInfo space) {
		StringBuilder builder = new StringBuilder(space.getName());
		while (space.getParent() != null) {
			space = space.getParent();
			if (space.getName().isEmpty()) return builder.toString();
			builder.insert(0,".");
			builder.insert(0,space.getName());
		}
		return builder.toString();
	}

	private void writeFunctionBody(FunctionInfo function, PrintStream out) {
		String retType = getJavaTypeName(function.getReturnType().getType());
		out.print("\tpublic static " + retType + " " + getJavaFunctionName(function) + "(");
		for (LocalVariableInfo arg : function.getArgs()) {
			out.print(getJavaTypeName(arg.getTypeRef().getType()) + " " + arg.getName());
			if (arg != function.getArgs().get(function.getArgs().size()-1)) out.print(',');
		}
		out.println(") {");
		out.println("\t}");
	}

	private String getJavaFunctionName(FunctionInfo function) {
		if (function.getArgs().isEmpty()) return function.getName();
		StringBuilder builder = new StringBuilder(function.getName());
		for (LocalVariableInfo arg : function.getArgs()) {
			builder.append("$").append(arg.getTypeRef().getType().getName());
		}
		return builder.toString();
	}

	private String getJavaTypeName(TypeInfo type) {
		if (type instanceof TypeInfo.PrimitiveTypeInfo) {
			if (type.getName().startsWith("u")) return type.getName().substring(1);
			if (type.getName().equals("bool")) return "boolean";
			return type.getName();
		}
		if (type instanceof TypeArrayInfo) {
			return getJavaTypeName(((TypeArrayInfo) type).getElementType()) + "[]";
		}
		throw new UnsupportedOperationException("not implemented yet");
	}
}
