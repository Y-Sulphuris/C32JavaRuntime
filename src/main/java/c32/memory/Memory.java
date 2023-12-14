package c32.memory;

import lombok.SneakyThrows;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Memory {
	private Memory() throws InstantiationException {
		throw new InstantiationException();
	}


	//region #unsafe bindings

	private static final Object unsafe = getUnsafe();
	private static Object getUnsafe() {
		try {
			final Field[] fields = sun.misc.Unsafe.class.getDeclaredFields();

			for ( Field field : fields ) {
				if ( !field.getType().equals(sun.misc.Unsafe.class) )
					continue;

				final int modifiers = field.getModifiers();
				if ( !(Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) )
					continue;

				//field found successful
				field.setAccessible(true);

				return field.get(null);
			}
			return null;
		} catch (Throwable e) {
			return null;
		}
	}

	public static boolean SunMiscUnsafeExist() {
		return unsafe != null;
	}


	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

	private static final MethodHandle getByte   = findSunUnsafeMethod("getByte", byte.class, long.class);
	private static final MethodHandle putByte   = findSunUnsafeMethod("putByte", void.class, long.class, byte.class);
	private static final MethodHandle getShort  = findSunUnsafeMethod("getShort", short.class, long.class);
	private static final MethodHandle putShort  = findSunUnsafeMethod("putShort", void.class, long.class, short.class);
	private static final MethodHandle getInt    = findSunUnsafeMethod("getInt", int.class, long.class);
	private static final MethodHandle putInt    = findSunUnsafeMethod("putInt", void.class, long.class, int.class);
	private static final MethodHandle getLong   = findSunUnsafeMethod("getLong", long.class, long.class);
	private static final MethodHandle putLong   = findSunUnsafeMethod("putLong", void.class, long.class, long.class);

	private static final MethodHandle allocateInstance  = findSunUnsafeMethod("allocateInstance",Object.class,Class.class);

	private static final MethodHandle objectFieldOffset = findSunUnsafeMethod("objectFieldOffset",long.class,Field.class);

	private static final MethodHandle getRefByte    = findSunUnsafeRefMethod("getByte", byte.class, Object.class, long.class);
	private static final MethodHandle putRefByte    = findSunUnsafeRefMethod("putByte", void.class, Object.class, long.class, byte.class);
	private static final MethodHandle getRefBoolean = findSunUnsafeRefMethod("getBoolean", boolean.class, Object.class, long.class);
	private static final MethodHandle putRefBoolean = findSunUnsafeRefMethod("putBoolean", void.class, Object.class, long.class, boolean.class);
	private static final MethodHandle getRefShort   = findSunUnsafeRefMethod("getShort", short.class, Object.class, long.class);
	private static final MethodHandle putRefShort   = findSunUnsafeRefMethod("putShort", void.class, Object.class, long.class, short.class);
	private static final MethodHandle getRefChar    = findSunUnsafeRefMethod("getChar", char.class, Object.class, long.class);
	private static final MethodHandle putRefChar    = findSunUnsafeRefMethod("putChar", void.class, Object.class, long.class, char.class);
	private static final MethodHandle getRefInt     = findSunUnsafeRefMethod("getInt", int.class, Object.class, long.class);
	private static final MethodHandle putRefInt     = findSunUnsafeRefMethod("putInt", void.class, Object.class, long.class, int.class);
	private static final MethodHandle getRefFloat   = findSunUnsafeRefMethod("getFloat", float.class, Object.class, long.class);
	private static final MethodHandle putRefFloat   = findSunUnsafeRefMethod("putFloat", void.class, Object.class, long.class, float.class);
	private static final MethodHandle getRefLong    = findSunUnsafeRefMethod("getLong", long.class, Object.class, long.class);
	private static final MethodHandle putRefLong    = findSunUnsafeRefMethod("putLong", void.class, Object.class, long.class, long.class);
	private static final MethodHandle getRefDouble  = findSunUnsafeRefMethod("getDouble", double.class, Object.class, long.class);
	private static final MethodHandle putRefDouble  = findSunUnsafeRefMethod("putDouble", void.class, Object.class, long.class, double.class);
	private static final MethodHandle getRefObject  = findSunUnsafeRefMethod("getObject",Object.class,Object.class, long.class);
	private static final MethodHandle putRefObject  = findSunUnsafeRefMethod("putObject",void.class, Object.class, long.class, Object.class);

	@SneakyThrows
	private static MethodHandle findSunUnsafeRefMethod(String methodName, Class<?> returnType, Class<?>... args) {
		try {
			return findMethod(methodName,unsafe,returnType,args);
		} catch (Throwable e) {
			return lookup.findStatic(Memory.class,methodName
					.replace("get","getRef")
					.replace("put","putRef")+"0", MethodType.methodType(returnType,args));
		}
	}
	@SneakyThrows
	private static MethodHandle findSunUnsafeMethod(String methodName, String altName, Class<?> returnType, Class<?>... args) {
		try {
			return findMethod(methodName,unsafe,returnType,args);
		} catch (Throwable e) {
			return lookup.findStatic(Memory.class,altName,MethodType.methodType(returnType,args));
		}
	}
	@SneakyThrows
	private static MethodHandle findSunUnsafeMethod(String methodName, Class<?> returnType, Class<?>... args) {
		try {
			return findMethod(methodName,unsafe,returnType,args);
		} catch (Throwable e) {
			//System.err.println(methodName + "0");
			return lookup.findStatic(Memory.class,methodName+"0",MethodType.methodType(returnType,args));
		}
	}
	private static MethodHandle findMethod(String methodName, Object target, Class<?> returnType, Class<?>... args) throws Throwable {
		return lookup.bind(target,methodName, MethodType.methodType(returnType,args));
	}

	//endregion


	//region #pointers


	/**
	 * Allocates a block of memory of the specified size.
	 *
	 * @param size the size of the memory block to allocate, in bytes
	 * @return a pointer to the allocated memory block
	 */
	public static native long malloc(long size);

	/**
	 * Allocates a block of memory of the specified elements.
	 *
	 * @param numOfElements amount of elements to allocate
	 * @param elementSize the size of single element to allocate, in bytes
	 * @return a pointer to the allocated memory block
	 */
	public static native long calloc(long numOfElements, long elementSize);

	/**
	 * Releases the memory allocated by {@link Memory#malloc(long)}.
	 *
	 * @param ptr the pointer to the memory to be freed
	 * @see #realloc(long, long)
	 * @see #malloc(long)
	 */
	public static native void free(long ptr);

	/**
	 * Reallocates the memory block pointed to by the specified pointer {@code ptr} that allocated by {@link Memory#malloc(long)}.
	 *
	 * @param ptr  the pointer to the memory block to be reallocated
	 * @param size the new size of the memory block, in bytes
	 * @return the pointer to the reallocated memory block
	 */
	public static native long realloc(long ptr, long size);

	@SneakyThrows
	public static byte getByte(long ptr) {
		return (byte) getByte.invokeExact(ptr);
	} private static native byte getByte0(long ptr);

	@SneakyThrows
	public static void putByte(long ptr, byte b) {
		putByte.invokeExact(ptr,b);
	} private static native void putByte0(long ptr, byte b);

	@SneakyThrows
	public static short getShort(long ptr) {
		return (short) getShort.invokeExact(ptr);
	} private static native short getShort0(long ptr);

	@SneakyThrows
	public static void putShort(long ptr, short s) {
		putShort.invokeExact(ptr,s);
	} private static native void putShort0(long ptr, short s);

	@SneakyThrows
	public static int getInt(long ptr) {
		return (int) getInt.invokeExact(ptr);
	} private static native int getInt0(long ptr);

	@SneakyThrows
	public static void putInt(long ptr, int i) {
		putInt.invokeExact(ptr,i);
	} private static native void putInt0(long ptr, int i);

	@SneakyThrows
	public static long getLong(long ptr) {
		return (long) getLong.invokeExact(ptr);
	} private static native long getLong0(long ptr);

	@SneakyThrows
	public static void putLong(long ptr, long l) {
		putLong.invokeExact(ptr,l);
	} private static native void putLong0(long ptr, long l);

	public static native long news(long size);
	public static native void delete(long ptr);


	@SneakyThrows
	public static long objectFieldOffset(Field f) {
		return (long) objectFieldOffset.invokeExact(f);
	} private static native long objectFieldOffset0(Field f);


	@SneakyThrows
	public static byte getByte(Object ref, long fieldOffset) {
		return (byte) getRefByte.invokeExact(ref,fieldOffset);
	} private static native byte getRefByte0(Object ref, long fieldOffset);

	@SneakyThrows
	public static void putByte(Object ref, long fieldOffset, byte b) {
		putRefByte.invokeExact(ref,fieldOffset,b);
	} private static native void putRefByte0(Object ref, long fieldOffset, byte b);

	@SneakyThrows
	public static boolean getBoolean(Object ref, long fieldOffset) {
		return (boolean) getRefBoolean.invokeExact(ref,fieldOffset);
	} private static native boolean getRefBoolean0(Object ref, long fieldOffset);

	@SneakyThrows
	public static void putBoolean(Object ref, long fieldOffset, boolean b) {
		putRefBoolean.invokeExact(ref,fieldOffset,b);
	} private static native void putRefBoolean0(Object ref, long fieldOffset, boolean b);


	@SneakyThrows
	public static short getShort(Object ref, long fieldOffset) {
		return (short) getRefShort.invokeExact(ref,fieldOffset);
	} private static native short getRefShort0(Object ref, long fieldOffset);

	@SneakyThrows
	public static void putShort(Object ref, long fieldOffset, short s) {
		putRefShort.invokeExact(ref,fieldOffset,s);
	} private static native void putRefShort0(Object ref, long fieldOffset, short s);

	@SneakyThrows
	public static char getChar(Object ref, long fieldOffset) {
		return (char) getRefChar.invokeExact(ref,fieldOffset);
	} private static native char getRefChar0(Object ref, long fieldOffset);

	@SneakyThrows
	public static void putChar(Object ref, long fieldOffset, char s) {
		putRefChar.invokeExact(ref,fieldOffset,s);
	} private static native void putRefChar0(Object ref, long fieldOffset, char s);


	@SneakyThrows
	public static int getInt(Object ref, long fieldOffset) {
		return (int) getRefInt.invokeExact(ref,fieldOffset);
	} private static native int getRefInt0(Object ref, long fieldOffset);

	@SneakyThrows
	public static void putInt(Object ref, long fieldOffset, int i) {
		putRefInt.invokeExact(ref,fieldOffset,i);
	} private static native void putRefInt0(Object ref, long fieldOffset, int i);

	@SneakyThrows
	public static float getFloat(Object ref, long fieldOffset) {
		return (float) getRefFloat.invokeExact(ref,fieldOffset);
	} private static native float getRefFloat0(Object ref, long fieldOffset);

	@SneakyThrows
	public static void putFloat(Object ref, long fieldOffset, float i) {
		putRefFloat.invokeExact(ref,fieldOffset,i);
	} private static native void putRefFloat0(Object ref, long fieldOffset, float i);


	@SneakyThrows
	public static long getLong(Object ref, long fieldOffset) {
		return (long) getRefLong.invokeExact(ref,fieldOffset);
	} private static native long getRefLong0(Object ref, long fieldOffset);

	@SneakyThrows
	public static void putLong(Object ref, long fieldOffset, long l) {
		putRefLong.invokeExact(ref,fieldOffset,l);
	} private static native void putRefLong0(Object ref, long fieldOffset, long l);

	@SneakyThrows
	public static double getDouble(Object ref, long fieldOffset) {
		return (double) getRefDouble.invokeExact(ref,fieldOffset);
	} private static native double getRefDouble0(Object ref, long fieldOffset);

	@SneakyThrows
	public static void putDouble(Object ref, long fieldOffset, double l) {
		putRefDouble.invokeExact(ref,fieldOffset,l);
	} private static native void putRefDouble0(Object ref, long fieldOffset, double l);


	@SneakyThrows
	public static Object getObject(Object ref, long fieldOffset) {
		return getRefObject.invokeExact(ref,fieldOffset);
	} public static native Object getRefObject0(Object ref, long fieldOffset);

	@SneakyThrows
	public static void putObject(Object ref, long fieldOffset, Object o) {
		putRefObject.invokeExact(ref,fieldOffset,o);
	} public static native void putRefObject0(Object ref, long fieldOffset, Object o);


	//endregion


	//region #memory manipulation

	/**
	 * Copies a block of memory from a source address to a destination address.
	 *
	 * @param _Dst The destination address to copy the memory to.
	 * @param _Src The source address to copy the memory from.
	 * @param _MaxCount The maximum number of bytes to copy.
	 * @return The destination address.
	 */
	@SneakyThrows
	public static long memcpy(long _Dst, long _Src, long _MaxCount) {
		copyMemory.invokeExact(_Dst,_Src,_MaxCount);
		return _Dst;
	}
	/**
	 * Copies a block of memory from a source to a destination.
	 *
	 * @param _DstBase the base object of the destination memory block
	 * @param _Dst the starting address of the destination memory block
	 * @param _SrcBase the base object of the source memory block
	 * @param _Src the starting address of the source memory block
	 * @param _MaxCount the number of bytes to be copied
	 *
	 * @return the starting address of the destination memory block
	 */
	@SneakyThrows
	public static long memcpy(Object _DstBase, long _Dst, Object _SrcBase, long _Src, long _MaxCount) {
		copyRefMemory.invokeExact(_DstBase,_Dst,_SrcBase,_Src,_MaxCount);
		return _Dst;
	}

	/**
	 * This method is used to copy a block of memory from one location to another.
	 *
	 * @param _Dst The destination memory address where the block of memory will be copied to.
	 * @param _Src The source memory address from where the block of memory will be copied from.
	 * @param _MaxCount The maximum number of bytes to be copied.
	 *
	 * @return The memory address of the destination after the copying is completed.
	 */
	public static native long memmove(long _Dst,long _Src,long _MaxCount);

	public static native long memccpy(long _Dst,long _Src,byte _Val,long _Size);

	public static native int memcmp(long _Buf1,long _Buf2,long _Size);

	@SneakyThrows
	public static long memset(long _Dst, byte _Val, long _Size) {
		setMemory.invokeExact(_Dst,_Size,_Val);
		return _Dst;
	}
	@SneakyThrows
	public static long memset(Object _DstBase, long _Dst, byte _Val, long _Size) {
		setRefMemory.invokeExact(_DstBase, _Dst,_Size,_Val);
		return _Dst;
	}

	private static final MethodHandle copyMemory    = findSunUnsafeMethod("copyMemory",void.class, long.class, long.class, long.class);
	private static final MethodHandle copyRefMemory    = findSunUnsafeMethod("copyMemory","copyMemoryRef0l",void.class,Object.class, long.class, Object.class, long.class,long.class);

	private static final MethodHandle setMemory    = findSunUnsafeMethod("setMemory",void.class, long.class, long.class, byte.class);
	private static final MethodHandle setRefMemory    = findSunUnsafeMethod("setMemory","setRefMemory0l",void.class,Object.class, long.class, long.class, byte.class);

	private static final MethodHandle getAddress    = findSunUnsafeMethod("getAddress",long.class,long.class);
	private static final MethodHandle putAddress    = findSunUnsafeMethod("putAddress",void.class,long.class,long.class);

	private static native void copyMemory0(long _Dst,long _Src,long _MaxCount);
	private static native void copyMemoryRef0(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes, int[] lock);
	private static void copyMemoryRef0l(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) {
		copyMemoryRef0(srcBase, srcOffset, destBase, destOffset, bytes,__lock);
	}

	private static native void setMemory0(long address, long bytes, byte value);
	private static native void setRefMemory0(Object o, long offset, long bytes, byte value, int[] lock);
	private static void setRefMemory0l(Object o, long offset, long bytes, byte value) {
		setRefMemory0(o,offset,bytes,value,__lock);
	}

	private static final int[] __lock = {};

	public static native int pageSize();
	public static native int addressSize();
	public static final int NATIVE_ADDRESS_SIZE = addressSize();

	@SneakyThrows
	public static long getAddress(long address) {
		return (long) getAddress.invokeExact(address);
	} private static native long getAddress0(long address);

	@SneakyThrows
	public static void putAddress(long address, long a) {
		putAddress.invokeExact(address,a);
	} private static native void putAddress0(long address, long x);

	//endregion
}
