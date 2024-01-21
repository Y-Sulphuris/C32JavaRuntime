package c32.compiler.logical.tree.statement;

import c32.compiler.Location;
import c32.compiler.logical.tree.FunctionImplementationInfo;
import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ExpressionStatement implements Statement {
	private final FunctionImplementationInfo function;
	private final BlockStatement container;

	private final Expression expression;
	public Location getLocation() {
		return expression.getLocation();
	}
}
