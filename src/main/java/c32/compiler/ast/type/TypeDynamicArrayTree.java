package c32.compiler.ast.type;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.TypeName;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;

public class TypeDynamicArrayTree extends TypeTree {
	@Getter
	private final TypeTree elementType;
	private static final HashMap<TypeTree,TypeDynamicArrayTree> arrayTypes = new HashMap<>();

	public TypeDynamicArrayTree(TypeTree elementType) {
		super("__array_"+elementType.name);
		this.elementType = elementType;
	}

	public static TypeDynamicArrayTree get(TypeTree type) {
		if (arrayTypes.containsKey(type)) {
			return arrayTypes.get(type);
		}
		TypeDynamicArrayTree arType = new TypeDynamicArrayTree(type);
		arrayTypes.put(type,arType);
		return arType;
	}

	@Override
	public TypeName getJavaType() {
		return ArrayTypeName.of(elementType.getJavaType());
	}
}
