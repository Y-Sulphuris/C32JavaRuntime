package c32.compiler.logical.tree;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class TypeRefInfo {
	private final boolean _const;
	private final boolean _restrict;
	private final TypeInfo type;

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
		if (_const) {
			builder.append("const ");
			br = true;
		}
		if (_restrict) {
			builder.append("restrict ");
			br = true;
		}
		return br ? builder.append('(').append(type.getName()).append(')').toString() : builder.append(type.getCanonicalName()).toString();
	}
}
