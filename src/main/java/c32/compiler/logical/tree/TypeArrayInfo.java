package c32.compiler.logical.tree;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.logical.tree.expression.NumericLiteralExpression;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class TypeArrayInfo extends AbstractSymbolInfo implements TypeInfo {
	private final TypeRefInfo elementType;
	private final int staticLength;

	public boolean isStaticArray() {
		return staticLength != -1;
	}

	private TypeArrayInfo(TypeRefInfo elementType, int staticLength) {
		this.elementType = elementType;
		this.staticLength = staticLength;
	}

	private static final HashMap<TypeRefInfo, Map<Integer, TypeArrayInfo>> arrayTypes = new HashMap<>();


	@NotNull
	public static TypeArrayInfo arrayOf(Location location, Expression expression, TypeRefInfo typeRefInfo) {
		Expression cExpression = expression.calculate().asCompileTimeLiteralExpression();
		if (cExpression instanceof NumericLiteralExpression) {
			long num = ((NumericLiteralExpression) cExpression).getNumber().longValueExact();
			if (num > Integer.MAX_VALUE || ((NumericLiteralExpression) cExpression).getNumber().bitLength() > 32) // 31?
				throw new CompilerException(location,"Static array length overflow");
			if (num < 0)
				throw new CompilerException(location,"Static array length cannot be negative");
			if (num == 0)
				throw new CompilerException(location,"Static array with zero length will declare nothing");
			return arrayOf((int)num,typeRefInfo);
		}
		throw new CompilerException(location,"Static array size expected");
	}

	@NotNull
	public static TypeArrayInfo arrayOf(Integer length, TypeRefInfo typeRefInfo) {
		Map<Integer, TypeArrayInfo> sizesMap;
		if (!arrayTypes.containsKey(typeRefInfo)) {
			sizesMap = new HashMap<>();
			arrayTypes.put(typeRefInfo,sizesMap);
		} else {
			sizesMap = arrayTypes.get(typeRefInfo);
		}

		if (sizesMap.containsKey(length)) {
			return sizesMap.get(length);
		}
		TypeArrayInfo arr = new TypeArrayInfo(typeRefInfo,length);
		sizesMap.put(length,arr);
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
		//return null;



		throw new UnsupportedOperationException();
	}

	@Override
	public String getCanonicalName() {
		return
				(elementType.is_const() ? "const " : "") +
						(elementType.is_restrict() ? "restrict " : "") +
						elementType.getType().getCanonicalName() +
						"[" + (staticLength != -1 ? staticLength + "]" : "]");
	}

	@Override
	public boolean canBeImplicitlyCastTo(TypeInfo type) {
		if (type instanceof TypeArrayInfo) {
			if (((TypeArrayInfo) type).staticLength != this.staticLength) return false;
			return TypeInfo.super.canBeImplicitlyCastTo(type) || this.getElementType().getType().equals(((TypeArrayInfo) type).getElementType().getType());
		}
		return TypeInfo.super.canBeImplicitlyCastTo(type);
	}
}
