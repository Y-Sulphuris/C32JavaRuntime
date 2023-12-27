package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import lombok.Getter;

import java.math.BigInteger;


//todo: re-code
@Getter
public class BinaryExpression implements Expression {
	private final Expression lhs;
	private final String operator;//todo: re-code
	private final Expression rhs;
	private final TypeInfo returnType;
//todo: re-code

	public BinaryExpression(Expression lhs, String operator, Expression rhs) {
		this.lhs = lhs;
		this.operator = operator;
		this.rhs = rhs;
//todo: re-code
		TypeInfo lt = lhs.getReturnType();
		TypeInfo rt = rhs.getReturnType();//todo: re-code
		if (rt.sizeof() > lt.sizeof())
			this.returnType = rt;
		else
			this.returnType = lt;
	}//todo: re-code

	public BinaryExpression(Location location, Expression lhs, String operator, Expression rhs, TypeInfo returnType) {//todo: re-code
		this.lhs = lhs;
		this.operator = operator;
		this.rhs = rhs;
		TypeInfo ret = getOperatorReturnType(operator);
		if (ret != null) {//todo: re-code
			if (!ret.canBeImplicitCastTo(returnType)) {
				throw new CompilerException(location,"cannot implicit cast '" + ret.getCanonicalName() + "' to '" + returnType + "'");
			}
		}
		if (returnType == null) {//todo
			TypeInfo lt = lhs.getReturnType();
			TypeInfo rt = rhs.getReturnType();//todo: re-code
			if (rt.sizeof() > lt.sizeof())
				returnType = rt;
			else
				returnType = lt;
		}
		this.returnType = returnType;
	}
	//todo: re-code
	private static TypeInfo getOperatorReturnType(String operator) {
		switch (operator) {//todo: re-code
			case "&&":
			case "||":
			case "==":
			case "!=":
			case ">":
			case "<":
			case ">="://todo: re-code
			case "<=":
				return TypeInfo.PrimitiveTypeInfo.BOOL;
			default://todo: re-code
				return null;
		}
	}

	@Override
	public TypeInfo getReturnType() {
		return returnType;
	}//todo: re-code
	//todo: re-code
	@Override
	public Expression calculate() {
		if (lhs instanceof NumericLiteralExpression && rhs instanceof NumericLiteralExpression) {
			BigInteger l = ((NumericLiteralExpression) lhs).getNumber();
			BigInteger r = ((NumericLiteralExpression) rhs).getNumber();
			BigInteger result = null;
			switch (operator) {
				case "+": {
					result = l.add(r);
					break;
				} case "-": {//todo: re-code
					result = l.subtract(r);
					break;//todo: re-code
				} case "/": {
					result = l.divide(r);
					break;
				} case "*": {
					result = l.multiply(r);
					break;
				} case "%": {
					result = l.mod(r);
					break;//todo: re-code
				}
			}
			if (result != null)
				return new NumericLiteralExpression(result,returnType);
		}
		return this;
	}
}//todo: re-code

//TODO: ВЕСЬ ЭТОТ КЛАСС НАДО ПЕРЕПИСАТЬ, ОН КРИВОЙ