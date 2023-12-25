package c32.compiler.logical.tree;

import c32.compiler.logical.tree.statement.BlockStatement;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
public class FunctionImplementationInfo implements FunctionInfo {
	private final FunctionDeclarationInfo declaration;
	private BlockStatement implementation;

	public void setImplementation(BlockStatement implementation) {
		this.implementation = implementation;
	}

	public FunctionImplementationInfo(FunctionDeclarationInfo declaration) {
		this.declaration = declaration;
	}

	@Override
	public SpaceInfo getParent() {
		return declaration.getParent();
	}

	@Override
	public Set<FunctionInfo> getFunctions() {
		return declaration.getFunctions();
	}

	@Override
	public FunctionInfo addFunction(FunctionInfo function) {
		return declaration.addFunction(function);
	}

	@Override
	public Set<NamespaceInfo> getNamespaces() {
		return declaration.getNamespaces();
	}

	@Override
	public NamespaceInfo addNamespace(NamespaceInfo namespace) {
		return declaration.addNamespace(namespace);
	}

	@Override
	public Set<FieldInfo> getFields() {
		return declaration.getFields();
	}

	@Override
	public FieldInfo addField(FieldInfo field) {
		return declaration.addField(field);
	}

	@Override
	public boolean isAccessibleFrom(SpaceInfo namespace) {
		return declaration.isAccessibleFrom(namespace);
	}

	@Override
	public String getName() {
		return declaration.getName();
	}

	@Override
	public TypeRefInfo getReturnType() {
		return declaration.getReturnType();
	}

	@Override
	public List<VariableInfo> getArgs() {
		return declaration.getArgs();
	}

	@Override
	public List<TypeRefInfo> getThrowTypes() {
		return declaration.getThrowTypes();
	}

	@Override
	public boolean is_pure() {
		return declaration.is_pure();
	}

	@Override
	public boolean is_noexcept() {
		return declaration.is_noexcept();
	}
}
