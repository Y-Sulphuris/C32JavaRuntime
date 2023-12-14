package c32;

import c32.memory.Memory;
import com.natives.NativesInit;

public final class Runtime {
	private Runtime() throws InstantiationException {
		throw new InstantiationException();
	}
	static {
		initNatives();
	}

	public static void initNatives() {
		switch (NativesInit.getOs()) {
			case NativesInit.WINDOWS_NAME:
				if (NativesInit.is64bitOs()) {
					NativesInit.addLibrary("c32rt_64.dll");
				} else {
					NativesInit.addLibrary("c32rt.dll");
				}
				break;
			case NativesInit.LINUX_NAME:
				if (NativesInit.is64bitOs()) {
					NativesInit.addLibrary("c32rt_64.so");
				} else {
					NativesInit.addLibrary("c32rt.so");
				}
				break;
			case NativesInit.MACOS_NAME:
				NativesInit.addLibrary("c32rt.dylib");//todo
				break;
		}
		NativesInit.extractNatives("native","c32/native");
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
