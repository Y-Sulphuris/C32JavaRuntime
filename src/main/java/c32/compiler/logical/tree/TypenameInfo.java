package c32.compiler.logical.tree;

import c32.compiler.Location;
import lombok.Getter;

@Getter
public class TypenameInfo extends AbstractSymbolInfo {
	private final String name;
	private final TypeInfo type;
	private final SpaceInfo parent;
	private final Location location;

	public TypenameInfo(String name, TypeInfo type, SpaceInfo parent, Location location) {
		this.name = name;
		this.type = type;
		this.parent = parent;
		this.location = location;
	}
}
