package c32.compiler.ast.type;

import c32.compiler.ast.NoUnsignedTypeException;
import c32.compiler.ast.Tree;
import com.squareup.javapoet.TypeName;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TypeTree implements Tree {
	protected final String name;

	public final String getName() {
		return name;
	}

	public String getJavaTypeName() {
		return getJavaType().toString();
	}
	public TypeName getJavaType() {
		throw new UnsupportedOperationException("no java type");
	}

	@Override
	public String toString() {
		return name;
	}

	public TypeTree(String name) {
		this.name = name;
	}

	public static final HashMap<String, TypeTree> overriddenTypes = new HashMap<>();

	public static final TypeTree TYPE = new TypeTree(null);

	public static final PrimitiveTypeTree
			VOID = new PrimitiveTypeTree("void", TypeName.VOID, 0),
			BYTE = new PrimitiveNumericTypeTree("byte",TypeName.BYTE, 1).setUnsigned(new PrimitiveNumericTypeTree("ubyte", TypeName.BYTE, 1)),
			SHORT = new PrimitiveNumericTypeTree("short",TypeName.SHORT, 2).setUnsigned(new PrimitiveNumericTypeTree("ushort", TypeName.SHORT, 2)),
			INT = new PrimitiveNumericTypeTree("int",TypeName.INT, 4).setUnsigned(new PrimitiveNumericTypeTree("uint", TypeName.INT, 4)),
			LONG = new PrimitiveNumericTypeTree("long",TypeName.LONG, 8).setUnsigned(new PrimitiveNumericTypeTree("ulong", TypeName.LONG, 8)),

			FLOAT = new PrimitiveNumericTypeTree("float",TypeName.FLOAT, 4),
			DOUBLE = new PrimitiveNumericTypeTree("double",TypeName.DOUBLE, 8),

			BOOL = new PrimitiveTypeTree("bool",TypeName.BOOLEAN,1),
			CHAR = new PrimitiveTypeTree("char",TypeName.CHAR,2)

			;
	static {
		BYTE.addImplicitCast(TypeTree.SHORT, TypeTree.INT, TypeTree.LONG, TypeTree.FLOAT, TypeTree.DOUBLE);
		BYTE.getUnsignedVersion().addImplicitCast(
				TypeTree.SHORT,TypeTree.SHORT.getUnsignedVersion(),
				TypeTree.INT, TypeTree.INT.getUnsignedVersion(),
				TypeTree.LONG, TypeTree.LONG.getUnsignedVersion(),
				TypeTree.FLOAT, TypeTree.DOUBLE
		);

		SHORT.addImplicitCast(TypeTree.INT, TypeTree.LONG, TypeTree.FLOAT, TypeTree.DOUBLE);
		SHORT.getUnsignedVersion().addImplicitCast(
				TypeTree.INT, TypeTree.INT.getUnsignedVersion(),
				TypeTree.LONG,TypeTree.LONG.getUnsignedVersion(),
				TypeTree.FLOAT, TypeTree.DOUBLE
		);

		INT.addImplicitCast(TypeTree.LONG, TypeTree.FLOAT, TypeTree.DOUBLE);
		INT.getUnsignedVersion().addImplicitCast(
				TypeTree.LONG, TypeTree.LONG.getUnsignedVersion(),
				TypeTree.FLOAT, TypeTree.DOUBLE
		);
		LONG.addImplicitCast(TypeTree.DOUBLE);
		LONG.getUnsignedVersion().addImplicitCast(TypeTree.DOUBLE);

		FLOAT.addImplicitCast(TypeTree.DOUBLE);
	}



	public boolean canBeImplicitCastTo(TypeTree type) {
		return type == this;
	}

	public PrimitiveNumericTypeTree getUnsignedVersion() {
		throw new NoUnsignedTypeException(this);
	}


}



