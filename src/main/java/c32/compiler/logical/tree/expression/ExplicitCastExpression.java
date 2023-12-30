package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.TypeInfo;
import lombok.Getter;

@Getter
public class ExplicitCastExpression implements Expression {
	private final TypeInfo targetType;
	private final Expression expression;

	public ExplicitCastExpression(Location location, TypeInfo targetType, Expression expression) {
		this.targetType = targetType;
		this.expression = expression;
		if (!expression.getReturnType().canBeExplicitCastTo(targetType)) {
			throw new CompilerException(location,"cannot cast type '" + expression.getReturnType().getCanonicalName() + "' to '" + targetType.getCanonicalName() + "'");
		}

		if (expression instanceof VariableRefExpression) {
			((VariableRefExpression) expression).getVariable().addUsage(this);
		}
	}

	@Override
	public TypeInfo getReturnType() {
		return targetType;
	}
}
