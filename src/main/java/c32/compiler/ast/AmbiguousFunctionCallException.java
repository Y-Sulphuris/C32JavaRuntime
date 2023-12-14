package c32.compiler.ast;

import c32.compiler.CompilerException;
import c32.compiler.ast.expr.ExprTree;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class AmbiguousFunctionCallException extends CompilerException {
	public AmbiguousFunctionCallException(String name, ArrayList<ExprTree> args) {
		super(name + ": " + args.stream().map(ExprTree::getRetType).collect(Collectors.toList()));
	}
}
