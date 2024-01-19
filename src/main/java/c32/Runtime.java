package c32;

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

	public static void pointer_println(long ptr) {
		System.out.println(Long.toHexString(ptr));
	}
	public static void pointer_print(long ptr) {
		System.out.print(Long.toHexString(ptr));
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
