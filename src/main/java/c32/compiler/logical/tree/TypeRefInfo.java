package c32.compiler.logical.tree;

import lombok.Data;

@Data
public class TypeRefInfo {
	private final boolean _const;
	private final boolean _restrict;
	private final TypeInfo type;
}
