package c32.compiler.logical.tree;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class TypeRefInfo {
	private final boolean _mut;
	private final boolean _const;
	private final boolean _restrict;
	private final TypeInfo type;

	public TypeRefInfo(boolean _mut, boolean _const, boolean restrict, TypeInfo type) {
		this._mut = _mut;
		this._const = _const;
		this._restrict = restrict;
		this.type = type;
	}

	public boolean canBeImplicitlyCastTo(@NotNull TypeRefInfo typeRef) {
		if (!typeRef.is_const() && this.is_const()) {
			return false;
		}
		return type.canBeImplicitlyCastTo(typeRef.getType());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		boolean br = false;
		if (is_mut()) {
			builder.append("mut ");
			br = true;
		}
		if (is_restrict()) {
			builder.append("restrict ");
			br = true;
		}
		return br ? builder.append('(').append(type.getName()).append(')').toString() : builder.append(type.getCanonicalName()).toString();
	}
}
