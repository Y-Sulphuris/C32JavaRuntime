package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
public abstract class AbstractSymbolInfo implements SymbolInfo {
	private final Set<Weak<Expression>> usages = new HashSet<>();

	@Setter
	private SpaceInfo accessRoot = null;
	@Override
	public boolean isAccessibleFrom(SpaceInfo space) {
		return accessRoot == null || accessRoot.containsOrThisRecursive(space);
	}
}
