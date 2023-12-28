package c32.compiler.logical.tree;

import c32.compiler.except.CompilerException;

public interface NonResolved<T extends SymbolInfo> extends SymbolInfo {
	CompilerException fail();
	default T forceResolve() {
		T info = tryToResolve();
		if (info.equals(this))
			throw fail();
		return info;
	}
	T tryToResolve();

	@Override
	default boolean isAccessibleFrom(SpaceInfo space) {
		return true;
	}
}
