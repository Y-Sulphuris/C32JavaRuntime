package c32;

public final class Unsigned {
	private Unsigned() {}

	public static void println(byte x) {
		System.out.println(Integer.toUnsignedString(x));
	}

	public static void println(short x) {
		System.out.println(Integer.toUnsignedString(x));
	}

	public static void println(int x) {
		System.out.println(Integer.toUnsignedString(x));
	}

	public static void println(long x) {
		System.out.println(Long.toUnsignedString(x));
	}

}
