package c32.compiler.logical.tree;

public interface SymbolInfo {
	String getName();

    default String getCanonicalName() {
        return getName();
    }
}
