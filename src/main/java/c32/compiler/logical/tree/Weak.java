package c32.compiler.logical.tree;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class Weak<T> extends WeakReference<T> {
	public Weak(T referent) {
		super(referent);
	}

	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	@Override
	public boolean equals(Object obj) {
		return Objects.equals(get(), obj);
	}

	@Override
	public int hashCode() {
		T var = get();
		if (var == null) return 0;
		return var.hashCode();
	}
}