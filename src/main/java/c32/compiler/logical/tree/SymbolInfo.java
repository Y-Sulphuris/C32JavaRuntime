package c32.compiler.logical.tree;

public interface SymbolInfo {
	String getName();

	boolean isAccessibleFrom(SpaceInfo space);
    default String getCanonicalName() {
        return getName();
    }
}
