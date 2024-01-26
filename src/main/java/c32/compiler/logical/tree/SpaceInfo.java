package c32.compiler.logical.tree;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.CompilerAccessException;
import c32.compiler.logical.FunctionNotFoundException;
import c32.compiler.logical.TypeNotFoundException;
import c32.compiler.logical.VariableNotFoundException;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.logical.tree.expression.VariableRefExpression;
import c32.compiler.parser.ast.expr.CallExprTree;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import c32.compiler.parser.ast.type.*;

import java.util.*;

public interface SpaceInfo extends SymbolInfo {
	SpaceInfo getParent();


	Collection<FunctionInfo> getFunctions();
	FunctionInfo addFunction(FunctionInfo function);



	Collection<NamespaceInfo> getNamespaces();
	default NamespaceInfo getNamespace(String name) {
		for (NamespaceInfo namespace : getNamespaces()) {
			if (namespace.getName().equals(name)) return namespace;
		}
		return null;
	}
	NamespaceInfo addNamespace(NamespaceInfo namespace);



	Collection<FieldInfo> getFields();
	default FieldInfo getField(String name) {
		for (FieldInfo field : getFields()) {
			if (field.getName().equals(name)) return field;
		}
		return null;
	}
	FieldInfo addField(FieldInfo field);



	default Map<String,TypeInfo> getTypenames() {
		return Collections.emptyMap();
	}

	default TypeInfo addTypeName(String name, TypeInfo type) {
		Map<String,TypeInfo> typenames = getTypenames();
		if (typenames.containsKey(name))
			throw new UnsupportedOperationException();
		typenames.put(name,type);
		return type;
	}


	default TypeInfo resolveType(SpaceInfo caller, TypeElementTree type) {
		if (type instanceof TypeKeywordElementTree)
		{
			return TypeInfo.PrimitiveTypeInfo.resolve((TypeKeywordElementTree)type);
		}
		else if (type instanceof StaticArrayTypeElementTree)
		{
			if (type.get_restrict() != null) throw new CompilerException(type.getLocation(),"arrays cannot be restrict");
			return TypeArrayInfo.arrayOf(
					((StaticArrayTypeElementTree) type).getSize().getLocation(),
					Expression.build(caller,caller,((StaticArrayTypeElementTree) type).getSize(), TypeInfo.PrimitiveTypeInfo.LONG),
					new TypeRefInfo(((ArrayTypeElementTree) type).getElementType().get_const() != null, false,
					this.resolveType(caller,((ArrayTypeElementTree) type).getElementType())));

		}
		else if (type instanceof ArrayTypeElementTree)
		{
			if (type.get_restrict() != null) throw new CompilerException(type.getLocation(),"arrays cannot be restrict");
			return TypeArrayInfo.arrayOf(-1, new TypeRefInfo(((ArrayTypeElementTree) type).getElementType().get_const() != null, false,
							this.resolveType(caller,((ArrayTypeElementTree) type).getElementType())));

		}
		else if (type instanceof PointerTypeElementTree)
		{
			return TypePointerInfo.pointerOf(
					new TypeRefInfo(
							((PointerTypeElementTree) type).getElementType().get_const() != null,
							((PointerTypeElementTree) type).getElementType().get_restrict() != null,
							this.resolveType(caller,((PointerTypeElementTree) type).getElementType())));
		} else if (type instanceof TypeReferenceElementTree) {
			if (((TypeReferenceElementTree) type).getReference().getReferences().size() == 1) {
				//trying to find typename
				if (true) {
					String ref = ((TypeReferenceElementTree) type).getReference().getReferences().get(0).getIdentifier().text;
					TypeInfo typeInfo = this.getTypenames().get(ref);
					if (typeInfo != null) return typeInfo;
					else {
						if (getParent() != null)
							return getParent().resolveType(caller,type);
						else
							throw new TypeNotFoundException(caller,type);
					}
				} else {
					if (true) throw new UnsupportedOperationException(this.getClass().getName());
					//todo: make all typename
					//trying to find typename
					/*TypeInfo struct = getStruct(((TypeReferenceElementTree) type).getReference().getReferences().get(0).getIdentifier().text);
					if (struct != null) return struct;

					//if we failed, trying to find it from parent space
					if (getParent() != null) {
						TypeInfo ref = getParent().resolveType(caller,type);
						if (ref == null) throw new TypeNotFoundException(caller,type);
						if (ref.isAccessibleFrom(caller)) {
							return ref;
						} else throw new CompilerAccessException(type.getLocation(), caller, ref);
					}
					return null;*/
				}
			} else {
				List<ReferenceExprTree> references = ((TypeReferenceElementTree) type).getReference().getReferences();
				ReferenceExprTree namespaceRef = references.remove(0);
				return resolveNamespace(caller, namespaceRef).resolveType(this, type);
			}
		} else if (type instanceof DeclTypeElementTree) {
			Expression expression = Expression.build(caller,caller,((DeclTypeElementTree) type).getExpression(),null);
			if (expression.getReturnType() == null)
				throw new CompilerException(type.getLocation(), "cannot get type from given expression");
			return expression.getReturnType();
		}

		throw new UnsupportedOperationException(type.getClass().getName());
	}

	default SpaceInfo resolveSpace(SpaceInfo caller, StaticElementReferenceTree reference) {
		List<ReferenceExprTree> references = reference.getReferences();
		if (references.isEmpty()) return this;
		ReferenceExprTree currentRef = references.remove(0);
		if (references.size() == 1) {
			return resolveSpace(caller,currentRef);
		}
		String current = currentRef.getIdentifier().text;
		for (NamespaceInfo namespace : getNamespaces()) {
			if (namespace.getName().equals(current)) {
				SpaceInfo space = namespace.resolveSpace(caller,reference);
				if (!space.isAccessibleFrom(caller))
					throw new CompilerAccessException(reference.getLocation(),caller,space);
				return space;
			}
		}
		/*for (FieldInfo field : getFields()) {
			field.getTypeRef().getType().get
		}*/
		if (getParent() == null)
			throw new CompilerException(reference.getLocation(), "cannot find anything from '" + caller.getCanonicalName() + "' for '" + reference.getReferences() + "'");
		return getParent().resolveSpace(caller,reference);
	}
	default SpaceInfo resolveSpace(SpaceInfo caller, ReferenceExprTree reference) {
		String current = reference.getIdentifier().text;
		for (NamespaceInfo namespace : getNamespaces()) {
			if (namespace.getName().equals(current)) {
				return namespace;
			}
		}
		if (getParent() == null)
			throw new CompilerException(reference.getLocation(), "cannot find anything from '" + caller.getCanonicalName() + "' for '" + reference.getIdentifier().text + "'");
		return getParent().resolveSpace(caller,reference);
	}

	default NamespaceInfo resolveNamespace(SpaceInfo caller, ReferenceExprTree reference) {
		for (NamespaceInfo namespace : getNamespaces()) {
			if (namespace.getName().equals(reference.getIdentifier().text) && namespace.isAccessibleFrom(caller)) return namespace;
		}
		if (getParent() == null)
			throw new CompilerException(reference.getLocation(), "cannot find namespace: " + reference.getIdentifier().text);
		return getParent().resolveNamespace(caller,reference);
	}

	default VariableRefExpression resolveVariable(SpaceInfo caller, ReferenceExprTree reference) {
		for (FieldInfo field : getFields()) {
			if (field.getName().equals(reference.getIdentifier().text) && field.getVariable().isAccessibleFrom(this)) {
				return new VariableRefExpression(field.getVariable(),reference.getLocation());
			}
		}
		if (getParent() != null) return getParent().resolveVariable(caller, reference);
		throw new VariableNotFoundException(reference);
	}

	default FunctionInfo resolveFunction(SpaceInfo caller, CallExprTree call, List<Expression> args) {
		String fname = call.getReference().getIdentifier().text;
		Collection<FunctionInfo> functions = getFunctions();

		EXIT:
		for (FunctionInfo function : functions) {
			if (!function.getName().equals(fname)) continue;
			if (args.size() != function.getArgs().size()) continue;
			for (int i = 0; i < args.size(); i++) {
				if (!args.get(i).getReturnType().equals(function.getArgs().get(i).getTypeRef().getType())) {
					continue EXIT;
				}
			}
			if (function.isAccessibleFrom(caller)) {
				return function;
			} else throw new CompilerAccessException(call.getLocation(), caller, function);
		}

		EXIT_IMPLICIT:
		for (FunctionInfo function : functions) {
			if (!function.getName().equals(fname)) continue;
			if (args.size() != function.getArgs().size()) continue;
			for (int i = 0; i < args.size(); i++) {
				if (!args.get(i).getReturnType().canBeImplicitlyCastTo(function.getArgs().get(i).getTypeRef().getType())) {
					continue EXIT_IMPLICIT;
				}
			}
			if (function.isAccessibleFrom(caller)) {
				return function;
			} else throw new CompilerAccessException(call.getLocation(), caller, function);
		}

		if (getParent() == null) {
			throw new FunctionNotFoundException(call,args);
		}
		return getParent().resolveFunction(caller,call,args);
	}

	default boolean containsOrThisRecursive(SpaceInfo space) {
		if (space == this) return true;
		if (space.getParent() == null) return false;
		return this.containsOrThisRecursive(space.getParent());
	}

}
