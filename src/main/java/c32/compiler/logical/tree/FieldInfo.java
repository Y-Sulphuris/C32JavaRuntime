package c32.compiler.logical.tree;

import c32.compiler.Location;
import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class FieldInfo extends VariableInfo {
    private final SpaceInfo container;

	public FieldInfo(Location location, String name, TypeRefInfo typeRef, @Nullable Expression initializer, boolean _static, Boolean _register, SpaceInfo container) {
		super(location, name, typeRef, initializer, _static, _register);
		this.container = container;
	}


}
