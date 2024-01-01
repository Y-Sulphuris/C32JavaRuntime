package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@Getter
public abstract class AbstractSymbolInfo implements SymbolInfo {
	private final Set<Weak<Expression>> usages = new HashSet<>();
}
