package c32.compiler.ast;

import c32.compiler.ast.expr.ExprTree;

import java.util.ArrayList;
import java.util.Set;

public interface FunctionContainer {

	Set<String> availableFunctionModifiers();

	FunctionDeclarationTree getFunction(String name, ArrayList<ExprTree> args);
}
