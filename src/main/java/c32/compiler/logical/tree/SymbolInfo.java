package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.Expression;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public interface SymbolInfo {

	Set<Weak<Expression>> getUsages();

	default void addUsage(Expression e) {
		if (e == null) return;
		getUsages().add(new Weak<>(e));
	}

	default void forEachUsage(Consumer<Expression> e) {
		for (Weak<Expression> usage : getUsages()) {
			if (usage.get() == null) {
				getUsages().remove(usage);
				continue;
			}
			e.accept(usage.get());
		}
	}

	default boolean isUnused() {
		return getUsages().isEmpty();
	}

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
