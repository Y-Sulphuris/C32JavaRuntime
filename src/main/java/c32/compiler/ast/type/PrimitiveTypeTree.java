package c32.compiler.ast.type;

import com.squareup.javapoet.TypeName;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PrimitiveTypeTree extends TypeTree {
	private final TypeName javaType;
	private final int size;
	private final Set<TypeTree> implicitCast = new HashSet<>();

	protected PrimitiveTypeTree(String name, TypeName javaType, int size) {
		super(name);
		this.javaType = javaType;
		this.size = size;
		overriddenTypes.put(this.name, this);
	}

	protected void addImplicitCast(TypeTree... types) {
		Collections.addAll(implicitCast, types);
	}

	@Override
	public boolean canBeImplicitCastTo(TypeTree type) {
		return super.canBeImplicitCastTo(type) || implicitCast.contains(type);
	}

	public int getSize() {
		return size;
	}

	@Override
	public String getJavaTypeName() {
		return javaType.toString();
	}

	@Override
	public TypeName getJavaType() {
		return javaType;
	}
}
