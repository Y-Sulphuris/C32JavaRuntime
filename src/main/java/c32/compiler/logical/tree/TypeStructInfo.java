package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class TypeStructInfo extends AbstractSpaceInfo implements TypeInfo {
	private final String name;
	private final SpaceInfo parent;
	private final NamespaceInfo staticSpace;

	@Override
	public boolean isAccessibleFrom(SpaceInfo space) {
		return true;
	}

	@Override
	public long sizeof() {
		return 0;
	}

	@Override
	public Expression getDefaultValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public NamespaceInfo addNamespace(NamespaceInfo namespace) {
		if (namespace.is_static()) {
			staticSpace.addNamespace(namespace);
			return namespace;
		}
		return super.addNamespace(namespace);
	}

	@Override
	public FunctionInfo addFunction(FunctionInfo function) {
		return super.addFunction(function);
	}

	@Override
	public FieldInfo addField(FieldInfo field) {
		if (field.is_static()) {
			staticSpace.addField(field);
			return field;
		}
		return super.addField(field);
	}

}
