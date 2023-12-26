package c32.compiler.logical.tree;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.CompilerAccessException;
import c32.compiler.logical.TypeNotFoundException;
import c32.compiler.logical.tree.expression.VariableRefExpression;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import c32.compiler.parser.ast.type.ArrayTypeElementTree;
import c32.compiler.parser.ast.type.TypeElementTree;
import c32.compiler.parser.ast.type.TypeKeywordElementTree;

import java.util.Set;

public interface SpaceInfo extends SymbolInfo {
	SpaceInfo getParent();

	Set<FunctionInfo> getFunctions();
	FunctionInfo addFunction(FunctionInfo function);

	Set<NamespaceInfo> getNamespaces();
	NamespaceInfo addNamespace(NamespaceInfo namespace);

	Set<FieldInfo> getFields();
	FieldInfo addField(FieldInfo field);


	default TypeRefInfo resolveType(SpaceInfo caller, TypeElementTree type) {
		if (type instanceof TypeKeywordElementTree) {
			return TypeInfo.PrimitiveTypeInfo.resolve((TypeKeywordElementTree)type);
		}
		if (type instanceof ArrayTypeElementTree) {
			if (type.get_restrict() != null) throw new CompilerException(type.getLocation(),"arrays cannot be restrict");
			return new TypeRefInfo(type.get_const() != null,false, TypeArrayInfo.arrayOf(this.resolveType(caller,((ArrayTypeElementTree) type).getElementType()).getType()));
		}
		if (getParent() != null) {
			TypeRefInfo ref = getParent().resolveType(caller,type);
			if (ref == null) throw new TypeNotFoundException(caller,type);
			if (ref.getType().isAccessibleFrom(caller)) {
				return ref;
			} else throw new CompilerAccessException(caller,type);
		}
		return null;
	}

	default VariableRefExpression resolveVariable(ReferenceExprTree reference) {
		for (FieldInfo field : getFields()) {
			if (field.getName().equals(reference.getIdentifier().text) && field.getVariable().isAccessibleFrom(this)) {
				return new VariableRefExpression(field.getVariable());
			}
		}
		if (getParent() != null) return getParent().resolveVariable(reference);
		throw new CompilerException(reference.getLocation(),"cannot find variable: " + reference.getIdentifier().text);
	}
}
