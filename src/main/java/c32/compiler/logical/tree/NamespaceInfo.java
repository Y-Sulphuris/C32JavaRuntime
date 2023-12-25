package c32.compiler.logical.tree;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class NamespaceInfo implements SpaceInfo {
	private final String name;
	private final SpaceInfo parent;

	private final Set<FunctionInfo> functions = new HashSet<>();
	private final Set<NamespaceInfo> namespaces = new HashSet<>();
	private final Set<FieldInfo> fields = new HashSet<>();


	@Override
	public FunctionInfo addFunction(FunctionInfo function) {
		functions.add(function);
		return function;
	}

	@Override
	public NamespaceInfo addNamespace(NamespaceInfo namespace) {
		namespaces.add(namespace);
		return namespace;
	}

	@Override
	public Set<FieldInfo> getFields() {
		return fields;
	}

	@Override
	public FieldInfo addField(FieldInfo field) {
		fields.add(field);
		return field;
	}

	@Override
	public boolean isAccessibleFrom(SpaceInfo namespace) {
		return true;
	}

	@Override
	public String toString() {
		return super.toString();
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object x) {
		return super.equals(x);
	}
}
