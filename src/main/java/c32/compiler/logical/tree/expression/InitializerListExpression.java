package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.TypeArrayInfo;
import c32.compiler.logical.tree.TypeInfo;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

@Getter
public class InitializerListExpression implements Expression {

	@Nullable private TypeInfo returnType;
	private final List<Expression> args;

	@Override
	public void forEachSubExpression(Consumer<Expression> act) {
		args.forEach(act);
	}

	public InitializerListExpression(Location location, @Nullable TypeInfo returnType, List<Expression> args) {
		this.returnType = returnType;
		this.args = args;
		String error = checkTypeCompatibility(returnType);
		if (error != null) {
			throw new CompilerException(location,error);
		}
	}

	private String checkTypeCompatibility(TypeInfo returnType) {
		if (returnType != null) {
			if (returnType instanceof TypeArrayInfo) {
				if (((TypeArrayInfo) returnType).isStaticArray()) {
					if (((TypeArrayInfo) returnType).getStaticLength() != args.size())
						return  "incorrect arguments amount";
				}
				for (Expression arg : args) {
					if (!arg.checkImplicitCastTo_mutable(((TypeArrayInfo) returnType).getElementType().getType()))
						return "cannot implicit cast type " + arg.getReturnType() + " to " + ((TypeArrayInfo) returnType).getElementType().getType();
				}
			}
		}
		return null;
	}

	@Override
	public boolean checkImplicitCastTo_mutable(TypeInfo type) {
		if (returnType != null) {
			return Expression.super.checkImplicitCastTo_mutable(type);
		} else {
			String error = checkTypeCompatibility(type);
			if (error == null) {
				this.returnType = type;
				return true;
			}
			return false;
		}
	}
}
