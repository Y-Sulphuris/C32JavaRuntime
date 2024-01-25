package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.logical.tree.CompileTimeTypeInfo;
import c32.compiler.logical.tree.SpaceInfo;
import c32.compiler.logical.tree.TypeInfo;
import lombok.Getter;

@Getter
public class SpaceRefExpression implements Expression {
    private final Location location;
    private final SpaceInfo space;
    public SpaceRefExpression(Location location, SpaceInfo space) {
        this.location = location;
        this.space = space;
    }

    @Override
    public TypeInfo getReturnType() {
        return CompileTimeTypeInfo.Namespace;
    }
}
