package c32.extern;

import com.IOUtils;
import com.natives.NativesInit;

public final class Runtime {
	private Runtime() throws InstantiationException {
		throw new InstantiationException();
	}
	private static boolean inited = false;
	static {
		initNatives();
	}

	public static void initNatives() {
		if (inited) return;
		String libName = null;
		switch (NativesInit.getOs()) {
			case NativesInit.WINDOWS_NAME:
				if (NativesInit.is64bitOs()) {
					libName = ("c32rt_64.dll");
				} else {
					libName = ("c32rt.dll");
				}
				break;
			case NativesInit.LINUX_NAME:
				if (NativesInit.is64bitOs()) {
					libName = ("c32rt_64.so");
				} else {
					libName = ("c32rt.so");
				}
				break;
			case NativesInit.MACOS_NAME:
				libName = ("c32rt.dylib");//todo
				break;
		}
		if (libName != null)
			NativesInit.addLibrary(libName);
		NativesInit.extractNatives("native","c32/native");
		System.load(IOUtils.toUserhome_path("c32/native/"+NativesInit.getOs()) + libName);
		inited = true;
	}

	public static void pprintln(long ptr) {
		System.out.println(Long.toHexString(ptr));
	}

	public static void pprint(long ptr) {
		System.out.print(Long.toHexString(ptr));
	}

	public static void println(boolean b) {
		System.out.println(b);
	} public static void print(boolean b) {
		System.out.print(b);
	}
	public static void println(byte b) {
		System.out.println(b);
	} public static void print(byte b) {
		System.out.print(b);
	}
	public static void println(short s) {
		System.out.println(s);
	} public static void print(short s) {
		System.out.print(s);
	}
	public static void println(int i) {
		System.out.println(i);
	} public static void print(int i) {
		System.out.print(i);
	}
	public static void println(long l) {
		System.out.println(l);
	} public static void print(long l) {
		System.out.print(l);
	}
	public static void println(char ch) {
		System.out.println(ch);
	} public static void print(char ch) {
		System.out.print(ch);
	}
	public static void println8(byte ch) {
		System.out.println((char)Byte.toUnsignedInt(ch));
	} public static void print8(byte ch) {
		System.out.print((char)Byte.toUnsignedInt(ch));
	}


	public static void uprintln(byte x) {
		System.out.println(Byte.toUnsignedInt(x));
	}

	public static void uprintln(short x) {
		System.out.println(Short.toUnsignedInt(x));
	}

	public static void uprintln(int x) {
		System.out.println(Integer.toUnsignedString(x));
	}

	public static void uprintln(long x) {
		System.out.println(Long.toUnsignedString(x));
	}


	public static void uprint(byte x) {
		System.out.print(Byte.toUnsignedInt(x));
	}

	public static void uprint(short x) {
		System.out.print(Short.toUnsignedInt(x));
	}

	public static void uprint(int x) {
		System.out.print(Integer.toUnsignedString(x));
	}

	public static void uprint(long x) {
		System.out.print(Long.toUnsignedString(x));
	}



	public static long I2UL(int i) {
		return Integer.toUnsignedLong(i);
	}
	/*private static final long String_value_offset;
	static {
		try {
			String_value_offset = Memory.objectFieldOffset(String.class.getDeclaredField("value"));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}


	public static String makeUnsafeString(char[] ch) {
		String str = new String();
		Memory.putObject(str,String_value_offset,ch);
		return str;
	}

	public static void printf(char[] ch, Object... args) {

	}*/
}
