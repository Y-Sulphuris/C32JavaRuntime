package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.logical.tree.CompileTimeTypeInfo;
import c32.compiler.logical.tree.SpaceInfo;
import c32.compiler.logical.tree.SymbolInfo;
import c32.compiler.logical.tree.TypeInfo;
import lombok.Getter;

@Getter
public class SpaceRefExpression extends SymbolRefExpression {
    public SpaceRefExpression(SpaceInfo space, Location location) {
        super(space, location);
    }

	@Override
	public SpaceInfo get() {
		return (SpaceInfo) super.get();
	}

	public SpaceInfo getSpace() {
		return get();
	}

	@Override
    public TypeInfo getReturnType() {
        return CompileTimeTypeInfo.Namespace;
    }
}
