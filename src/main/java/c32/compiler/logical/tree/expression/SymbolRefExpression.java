package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.logical.tree.SymbolInfo;

public abstract class SymbolRefExpression implements Expression {
	private final SymbolInfo symbol;
	private final Location location;

	SymbolRefExpression(SymbolInfo symbol, Location location) {
		this.symbol = symbol;
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	public SymbolInfo get() {
		return symbol;
	}
}
