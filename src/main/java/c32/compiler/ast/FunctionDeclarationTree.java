package c32.compiler.ast;

import c32.compiler.Compiler;
import c32.compiler.ast.expr.ExprTree;
import c32.compiler.ast.type.TypeTree;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FunctionDeclarationTree implements Tree, VariableContainer {
	private final String functionName;
	public String getFunctionName() {
		return functionName;
	}

	private final VariableDeclarationTree[] args;
	public VariableDeclarationTree[] getArgs() {
		return args;
	}

	private final TypeTree returnType;
	public TypeTree getReturnType() {
		return returnType;
	}

	private FunctionImplTree impl = null;

	public FunctionDeclarationTree setImpl(FunctionImplTree impl) {
		this.impl = impl;
		return this;
	}

	public FunctionImplTree getImpl() {
		return impl;
	}

	public String getJavaFunctionName() {
		if (getArgs().length == 0) return getFunctionName();
		StringBuilder builder = new StringBuilder(getFunctionName());
		for (int i = 0; i < getArgs().length; i++) {
			builder.append("_").append(getArgs()[i].getType().getName());
		}
		return builder.toString();
	}

	public MethodSpec brewJava() {
		if (modifiers.is_extern()) return null;
		MethodSpec.Builder method = MethodSpec.methodBuilder(getJavaFunctionName());
		method.returns(getReturnType().getJavaType());
		method.addModifiers(Modifier.STATIC,Modifier.PUBLIC);
		if (getArgs().length == 0 && getFunctionName().equals("main")) {
			method.addParameter(ParameterSpec.builder(String[].class,"$args",Modifier.FINAL).build());
			method.varargs(true);
		} else for(VariableDeclarationTree arg : getArgs()) {
			for (ParameterSpec parameterSpec : arg.brewJavaAsArgument()) method.addParameter(parameterSpec);
		}

		for (StatementTree statement : impl.statements) {
			for (CodeBlock codeBlock : statement.brewJava()) {
				method.addStatement(codeBlock);
			}
		}

		return method.build();
	}


	public TypeTree getType(CompilationUnitTree unit, String text) {
		return unit.getType(text);
	}

	public FunctionDeclarationTree getFunction(CompilationUnitTree unit, String name, ArrayList<ExprTree> args) {
		return unit.getFunction(name,args);
	}

	@Override
	public Set<String> availableVariableModifiers() {
		return availableVariableMod;
	}
	public static final Set<String> availableVariableMod = new HashSet<>();
	static {
		availableVariableMod.add(Compiler.CONST);
	}

	public VariableDeclarationTree getVariable(String name) {
		for (VariableDeclarationTree var : getArgs()) {
			if (var.getVarName().equals(name)) return var;
		}
		return impl.getVariable(name);
	}




	private final Modifiers modifiers;
	public Modifiers getModifiers() {
		return modifiers;
	}

	public FunctionDeclarationTree(String functionName, VariableDeclarationTree[] args, TypeTree returnType, Modifiers modifiers) {
		this.functionName = functionName;
		this.args = args;
		this.returnType = returnType;
		this.modifiers = modifiers;
	}



	@Override
	public String toString() {
		return getReturnType() + " " + getFunctionName() + ": " + Arrays.stream(getArgs()).map(exprTree -> exprTree.getType().getName()).collect(Collectors.toList());
	}
}
