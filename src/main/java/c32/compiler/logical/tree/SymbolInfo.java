package c32.compiler.logical.tree;

public interface SymbolInfo {
	String getName();

	boolean isAccessibleFrom(SpaceInfo space);

	SpaceInfo getParent();

    default String getCanonicalName() {
        if (getParent() == null || getParent().getName().isEmpty()) return getName();
        return getParent().getCanonicalName() + "." + getName();
    }

    default String getFullName() {
        if (getParent() == null) return getName();
        else return getParent().getFullName() + "$$$" + getName();
    }
}
