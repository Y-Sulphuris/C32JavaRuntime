package c32;

public final class Unsigned {
	private Unsigned() {}

	public static void println(byte x) {
		System.out.println(Byte.toUnsignedInt(x));
	}

	public static void println(short x) {
		System.out.println(Short.toUnsignedInt(x));
	}

	public static void println(int x) {
		System.out.println(Integer.toUnsignedString(x));
	}

	public static void println(long x) {
		System.out.println(Long.toUnsignedString(x));
	}


	public static void print(byte x) {
		System.out.print(Byte.toUnsignedInt(x));
	}

	public static void print(short x) {
		System.out.print(Short.toUnsignedInt(x));
	}

	public static void print(int x) {
		System.out.print(Integer.toUnsignedString(x));
	}

	public static void print(long x) {
		System.out.print(Long.toUnsignedString(x));
	}

}
