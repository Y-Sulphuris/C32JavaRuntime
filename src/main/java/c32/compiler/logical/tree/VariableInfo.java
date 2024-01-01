package c32.compiler.logical.tree;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.logical.tree.expression.IndexExpression;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;



public class VariableInfo extends AbstractSymbolInfo {
	@Getter
	private final String name;
	@Getter
	private final TypeRefInfo typeRef;
	@Getter
	@Nullable private final Expression initializer;

	public VariableInfo(Location location, String name, TypeRefInfo typeRef, @Nullable Expression initializer, boolean _static, Boolean _register) {
		this.name = name;
		this.typeRef = typeRef;
		this.initializer = initializer;
		this._static = _static;
		this._register = _register;
		if (initializer != null && !initializer.checkImplicitCastTo_mutable(typeRef.getType())) {
			throw new CompilerException(location,"cannot cast " + initializer.getReturnType() + " to " + typeRef.getType());
		}
	}

	@Override
	public boolean isAccessibleFrom(SpaceInfo space) {
		return true;
	}

	@Override
	public SpaceInfo getParent() {
		return null;
	}

	@Getter
	private final boolean _static;

	/*
	true - force register (has register modifier)
	false - unknown, but can be register (has no register modifier)
	null - force no register (register modifier are not allowed, or register[false] is used)
	 */
	private final Boolean _register;

	public boolean is_register() {
		if (_register == null) return false;
		return _register;
	}

	public boolean isRegister() {
		if (_register == null) return false;
		if (_register) return true;
		if (typeRef.getType() instanceof TypeArrayInfo && !((TypeArrayInfo) typeRef.getType()).isStaticArray())
			return false;
		for (Weak<Expression> usage : getUsages()) {
			Expression expr = usage.get();
			if (expr instanceof IndexExpression) {
				if (((IndexExpression) expr).getArgs().get(0).asCompileTimeLiteralExpression() == null) {
					return false;
				}
			}
		}
		return true;
	}

	public Weak<VariableInfo> weakReference() {
		return new Weak<>(this);
	}
}
