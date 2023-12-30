package c32.compiler.logical.tree.expression;

public interface LiteralExpression extends Expression {
	@Override
	default Expression asCompileTimeLiteralExpression() {
		return this;
	}
}
