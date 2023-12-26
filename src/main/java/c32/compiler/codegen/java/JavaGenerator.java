package c32.compiler.codegen.java;

import c32.compiler.logical.tree.expression.BooleanLiteralExpression;
import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.logical.tree.expression.NumericLiteralExpression;
import c32.compiler.logical.tree.statement.BlockStatement;
import c32.compiler.logical.tree.statement.Statement;
import c32.compiler.logical.tree.statement.VariableDeclarationStatement;

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
				writeFunction(function,out);
				writeFunctionNamespace(function,out);
			}
			for (FieldInfo field : space.getFields()) {
				writeField(field,out);
			}
			out.println('}');
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeField(FieldInfo field, PrintStream out) {
		out.print("\tpublic static ");
		if (field.getTypeRef().is_const()) out.print("final ");
		out.print(getJavaTypeName(field.getTypeRef().getType()) + " " + field.getName() + ";");
		Expression init = field.getInitializer();
		if (init == null) init = field.getTypeRef().getType().getDefaultValue();
		out.print("\n\tstatic {\n\t\t");
		out.print(field.getName() + " = ");
		writeExpression(init, out);
		out.println(';');
		out.println("\t}");
		out.println();
	}

	private void writeExpression(Expression init, PrintStream out) {
		if (init instanceof NumericLiteralExpression) {
			out.print(((NumericLiteralExpression) init).getNumber());
			switch (getJavaTypeName(init.getReturnType().getType())) {
				case "long":
					out.print("L");
					break;
				case "float":
					out.print("f");
					break;
				case "double":
					out.print("d");
					break;
			}
		} else if (init instanceof BooleanLiteralExpression) {
			out.print(((BooleanLiteralExpression) init).isValue());
		} else throw new UnsupportedOperationException();
	}

	private void writeFunctionNamespace(FunctionInfo function, PrintStream out) {
		//todo
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

	private void writeFunction(FunctionInfo function, PrintStream out) {
		String retType = getJavaTypeName(function.getReturnType().getType());
		out.print("\tpublic static " + retType + " " + getJavaFunctionName(function) + "(");
		for (VariableInfo arg : function.getArgs()) {
			out.print(getJavaTypeName(arg.getTypeRef().getType()) + " " + arg.getName());
			if (arg != function.getArgs().get(function.getArgs().size()-1)) out.print(',');
		}
		if (function instanceof FunctionDeclarationInfo) {
			out.println(");");
		} else if (function instanceof FunctionImplementationInfo) {
			FunctionImplementationInfo func = (FunctionImplementationInfo) function;
			out.println(") {");
			writeStatement(func.getImplementation(), out);
			out.println("\t}");
		}
	}

	private void writeStatement(Statement state, PrintStream out) {
		if (state instanceof BlockStatement) {
			for (Statement statement : ((BlockStatement) state).getStatements()) {
				writeStatement(statement,out);
			}
		} else if (state instanceof VariableDeclarationStatement) {
			for (VariableInfo variable : ((VariableDeclarationStatement) state).getVariable()) {
				out.print("\t\t");
				if (variable.getTypeRef().is_const()) out.print("final ");
				out.print(getJavaTypeName(variable.getTypeRef().getType()) + " " + variable.getName());
				if (variable.getInitializer() != null) {
					out.print(" = ");
					writeExpression(variable.getInitializer(),out);
				}
				out.println(';');
			}
		} else throw new UnsupportedOperationException();
	}

	private String getJavaFunctionName(FunctionInfo function) {
		if (function.getArgs().isEmpty()) return function.getName();
		StringBuilder builder = new StringBuilder(function.getName());
		for (VariableInfo arg : function.getArgs()) {
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
