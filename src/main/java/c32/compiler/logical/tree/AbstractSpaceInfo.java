package c32.compiler.logical.tree;

import c32.compiler.logical.DuplicatedFunctionException;
import lombok.Getter;

import java.util.*;

@Getter
public abstract class AbstractSpaceInfo extends AbstractSymbolInfo implements SpaceInfo {
	private final Map<String,Set<FunctionInfo>> functions = new HashMap<>();
	private final Map<String,NamespaceInfo> namespaces = new HashMap<>();
	private final Map<String,FieldInfo> fields = new HashMap<>();
	private final Map<String,TypeInfo> typenames = new HashMap<>();
	private final Set<ImportInfo> imports = new HashSet<>();

	@Override
	public void addImport(ImportInfo importInfo) {
		imports.add(importInfo);
	}

	@Override
	public Set<FunctionInfo> getFunctions() {
		Set<FunctionInfo> set = new HashSet<>();
		for (Set<FunctionInfo> value : functions.values()) {
			set.addAll(value);
		}
		return set;
	}

	@Override
	public FunctionInfo addFunction(FunctionInfo function) {
		Set<FunctionInfo> funcWithThisName = functions.get(function.getName());
		if (funcWithThisName != null) {
			for (FunctionInfo functionInfo : funcWithThisName) {
				if (functionInfo.equalsDeclarationSignature(function))
					throw new DuplicatedFunctionException(function);
			}
		}
		Set<FunctionInfo> functionSet = functions.computeIfAbsent(function.getName(), k -> new HashSet<>());

		functionSet.add(function);
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
}
