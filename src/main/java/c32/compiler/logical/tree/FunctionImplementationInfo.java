package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.VariableRefExpression;
import c32.compiler.logical.tree.statement.BlockStatement;
import c32.compiler.logical.tree.statement.Statement;
import c32.compiler.logical.tree.statement.VariableDeclarationStatement;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
public class FunctionImplementationInfo extends AbstractSymbolInfo implements FunctionInfo, SpaceInfo {
	private final FunctionDeclarationInfo declaration;
	private final BlockStatement implementation = new BlockStatement(this,null);

	public FunctionImplementationInfo(FunctionDeclarationInfo declaration) {
		this.declaration = declaration;
	}

	@Override
	public SpaceInfo getParent() {
		return declaration.getParent();
	}

	@Override
	public Set<FunctionInfo> getFunctions() {
		return implementation.getFunctions();
	}

	@Override
	public FunctionInfo addFunction(FunctionInfo function) {
		return implementation.addFunction(function);
	}

	@Override
	public Collection<NamespaceInfo> getNamespaces() {
		return implementation.getNamespaces();
	}

	@Override
	public NamespaceInfo addNamespace(NamespaceInfo namespace) {
		return implementation.addNamespace(namespace);
	}

	@Override
	public Collection<FieldInfo> getFields() {
		return implementation.getFields();
	}

	@Override
	public FieldInfo addField(FieldInfo field) {
		return implementation.addField(field);
	}

	@Override
	public Collection<TypeStructInfo> getStructs() {
		return implementation.getStructs();
	}

	@Override
	public TypeStructInfo addStruct(TypeStructInfo struct) {
		return implementation.addStruct(struct);
	}

	@Override
	public boolean isAccessibleFrom(SpaceInfo namespace) {
		return implementation.isAccessibleFrom(namespace);
	}

	@Override
	public String getName() {
		return declaration.getName();
	}

	@Override
	public TypeInfo getReturnType() {
		return declaration.getReturnType();
	}

	@Override
	public List<VariableInfo> getArgs() {
		return declaration.getArgs();
	}

	@Override
	public List<TypeInfo> getThrowTypes() {
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

	@Override
	public boolean is_extern() {
		return declaration.is_extern();
	}

	@Override
	public boolean is_native() {
		return declaration.is_native();
	}


	@Override
	public VariableRefExpression resolveVariable(SpaceInfo caller, ReferenceExprTree reference) {
		for (VariableInfo arg : declaration.getArgs()) {
			if (arg.getName().equals(reference.getIdentifier().text))
				return new VariableRefExpression(arg);
		}
		return getParent().resolveVariable(caller,reference);
	}
}