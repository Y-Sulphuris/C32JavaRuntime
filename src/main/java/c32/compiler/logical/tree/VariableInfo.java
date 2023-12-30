package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.logical.tree.expression.IndexExpression;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

@Getter
public class VariableInfo extends AbstractSymbolInfo {
	private final String name;
	private final TypeRefInfo typeRef;
	@Nullable private final Expression initializer;

	public VariableInfo(String name, TypeRefInfo typeRef, @Nullable Expression initializer, boolean _static, boolean _register) {
		this.name = name;
		this.typeRef = typeRef;
		this.initializer = initializer;
		this._static = _static;
		this._register = _register;
	}

	@Override
	public boolean isAccessibleFrom(SpaceInfo space) {
		return true;
	}

	@Override
	public SpaceInfo getParent() {
		return null;
	}

	private final boolean _static;
	private final boolean _register;

	public boolean isRegister() {
		if (typeRef.getType() instanceof TypeArrayInfo && !((TypeArrayInfo) typeRef.getType()).isStaticArray())
			return false;
		if (_register) return true;
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
