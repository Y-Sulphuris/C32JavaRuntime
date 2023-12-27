package c32.compiler.logical.tree;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.parser.ast.type.TypeKeywordElementTree;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public interface TypeInfo extends SpaceInfo {
	long sizeof();

	Expression getDefaultValue();

	default boolean canBeImplicitCastTo(TypeInfo type) {
		return this.equals(type);
	}

	class NumericPrimitiveTypeInfo extends PrimitiveTypeInfo {
		public NumericPrimitiveTypeInfo(String name, long size) {
			super(name, size);
		}
		public UnsignedIntegerPrimitiveTypeInfo getUnsigned() {
			return null;
		}
	}
	class IntegerPrimitiveTypeInfo extends NumericPrimitiveTypeInfo {
		public IntegerPrimitiveTypeInfo(String name, long size) {
			super(name, size);
		}

		@Override
		public UnsignedIntegerPrimitiveTypeInfo getUnsigned() {
			return (UnsignedIntegerPrimitiveTypeInfo) PrimitiveTypeInfo.types.get('u'+this.getName());
		}
	}
	class UnsignedIntegerPrimitiveTypeInfo extends IntegerPrimitiveTypeInfo {
		public UnsignedIntegerPrimitiveTypeInfo(String name, long size) {
			super(name, size);
		}
	}
	class CharPrimitiveTypeInfo extends IntegerPrimitiveTypeInfo {
		public CharPrimitiveTypeInfo(String name, long size) {
			super(name, size);
		}
	}
	@Getter
	class PrimitiveTypeInfo implements TypeInfo {

		@Override
		public String toString() {
			return "TypeInfo(" + name + ')';
		}

		private final String name;
		private final long size;
		private static final HashMap<String, PrimitiveTypeInfo> types = new HashMap<>();

		public static final PrimitiveTypeInfo                   VOID = new PrimitiveTypeInfo("void",0);
		public static final PrimitiveTypeInfo                   BOOL = new PrimitiveTypeInfo("bool",1);
		public static final IntegerPrimitiveTypeInfo            BYTE = new IntegerPrimitiveTypeInfo("byte",1);
		public static final UnsignedIntegerPrimitiveTypeInfo    UBYTE = new UnsignedIntegerPrimitiveTypeInfo("ubyte",1);
		public static final IntegerPrimitiveTypeInfo            SHORT = new IntegerPrimitiveTypeInfo("short",2);
		public static final UnsignedIntegerPrimitiveTypeInfo    USHORT = new UnsignedIntegerPrimitiveTypeInfo("ushort",2);
		public static final IntegerPrimitiveTypeInfo            INT = new IntegerPrimitiveTypeInfo("int",4);
		public static final UnsignedIntegerPrimitiveTypeInfo    UINT = new UnsignedIntegerPrimitiveTypeInfo("uint",4);
		public static final IntegerPrimitiveTypeInfo            LONG = new IntegerPrimitiveTypeInfo("long",8);
		public static final UnsignedIntegerPrimitiveTypeInfo    ULONG = new UnsignedIntegerPrimitiveTypeInfo("ulong",8);
		public static final NumericPrimitiveTypeInfo            FLOAT = new NumericPrimitiveTypeInfo("float",4);
		public static final NumericPrimitiveTypeInfo            DOUBLE = new NumericPrimitiveTypeInfo("double",8);

		public static final CharPrimitiveTypeInfo               CHAR = new CharPrimitiveTypeInfo("char",2);

		public PrimitiveTypeInfo(String name, long size) {
			this.name = name;
			this.size = size;
			types.put(name,this);
		}

		public static TypeInfo resolve(TypeKeywordElementTree type) {
			TypeInfo typeInfo = types.get(type.getKeyword().text);
			if (typeInfo == null) return null;
			if (type.get_restrict() != null) throw new CompilerException(type.getLocation(),"primitives cannot be restrict");
			return typeInfo;
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
			throw new UnsupportedOperationException("cannot add function to primitive type");
		}

		@Override
		public Set<NamespaceInfo> getNamespaces() {
			return Collections.emptySet();
		}

		@Override
		public NamespaceInfo addNamespace(NamespaceInfo namespace) {
			throw new UnsupportedOperationException("cannot add function to primitive type");
		}

		@Override
		public Set<FieldInfo> getFields() {
			return Collections.emptySet();
		}

		@Override
		public FieldInfo addField(FieldInfo field) {
			throw new UnsupportedOperationException("cannot add field to primitive type");
		}

		@Override
		public boolean isAccessibleFrom(SpaceInfo namespace) {
			return true;
		}

		@Override
		public long sizeof() {
			return size;
		}

		@Override
		public Expression getDefaultValue() {
			throw new UnsupportedOperationException();
		}
	}
}
