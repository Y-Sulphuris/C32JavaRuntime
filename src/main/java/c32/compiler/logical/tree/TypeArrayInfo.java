package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

@Getter
public class TypeArrayInfo implements TypeInfo {
	private final TypeRefInfo elementType;

	private TypeArrayInfo(TypeRefInfo elementType) {
		this.elementType = elementType;
	}
	private static final HashMap<TypeRefInfo, TypeArrayInfo> arrayTypes = new HashMap<>();

	@NotNull
	public static TypeArrayInfo arrayOf(TypeRefInfo typeRefInfo) {
		if (arrayTypes.containsKey(typeRefInfo))
			return arrayTypes.get(typeRefInfo);
		TypeArrayInfo arr = new TypeArrayInfo(typeRefInfo);
		arrayTypes.put(typeRefInfo,arr);
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
		return "__array__$" + elementType.getType().getName() + "$";
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
		return (elementType.is_const() ? "const " : "") + (elementType.is_restrict() ? "restrict " : "") + elementType.getType().getCanonicalName() + "[]";
	}

	@Override
	public boolean canBeImplicitCastTo(TypeInfo type) {
		if (type instanceof TypeArrayInfo) {
			return TypeInfo.super.canBeImplicitCastTo(type) || this.getElementType().canBeImplicitCastTo(((TypeArrayInfo) type).getElementType());
		}
		return TypeInfo.super.canBeImplicitCastTo(type);
	}
}
