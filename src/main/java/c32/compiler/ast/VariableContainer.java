package c32.compiler.ast;

import c32.compiler.ast.statement.VariableDeclarationStatementTree;

import java.util.*;

public interface VariableContainer {
	Set<String> availableVariableModifiers();

	VariableDeclarationStatementTree getVariable(String name);

}
