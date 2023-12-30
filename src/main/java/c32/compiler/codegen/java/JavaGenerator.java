package c32.compiler.codegen.java;

import c32.compiler.Compiler;
import c32.compiler.codegen.Generator;
import c32.compiler.codegen.LinkerException;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.expression.*;
import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.statement.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;

public class JavaGenerator implements Generator {

	@Override
	public void generate(NamespaceInfo space) {
		File dir = new File("out/java");
		dir.mkdir();
		writeNamespace(space);
	}

	private void writeNamespace(NamespaceInfo space) {
		String className = "$package";
		String packageName = getJavaPackageName(space);
		File dir = new File("out/java/"+packageName.replace('.','/')+'/');
		dir.mkdirs();
		File file = new File(dir,className+".java");
		for (NamespaceInfo namespace : space.getNamespaces()) {
			writeNamespace(namespace);
		}
		try {
			file.createNewFile();
			PrintStream out = new PrintStream(file);
			if (!packageName.isEmpty()) out.println("package " + packageName + ";");
			out.println("public final class " + className + " {");
			out.println("\tprivate " + className + "() throws java.lang.InstantiationException { throw new java.lang.InstantiationException(); }");
			for (TypeStructInfo struct : space.getStructs()) {
				writeStruct(struct,out);
			}
			for (FunctionInfo function : space.getFunctions()) {
				writeFunction(function,true,out);
				writeFunctionNamespace(function,true,out);
			}
			for (FieldInfo field : space.getFields()) {
				writeField(field,true,out);
			}
			if (packageName.isEmpty()) {
				String main = Compiler.config.getMainFunctionName();
				SpaceInfo mainSpace = space;
				while (main.contains(".")) {
					mainSpace = mainSpace.getNamespace(main.substring(0,main.indexOf(".")));
					main = main.substring(main.indexOf(".")+1);
				}
				FunctionInfo mainFunc = null;
				//System.out.println(mainSpace.getCanonicalName());
				for (FunctionInfo function : mainSpace.getFunctions()) {
					//System.out.println(function.getCanonicalName());
					if (!function.getName().equals(main) || !function.getArgs().isEmpty()) continue;
					mainFunc = function;
				}
				if (mainFunc == null)
					throw new LinkerException("no main function found");

				out.println("public static void main(String... args) {");
				out.print(getJavaFunctionName(mainFunc) + "();");
				out.println("}");
			}
			out.println('}');
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeStruct(TypeStructInfo struct, PrintStream out) {
		writeStruct(null,struct,out);
	}
	private void writeStruct(String prefix, TypeStructInfo struct, PrintStream out) {
		String name = toLocalName(getJavaTypeName(struct));
		if (prefix != null) name = prefix + "$$$$" + name;
		out.println("public static final class " + name + "{");

		//write static content
		for (NamespaceInfo space : struct.getStaticSpace().getNamespaces()) {
			writeLocalNamespace(null,space,true,out);
		}
		for (TypeStructInfo str : struct.getStaticSpace().getStructs()) {
			writeStruct(str,out);
		}
		for (FunctionInfo function : struct.getStaticSpace().getFunctions()) {
			writeFunction(function,true,out);
			writeFunctionNamespace(function,true,out);
		}
		for (FieldInfo field : struct.getStaticSpace().getFields()) {
			writeField(field,true,out);
		}

		//write an instance content
		for (NamespaceInfo space : struct.getNamespaces()) {
			writeLocalNamespace(null,space,false,out);
		}
		for (TypeStructInfo str : struct.getStructs()) {
			writeStruct(str,out);
		}
		for (FunctionInfo function : struct.getFunctions()) {
			writeFunction(function,false,out);
			writeFunctionNamespace(function,false,out);
		}
		for (FieldInfo field : struct.getFields()) {
			writeField(field,false,out);
		}
		out.println("}");
	}

	private void writeLocalNamespace(String prefix, NamespaceInfo namespace,boolean _static, PrintStream out) {
		String thisPrefixName = namespace.getName();
		if (prefix != null) thisPrefixName = prefix + "$$$$" + thisPrefixName;
		for (NamespaceInfo space : namespace.getNamespaces()) {
			writeLocalNamespace(thisPrefixName,space,_static,out);
		}
		for (TypeStructInfo str : namespace.getStructs()) {
			writeStruct(thisPrefixName,str,out);
		}
		for (FunctionInfo function : namespace.getFunctions()) {
			writeFunction(thisPrefixName,function,_static,out);
			writeFunctionNamespace(thisPrefixName,function,_static,out);
		}
		for (FieldInfo field : namespace.getFields()) {
			writeField(thisPrefixName,field,_static,out);
		}
	}

	private void writeField(FieldInfo field, boolean _static, PrintStream out) {
		writeField(null,field, _static,out);
	}
	private void writeField(String prefix, FieldInfo field, boolean _static, PrintStream out) {
		out.print("\tpublic ");
		if (_static) out.print("static ");
		if (field.getTypeRef().is_const()) out.print("final ");
		out.print(getJavaTypeName(field.getTypeRef().getType()) + " ");
		if (prefix != null) out.print(prefix + "$$$$");
		out.print(field.getName());
		Expression init = field.getInitializer();
		if (init == null) init = field.getTypeRef().getType().getDefaultValue();
		out.print(" = ");
		writeExpression(init, out);
		out.println(';');
		out.println();
	}

	private void writeExpression(Expression expr, PrintStream out) {
		if (expr instanceof NumericLiteralExpression) {
			out.print("((" + getJavaTypeName(expr.getReturnType()) + ")");
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
			out.print(")");
		} else if (expr instanceof BooleanLiteralExpression) {
			out.print(((BooleanLiteralExpression) expr).isValue());
		} else if (expr instanceof VariableRefExpression) {
			out.print("("+((VariableRefExpression) expr).getVariable().getName()+")");
		} else if (expr instanceof BinaryExpression) {
			out.print('(');
			writeExpression(((BinaryExpression) expr).getLhs(),out);
			out.print(((BinaryExpression) expr).getOperator().getOp());
			writeExpression(((BinaryExpression) expr).getRhs(), out);
			out.print(')');
		} else if (expr instanceof CallExpression) {
			String fname = getJavaFunctionName(((CallExpression) expr).getFunction());
			out.print(fname + '(');
			for (Expression arg : ((CallExpression) expr).getArgs()) {
				writeExpression(arg,out);
				if (arg != ((CallExpression) expr).getArgs().get(((CallExpression) expr).getArgs().size() - 1))
					out.print(", ");
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
			out.print("((" + getJavaTypeName(expr.getReturnType()) + ")");
			out.print(((UnaryPrefixExpression) expr).getOperator());
			writeExpression(((UnaryPrefixExpression) expr).getExpr(),out);
			out.print(")");
		} else if (expr instanceof AssignExpression) {
			writeExpression(((AssignExpression) expr).getLvalue(),out);
			BinaryOperator parent = ((AssignExpression) expr).getParentOperator();
			if (parent != null) out.print(parent.getOp());
			out.print("=");
			writeExpression(((AssignExpression) expr).getRvalue(),out);
		} else if (expr instanceof ExplicitCastExpression) {
			out.print("(("+getJavaTypeName(((ExplicitCastExpression) expr).getTargetType()) + ")");
			writeExpression(((ExplicitCastExpression) expr).getExpression(),out);
			out.print(")");
		} else if (expr instanceof IndexExpression) {
			VariableInfo registerArray = ((IndexExpression) expr).arrayIsRegister();
			if (registerArray != null) {
				out.print(registerArray.getName() + "$");
				if (((IndexExpression) expr).getArgs().get(0) instanceof NumericLiteralExpression) {
					BigInteger index = ((NumericLiteralExpression) ((IndexExpression) expr).getArgs().get(0)).getNumber();
					out.print(index);
				} else throw new UnsupportedOperationException("what");
			} else {
				writeExpression(((IndexExpression) expr).getArray(),out);
				out.print("[");
				writeExpression(((IndexExpression) expr).getArgs().get(0),out);
				out.print("]");
			}
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
					ch = "((char)" + (int)ch.charAt(0) + ")";
				} else ch = "'" + ch + "'";
		}
		out.print(ch);
	}

	private void writeFunctionNamespace(FunctionInfo function, boolean _static, PrintStream out) {
		writeFunctionNamespace(null,function,_static,out);
	}
	private void writeFunctionNamespace(String prefix, FunctionInfo function, boolean _static, PrintStream out) {
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

	private void writeFunction(FunctionInfo function, boolean _static, PrintStream out) {
		writeFunction(null,function,_static,out);
	}
	private void writeFunction(String prefix, FunctionInfo function, boolean _static, PrintStream out) {
		if (function.is_extern()) return;
		String retType = getJavaTypeName(function.getReturnType());
		String name = toLocalName(getJavaFunctionName(function));
		if (prefix != null) name = prefix + "$$$$" + name;
		out.print("\tpublic ");
		if (function.getParent() instanceof NamespaceInfo) out.print("static ");
		out.print(retType + " " + name + "(");
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

	private void writeType(TypeInfo type, PrintStream out) {
		writeType(type,-1,out);
	}
	private void writeType(TypeInfo type, int writeArrayLength, PrintStream out) {
		if (type instanceof TypeInfo.PrimitiveTypeInfo) {
			out.print(getJavaTypeName(type));
		} else if (type instanceof TypeArrayInfo) {
			writeType(((TypeArrayInfo) type).getElementType().getType(), out);
			if (writeArrayLength != -1) out.print("["+writeArrayLength+"]");
			else out.print("[]");
		}
	}

	private void writeVariable(VariableInfo variable, PrintStream out) {
		if (variable.getTypeRef().getType() instanceof TypeInfo.PrimitiveTypeInfo) {
			out.print("\t\t");
			if (variable.getTypeRef().is_const()) out.print("final ");
			writeType(variable.getTypeRef().getType(),out);
			out.print(" " + variable.getName());
			if (variable.getInitializer() != null) {
				out.print(" = ");
				writeExpression(variable.getInitializer(),out);
			}
			out.println(';');
		} else if (variable.getTypeRef().getType() instanceof TypeArrayInfo) {
			if (variable.isRegister()) {
				TypeArrayInfo array = ((TypeArrayInfo) variable.getTypeRef().getType());
				for (int i = 0; i < array.getStaticLength(); i++) {
					writeVariable(new VariableInfo(variable.getName()+"$"+i,array.getElementType(),array.getElementType().getType().getDefaultValue(), variable.is_static(),true),out);
				}
			} else {
				if (variable.getTypeRef().is_const()) out.print("final ");
				writeType(variable.getTypeRef().getType(),out);
				out.print(" " + variable.getName());
				int len = ((TypeArrayInfo) variable.getTypeRef().getType()).getStaticLength();
				if (variable.getInitializer() != null) {
					out.print(" = ");
					writeExpression(variable.getInitializer(),out);
				} else if (len != -1){
					out.print(" = ");
					out.print("new ");
					writeType(variable.getTypeRef().getType(),len,out);
					out.println(";");
				}
				else
					out.println(" = null;");
			}
		}
		else
			throw new UnsupportedOperationException(variable.getTypeRef().getType().getClass().getName());
	}

	private void writeStatement(Statement state, PrintStream out) {
		if (state instanceof BlockStatement) {
			out.println("\t{");
			for (Statement statement : ((BlockStatement) state).getStatements()) {
				writeStatement(statement,out);
			}
			out.println("\t}");
		} else if (state instanceof VariableDeclarationStatement) {
			for (VariableInfo variable : ((VariableDeclarationStatement) state).getVariables()) {
				writeVariable(variable, out);
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

	public static String toLocalName(String name) {
		int index = name.lastIndexOf('.')+1;
		if (index == 0) return name;
		return name.substring(index);
	}
	public static String getJavaFunctionName(FunctionInfo function) {
		String fname = getFname(function);
		try {
			if (function.is_extern()) return IntrinsicFunctionTable.get(function,fname);
		} catch (CompilerException e) {
			//e.setLocation(function.getLocation());
			throw e;
		}
		StringBuilder editor = new StringBuilder(fname);
		editor.insert(fname.lastIndexOf(".")+1,"$package.");
		fname = editor.toString();
		return fname;
	}
	private static String getFname(FunctionInfo function) {
		if (function.getArgs().isEmpty()) return function.getCanonicalName();
		StringBuilder builder = new StringBuilder(function.getCanonicalName());
		for (VariableInfo arg : function.getArgs()) {
			builder.append("$").append(arg.getTypeRef().getType().getFullName());
		}
		return builder.toString();
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
		if (type instanceof TypeStructInfo) {
			return type.getName();
		}
		throw new UnsupportedOperationException(type.getCanonicalName() + " not implemented yet");
	}


	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == this.getClass();
	}
}
