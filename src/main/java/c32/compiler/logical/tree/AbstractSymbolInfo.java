package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;

@Getter
public abstract class AbstractSymbolInfo implements SymbolInfo {
	private final Collection<Weak<Expression>> usages = new LinkedList<>();
}
