package c32.compiler.logical.tree;

import lombok.Getter;

import java.util.*;

@Getter
public abstract class AbstractSpaceInfo extends AbstractSymbolInfo implements SpaceInfo {
	private final Set<FunctionInfo> functions = new HashSet<>();
	private final Map<String,NamespaceInfo> namespaces = new HashMap<>();
	private final Map<String,FieldInfo> fields = new HashMap<>();
	private final Map<String,TypeStructInfo> structs = new HashMap<>();


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

	public Collection<TypeStructInfo> getStructs() {
		return structs.values();
	}

	@Override
	public TypeStructInfo getStruct(String name) {
		return structs.get(name);
	}
	@Override
	public TypeStructInfo addStruct(TypeStructInfo struct) {
		if (structs.containsKey(struct.getName()))
			throw new UnsupportedOperationException();
		structs.put(struct.getName(),struct);
		return struct;
	}
}
