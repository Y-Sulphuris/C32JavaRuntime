package c32.compiler.codegen.java;

import c32.compiler.logical.tree.expression.*;
import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.statement.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class JavaGenerator {

	public void generate(SpaceInfo space) {
		File dir = new File("out/java");
		dir.mkdir();
		writeNamespace(space);
	}

	private void writeNamespace(SpaceInfo space) {
		String className = "$package";
		String packageName = getJavaPackageName(space);
		File dir = new File("out/java/"+packageName.replace('.','/')+'/');
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

	private void writeExpression(Expression expr, PrintStream out) {
		if (expr instanceof NumericLiteralExpression) {
			out.print(((NumericLiteralExpression) expr).getNumber());
			switch (getJavaTypeName(expr.getReturnType())) {
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
		} else if (expr instanceof BooleanLiteralExpression) {
			out.print(((BooleanLiteralExpression) expr).isValue());
		} else if (expr instanceof VariableRefExpression) {
			out.print(((VariableRefExpression) expr).getVariable().getName());
		} else if (expr instanceof BinaryExpression) {
			out.print('(');
			writeExpression(((BinaryExpression) expr).getLhs(),out);
			out.print(((BinaryExpression) expr).getOperator());
			writeExpression(((BinaryExpression) expr).getRhs(), out);
			out.print(')');
		} else if (expr instanceof CallExpression) {
			String fname = getJavaFunctionName(((CallExpression) expr).getFunction());
			out.print(fname + "(");
			for (Expression arg : ((CallExpression) expr).getArgs()) {
				writeExpression(arg,out);
				if (arg != ((CallExpression) expr).getArgs().get(((CallExpression) expr).getArgs().size() - 1))out.print(", ");
			}
			out.print(')');
		} else if (expr instanceof StringLiteralExpression) {
			out.print("new char[]{");
			String text = ((StringLiteralExpression) expr).getString();
			for (int i = 0; i < text.length(); i++) {
				writeChar(String.valueOf(text.charAt(i)),out);
				if (i != text.length()-1) out.print(",");
			}
			out.print("}");
		} else if (expr instanceof CharLiteralExpression) {
			writeChar(String.valueOf(((CharLiteralExpression) expr).getCh()),out);
		} else if (expr instanceof UnaryPrefixExpression) {
			out.print(((UnaryPrefixExpression) expr).getOperator());
			writeExpression(((UnaryPrefixExpression) expr).getExpr(),out);
		} else if (expr instanceof AssignExpression) {
			writeExpression(((AssignExpression) expr).getLvalue(),out);
			BinaryOperator parent = ((AssignExpression) expr).getParentOperator();
			if (parent != null) out.print(parent.getOp());
			out.print("=");
			writeExpression(((AssignExpression) expr).getRvalue(),out);
		}
		else
			throw new UnsupportedOperationException(expr.getClass().getName());
	}
	private void writeChar(String ch, PrintStream out) {
		switch (ch) {
			case "\n":
				ch = "'\\n'";
				break;
			case "\t":
				ch = "'\\t'";
				break;
			default:
				if (!Character.isAlphabetic(ch.charAt(0)) && !Character.isDigit(ch.charAt(0))) {
					ch = "((char)" + ch.charAt(0) + ")";
				} else ch = "'" + ch + "'";
		}
		out.print(ch);
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
		if (function.is_extern()) return;
		String retType = getJavaTypeName(function.getReturnType());
		out.print("\tpublic static " + retType + " " + getJavaFunctionName(function) + "(");
		for (VariableInfo arg : function.getArgs()) {
			out.print(getJavaTypeName(arg.getTypeRef().getType()) + " " + arg.getName());
			if (arg != function.getArgs().get(function.getArgs().size()-1)) out.print(',');
		}
		if (function instanceof FunctionDeclarationInfo) {
			out.println(");");
		} else if (function instanceof FunctionImplementationInfo) {
			FunctionImplementationInfo func = (FunctionImplementationInfo) function;
			out.println(")");
			writeStatement(func.getImplementation(), out);
		}
	}

	private void writeStatement(Statement state, PrintStream out) {
		if (state instanceof BlockStatement) {
			out.println("\t{");
			for (Statement statement : ((BlockStatement) state).getStatements()) {
				writeStatement(statement,out);
			}
			out.println("\t}");
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
		} else if (state instanceof IfStatement) {
			out.print("if (");
			writeExpression(((IfStatement) state).getCondition(),out);
			out.print(") ");
			writeStatement(((IfStatement) state).getStatement(),out);
			if (((IfStatement) state).getElseStatement() != null) {
				out.print("\n else ");
				writeStatement(((IfStatement) state).getStatement(),out);
			}
		} else if (state instanceof WhileStatement) {
			if (((WhileStatement) state).getCondition() instanceof BooleanLiteralExpression && !((BooleanLiteralExpression) ((WhileStatement) state).getCondition()).isValue()) {
				//while (false)
			} else {
				out.print("while (");
				writeExpression(((WhileStatement) state).getCondition(),out);
				out.print(")");
				writeStatement(((WhileStatement) state).getStatement(),out);
			}
		} else if (state instanceof ReturnStatement) {
			out.print("return ");
			if (((ReturnStatement) state).getExpr() != null) writeExpression(((ReturnStatement) state).getExpr(),out);
			out.println(';');
		} else if (state instanceof BreakStatement) {
			out.println("break;");
		} else if (state instanceof ExpressionStatement) {
			if (((ExpressionStatement) state).getExpression() instanceof BinaryExpression) {
				return;//todo: это уберём, когда добавим перегрузку операторов
			}
			writeExpression(((ExpressionStatement) state).getExpression(),out);
			out.println(';');
		} else throw new UnsupportedOperationException(state.getClass().toString());
	}

	public static String getJavaFunctionName(FunctionInfo function) {
		String fname = getFname(function);
		if (function.is_extern()) return IntrinsicFunctionTable.get(function,fname);
		return fname;
	}
	private static String getFname(FunctionInfo function) {
		if (function.getArgs().isEmpty()) return function.getName();
		StringBuilder builder = new StringBuilder(function.getName());
		for (VariableInfo arg : function.getArgs()) {
			builder.append("$").append(arg.getTypeRef().getType().getName());
		}
		String base = builder.toString();
		StringBuilder fullPath = new StringBuilder(base);
		SpaceInfo space = function.getParent();
		while (space != null) {
			if (!space.getName().isEmpty()) fullPath.insert(0,'.');
			fullPath.insert(0,space.getName());
			space = space.getParent();
		}
		return fullPath.toString();
	}

	public static String getJavaTypeName(TypeInfo type) {
		if (type instanceof TypeInfo.PrimitiveTypeInfo) {
			if (type.getName().startsWith("u")) return type.getName().substring(1);
			if (type.getName().equals("bool")) return "boolean";
			return type.getName();
		}
		if (type instanceof TypeArrayInfo) {
			return getJavaTypeName(((TypeArrayInfo) type).getElementType().getType()) + "[]";
		}
		throw new UnsupportedOperationException(type.getCanonicalName() + "not implemented yet");
	}
}
