package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public class IndexExpression implements Expression {
	private final Expression array;
	private final List<Expression> args;
	private final Location location;


	@Override
	public void forEachSubExpression(Consumer<Expression> act) {
		args.forEach(act);
		act.accept(array);
	}

	@Override
	public boolean isAssignable() {
		return array.isAssignable();
	}

	public IndexExpression(Location location, Expression array, List<Expression> args) {
		this.location = location;
		if (array.getReturnType() instanceof TypePointerInfo) {

		} else {
			if (!(array.getReturnType() instanceof TypeArrayInfo)) {
				throw new CompilerException(location,"array expected");
			}
			TypeArrayInfo arrayType = (TypeArrayInfo) array.getReturnType();
			if (args.size() != 1 || !args.set(0,args.get(0).calculate()).getReturnType().canBeImplicitlyCastTo(TypeInfo.PrimitiveTypeInfo.INT)) {
				throw new CompilerException(location,"index expected");
			}
			Expression argument = args.get(0);

			Expression constexpr = argument.asCompileTimeLiteralExpression();
			if (constexpr instanceof NumericLiteralExpression) {
				BigInteger exprValue = ((NumericLiteralExpression) constexpr).getNumber();
				if (exprValue.bitLength() > 31)
					throw new CompilerException(location,"array index overflow");
				int index = exprValue.intValue();
				if (index < 0 || (arrayType.isStaticArray() && index >= arrayType.getStaticLength())) {
					throw new CompilerException(location,"array index " + index + " out of bounds (length = " + arrayType.getStaticLength() + ")");
				}
			} else {
				if (array instanceof VariableRefExpression && ((VariableRefExpression) array).getVariable().is_register()) {
					throw new CompilerException(location,"register array length must be known at compile-time");
				}
			}
		}


		this.array = array;
		this.args = args;

		addUsageIfVariable(array);
	}

	@Override
	public TypeInfo getReturnType() {
		if (array.getReturnType() instanceof TypeArrayInfo) {
			return ((TypeArrayInfo) array.getReturnType()).getElementType().getType();
		}
		if (array.getReturnType() instanceof TypePointerInfo) {
			return ((TypePointerInfo) array.getReturnType()).getTargetType().getType();
		}
		throw new UnsupportedOperationException();
	}
}
