package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

@Getter
public class TypePointerInfo extends AbstractSymbolInfo implements TypeInfo {

	private final TypeRefInfo targetType;

	private TypePointerInfo(TypeRefInfo targetType) {
		this.targetType = targetType;
	}
	private static final HashMap<TypeRefInfo, TypePointerInfo> ptrTypes = new HashMap<>();

	@NotNull
	public static TypePointerInfo pointerOf(TypeRefInfo typeRefInfo) {
		if (ptrTypes.containsKey(typeRefInfo))
			return ptrTypes.get(typeRefInfo);
		TypePointerInfo arr = new TypePointerInfo(typeRefInfo);
		ptrTypes.put(typeRefInfo,arr);
		return arr;
	}

	@Override
	public SpaceInfo getParent() {
		return null;
	}

	@Override
	public Set<FunctionInfo> getFunctions() {
		return Collections.emptySet();
	}

	@Override
	public FunctionInfo addFunction(FunctionInfo function) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<NamespaceInfo> getNamespaces() {
		return Collections.emptySet();
	}

	@Override
	public NamespaceInfo addNamespace(NamespaceInfo namespace) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<FieldInfo> getFields() {
		return Collections.emptySet();
	}

	@Override
	public FieldInfo addField(FieldInfo field) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAccessibleFrom(SpaceInfo namespace) {
		return true;
	}

	@Override
	public String getName() {
		return "__pointer__$" + targetType.getType().getName() + "$";
	}

	@Override
	public long sizeof() {
		return 8;
	}

	@Override
	public Expression getDefaultValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCanonicalName() {
		return (targetType.is_mut() ? "mut " : "") + (targetType.is_restrict() ? "restrict " : "") + targetType.getType().getCanonicalName() + "*";
	}

	@Override
	public boolean canBeImplicitlyCastTo(TypeInfo type) {
		if (type instanceof TypePointerInfo) {
			TypeRefInfo typeRef = ((TypePointerInfo) type).getTargetType();
			TypeRefInfo thisRef = this.getTargetType();

			if (thisRef.getType() == PrimitiveTypeInfo.VOID || typeRef.getType() == PrimitiveTypeInfo.VOID || typeRef.getType().equals(thisRef.getType())) {
				return thisRef.is_mut() || !typeRef.is_mut(); // в чём прикол этой строки
			}
			return typeRef.equals(this.targetType);
		}
		return TypeInfo.super.canBeImplicitlyCastTo(type);
	}

	@Override
	public boolean canBeExplicitlyCastTo(TypeInfo type) {
		if (TypeInfo.super.canBeExplicitlyCastTo(type))
			return true;
		return type.canBeImplicitlyCastTo(PrimitiveTypeInfo.LONG);
	}
}
