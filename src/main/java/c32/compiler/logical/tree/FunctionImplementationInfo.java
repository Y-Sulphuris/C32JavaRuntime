package c32.compiler.logical.tree;

import c32.compiler.logical.tree.statement.BlockStatement;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
public class FunctionImplementationInfo implements FunctionInfo {
	private final FunctionDeclarationInfo declaration;
	private final BlockStatement implementation = new BlockStatement();

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
	public List<LocalVariableInfo> getArgs() {
		return declaration.getArgs();
	}

	@Override
	public List<TypeRefInfo> getThrowTypes() {
		return declaration.getThrowTypes();
	}
}
