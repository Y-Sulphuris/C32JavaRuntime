package c32.compiler.logical.tree;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.logical.tree.expression.NumericLiteralExpression;
import c32.compiler.parser.ast.type.TypeKeywordElementTree;
import lombok.Getter;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

public interface TypeInfo extends SpaceInfo {
	long sizeof();

	Expression getDefaultValue();

	@Override
	default TypenameInfo addTypename(TypenameInfo typename) {
		throw new UnsupportedOperationException();
	}

	@Override
	default Collection<TypenameInfo> getTypenames() {
		return Collections.emptySet();
	}

	@Override
	default Set<ImportInfo> getImports() {
		return Collections.emptySet();
	}

	@Override
	default void addImport(ImportInfo importInfo) {
		throw new UnsupportedOperationException();
	}

	default boolean canBeImplicitlyCastTo(TypeInfo type) {
		return this.equals(type);
	}

	default boolean canBeExplicitlyCastTo(TypeInfo type) {
		return canBeImplicitlyCastTo(type);
	}

	default boolean canHaveVariable() {
		return sizeof() != 0;
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

		@Override
		public Expression getDefaultValue() {
			return new NumericLiteralExpression(BigInteger.valueOf(0L),this,null);
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
	class PrimitiveTypeInfo extends AbstractSymbolInfo implements TypeInfo {

		private final Set<PrimitiveTypeInfo> implicitCast = new HashSet<>();
		void addImplicitCast(PrimitiveTypeInfo... types) {
			Collections.addAll(implicitCast,types);
		}

		@Override
		public boolean canBeImplicitlyCastTo(TypeInfo type) {
			return TypeInfo.super.canBeImplicitlyCastTo(type) || (type instanceof PrimitiveTypeInfo && implicitCast.contains(type));
		}

		private final Set<PrimitiveTypeInfo> explicitCast = new HashSet<>();
		void addExplicitCast(PrimitiveTypeInfo... types) {
			Collections.addAll(explicitCast,types);
		}

		@Override
		public boolean canBeExplicitlyCastTo(TypeInfo type) {
			if (type instanceof TypePointerInfo) {
				return this.canBeExplicitlyCastTo(PrimitiveTypeInfo.ULONG);
			}
			return TypeInfo.super.canBeExplicitlyCastTo(type) || (type instanceof PrimitiveTypeInfo && explicitCast.contains(type));
		}

		@Override
		public String toString() {
			return getCanonicalName();
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
		public static final NumericPrimitiveTypeInfo            HALF = new NumericPrimitiveTypeInfo("half",2);
		public static final NumericPrimitiveTypeInfo            FLOAT = new NumericPrimitiveTypeInfo("float",4);
		public static final NumericPrimitiveTypeInfo            DOUBLE = new NumericPrimitiveTypeInfo("double",8);

		public static final CharPrimitiveTypeInfo               CHAR8 = new CharPrimitiveTypeInfo("char8",1);
		public static final CharPrimitiveTypeInfo               CHAR = new CharPrimitiveTypeInfo("char",2);
		public static final CharPrimitiveTypeInfo               CHAR32 = new CharPrimitiveTypeInfo("char32",4);

		static {
			BYTE.addImplicitCast(SHORT,USHORT,INT,UINT,LONG,ULONG,FLOAT,DOUBLE);
			UBYTE.addImplicitCast(SHORT,USHORT,INT,UINT,LONG,ULONG,FLOAT,DOUBLE);
			SHORT.addImplicitCast(INT,UINT,LONG,ULONG,FLOAT,DOUBLE);
			USHORT.addImplicitCast(INT,UINT,LONG,ULONG,FLOAT,DOUBLE);
			INT.addImplicitCast(LONG,ULONG,FLOAT,DOUBLE);
			UINT.addImplicitCast(LONG,ULONG,FLOAT,DOUBLE);
			LONG.addImplicitCast(DOUBLE);
			ULONG.addImplicitCast(DOUBLE);
			FLOAT.addImplicitCast(DOUBLE);

			BYTE.addExplicitCast(UBYTE,CHAR);
			UBYTE.addExplicitCast(BYTE,CHAR);
			SHORT.addExplicitCast(BYTE,UBYTE,USHORT,CHAR);
			USHORT.addExplicitCast(BYTE,UBYTE,SHORT,CHAR);
			INT.addExplicitCast(BYTE,UBYTE,SHORT,USHORT,UINT,CHAR);
			UINT.addExplicitCast(BYTE,UBYTE,SHORT,USHORT,INT,CHAR);
			LONG.addExplicitCast(BYTE,UBYTE,SHORT,USHORT,INT,UINT,ULONG,CHAR);
			ULONG.addExplicitCast(BYTE,UBYTE,SHORT,USHORT,INT,UINT,LONG,CHAR);
			FLOAT.addExplicitCast(BYTE,UBYTE,SHORT,USHORT,INT,UINT,LONG,ULONG,CHAR);
			DOUBLE.addExplicitCast(BYTE,UBYTE,SHORT,USHORT,INT,UINT,LONG,ULONG,FLOAT,CHAR);

			CHAR.addExplicitCast(BYTE,UBYTE,SHORT,USHORT,INT,UINT,LONG,ULONG);
			CHAR8.addExplicitCast(BYTE,UBYTE,SHORT,USHORT,INT,UINT,LONG,ULONG);
			CHAR32.addExplicitCast(BYTE,UBYTE,SHORT,USHORT,INT,UINT,LONG,ULONG);
		}
		public static void forEachValued(Consumer<PrimitiveTypeInfo> action) {
			forEachNumeric(action);
			action.accept(BOOL);
			action.accept(CHAR);
		}
		public static void forEachNumeric(Consumer<PrimitiveTypeInfo> action) {
			forEachInteger(action);
			action.accept(FLOAT);
			action.accept(DOUBLE);
		}
		public static void forEachInteger(Consumer<PrimitiveTypeInfo> action) {
			action.accept(BYTE);
			action.accept(UBYTE);
			action.accept(SHORT);
			action.accept(USHORT);
			action.accept(INT);
			action.accept(UINT);
			action.accept(LONG);
			action.accept(ULONG);
		}

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
			throw new UnsupportedOperationException(this.getName());
		}
	}
}
