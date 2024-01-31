package c32.extern;

public final class Cast {
    private Cast() throws InstantiationException
        { throw new InstantiationException(); }


    public static long int2ulong(int x) {
        return Integer.toUnsignedLong(x);
    }
}
