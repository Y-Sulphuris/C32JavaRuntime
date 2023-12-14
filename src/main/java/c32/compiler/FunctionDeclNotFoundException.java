package c32.compiler;

import c32.compiler.ast.expr.ExprTree;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class FunctionDeclNotFoundException extends CompilerException {
	public FunctionDeclNotFoundException(String name, ArrayList<ExprTree> args) {
		super(name + ": " + args.stream().map(ExprTree::getRetType).collect(Collectors.toList()));
	}
}
