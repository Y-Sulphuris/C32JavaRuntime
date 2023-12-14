package c32.compiler.ast;

import c32.compiler.Compiler;
import c32.compiler.ast.statement.VariableDeclarationStatementTree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FunctionImplTree implements Tree, VariableContainer {
	private final List<VariableDeclarationStatementTree> variables = new ArrayList<>();

	final List<StatementTree> statements = new ArrayList<>();

	protected FunctionImplTree() {

	}


	@Override
	public Set<String> availableVariableModifiers() {
		return availableVariableMod;
	}
	public static final HashSet<String> availableVariableMod = new HashSet<>();
	static {
		availableVariableMod.add(Compiler.CONST);
	}

	@Override
	public VariableDeclarationStatementTree getVariable(String name) {
		for (VariableDeclarationStatementTree var : variables) {
			if (var.getVarName().equals(name)) return var;
		}
		throw new VariableDeclNotFoundException(this,name);
	}

	public VariableDeclarationStatementTree declareVariable(VariableDeclarationStatementTree var) {
		variables.add(var);
		return var;
	}

}
