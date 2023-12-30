package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.SymbolInfo;
import c32.compiler.logical.tree.TypeArrayInfo;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.VariableInfo;
import lombok.Getter;

import java.math.BigInteger;
import java.util.List;

@Getter
public class IndexExpression implements Expression {
	private final Expression array;
	private final List<Expression> args;

	public VariableInfo arrayIsRegister() {
		if (array.getReturnType() instanceof TypeArrayInfo) {
			if (array instanceof VariableRefExpression) {
				if (((VariableRefExpression) array).getVariable().isRegister())
					return ((VariableRefExpression) array).getVariable();
			}
		}
		return null;
	}

	public IndexExpression(Location location, Expression array, List<Expression> args) {
		if (!(array.getReturnType() instanceof TypeArrayInfo)) {
			throw new CompilerException(location,"array expected");
		}
		TypeArrayInfo arrayType = (TypeArrayInfo) array.getReturnType();
		if (args.size() != 1 || !args.set(0,args.get(0).calculate()).getReturnType().canBeImplicitCastTo(TypeInfo.PrimitiveTypeInfo.INT)) {
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
				throw new CompilerException(location,"array index " + index + " out of bounds");
			}
		} else {
			if (array instanceof VariableRefExpression && ((VariableRefExpression) array).getVariable().is_register()) {
				throw new CompilerException(location,"register array length must be known in compile-time");
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
		throw new UnsupportedOperationException();
	}
}
