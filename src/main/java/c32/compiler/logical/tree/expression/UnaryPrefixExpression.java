package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.IllegalOperatorException;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypePointerInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Getter
public class UnaryPrefixExpression implements Expression {
    private final Expression expr;
    private final UnaryPrefixOperator operator;
	private final Location location;

	public UnaryPrefixExpression(Location location, boolean _const, Expression expr, String operator) {
		this.location = location;
		this.expr = expr;
		if (operator.equals("&")) {
			if (!(expr instanceof VariableRefExpression) || ((VariableRefExpression) expr).getVariable().is_register())
				throw new CompilerException(location, "cannot take address from given expression");
			((VariableRefExpression) expr).getVariable().setNotRegister();
		}
		this.operator = UnaryPrefixOperator.findOperator(location, new TypeRefInfo(_const,false,expr.getReturnType()), operator);

	}

	@Override
	public void forEachSubExpression(Consumer<Expression> act) {
		act.accept(expr);
	}

	@Override
    public TypeInfo getReturnType() {
        return operator.getReturnType();
    }

	@Override
	public boolean isAssignable() {
		return operator.getOp().equals("*");
	}

	@Override
	public Expression asCompileTimeLiteralExpression() {
		if (operator.getOp().equals("-")) {
			Expression t = expr.asCompileTimeLiteralExpression();
			if (t instanceof NumericLiteralExpression) {
				return new NumericLiteralExpression(((NumericLiteralExpression) t).getNumber().negate(),t.getReturnType(),t.getLocation());
			}
		}
		return Expression.super.asCompileTimeLiteralExpression();
	}
}

