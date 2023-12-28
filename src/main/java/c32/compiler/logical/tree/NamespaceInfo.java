package c32.compiler.logical.tree;

import c32.compiler.except.CompilerException;
import lombok.Data;

import java.util.*;

@Data
public class NamespaceInfo implements SpaceInfo {
	private final String name;
	private final SpaceInfo parent;

	private final Set<FunctionInfo> functions = new HashSet<>();
	private final Map<String,NamespaceInfo> namespaces = new HashMap<>();
	private final Map<String,FieldInfo> fields = new HashMap<>();


	@Override
	public FunctionInfo addFunction(FunctionInfo function) {
		functions.add(function);
		return function;
	}

	public Collection<NamespaceInfo> getNamespaces() {
		return namespaces.values();
	}

	@Override
	public NamespaceInfo getNamespace(String name) {
		return namespaces.get(name);
	}

	@Override
	public NamespaceInfo addNamespace(NamespaceInfo namespace) {
		if (namespaces.containsKey(namespace.getName()))
			throw new UnsupportedOperationException();
		namespaces.put(namespace.getName(), namespace);
		return namespace;
	}

	@Override
	public Collection<FieldInfo> getFields() {
		return fields.values();
	}

	@Override
	public FieldInfo getField(String name) {
		return fields.get(name);
	}

	@Override
	public FieldInfo addField(FieldInfo field) {
		if (fields.containsKey(field.getName()))
			throw new UnsupportedOperationException();
		fields.put(field.getName(),field);
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
