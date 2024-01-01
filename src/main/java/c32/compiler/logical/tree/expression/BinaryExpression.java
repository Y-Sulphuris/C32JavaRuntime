package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.BinaryOperator;
import c32.compiler.logical.tree.SymbolInfo;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import lombok.Getter;

import java.math.BigInteger;
import java.util.function.Consumer;


@Getter
public class BinaryExpression implements Expression {
	private final Expression lhs;
	private final BinaryOperator operator;
	private final Expression rhs;

	@Override
	public void forEachSubExpression(Consumer<Expression> act) {
		act.accept(lhs);
		act.accept(rhs);
	}

	public BinaryExpression(Location location, Expression lhs, String operator, Expression rhs) {
		this.lhs = lhs;
		this.operator = BinaryOperator.findOperator(location, lhs,operator,rhs);
		this.rhs = rhs;
	}

	public BinaryExpression(Location location, Expression lhs, String operator, Expression rhs, TypeInfo returnType) {
		this.lhs = lhs;
		this.operator = BinaryOperator.findOperator(location, lhs,operator,rhs);
		this.rhs = rhs;
		TypeInfo ret = this.operator.getReturnType();
		if (returnType != null && !ret.canBeImplicitCastTo(returnType)) {
			throw new CompilerException(location,"cannot implicit cast '" + ret.getCanonicalName() + "' to '" + returnType.getCanonicalName() + "'");
		}

		addUsageIfVariable(lhs);
		addUsageIfVariable(rhs);
	}


	@Override
	public TypeInfo getReturnType() {
		return operator.getReturnType();
	}
	
	@Override
	public Expression calculate() {
		if (lhs instanceof NumericLiteralExpression && rhs instanceof NumericLiteralExpression) {
			BigInteger l = ((NumericLiteralExpression) lhs).getNumber();
			BigInteger r = ((NumericLiteralExpression) rhs).getNumber();
			BigInteger result = null;
			switch (operator.getOp()) {
				case "+": {
					result = l.add(r);
					break;
				} case "-": {
					result = l.subtract(r);
					break;
				} case "/": {
					result = l.divide(r);
					break;
				} case "*": {
					result = l.multiply(r);
					break;
				} case "%": {
					result = l.mod(r);
					break;
				}
			}
			if (result != null)
				return new NumericLiteralExpression(result,operator.getReturnType());
		}
		return this;
	}
}

//TODO: ВЕСЬ ЭТОТ КЛАСС НАДО ПЕРЕПИСАТЬ, ОН КРИВОЙ