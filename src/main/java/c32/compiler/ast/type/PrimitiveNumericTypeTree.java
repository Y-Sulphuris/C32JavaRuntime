package c32.compiler.ast.type;

import c32.compiler.ast.NoUnsignedTypeException;
import com.squareup.javapoet.TypeName;

public class PrimitiveNumericTypeTree extends PrimitiveTypeTree {
	private PrimitiveNumericTypeTree unsigned;

	public PrimitiveTypeTree setUnsigned(PrimitiveNumericTypeTree unsigned) {
		if (this.unsigned != null) throw new UnsupportedOperationException();
		this.unsigned = unsigned;
		return this;
	}

	protected PrimitiveNumericTypeTree(String name, TypeName javaType, int size) {
		super(name, javaType, size);
	}

	public boolean isUnsigned() {
		return getName().startsWith("u");
	}

	@Override
	public PrimitiveNumericTypeTree getUnsignedVersion() {
		if (isUnsigned()) return this;
		if (unsigned == null) throw new NoUnsignedTypeException(this);
		return unsigned;
	}
}
