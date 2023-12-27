package c32.compiler.logical;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.parser.ast.expr.CallExprTree;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionNotFoundException extends CompilerException {
	public FunctionNotFoundException(CallExprTree call, List<Expression> args) {
		super(call.getLocation(),"cannot found function '" + call.getReference().getIdentifier() +
				args.stream().map(e -> e.getReturnType().getCanonicalName()).collect(Collectors.toList()));
	}
}
