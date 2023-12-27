package c32.compiler.logical;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.SpaceInfo;
import c32.compiler.logical.tree.SymbolInfo;
import c32.compiler.parser.ast.type.TypeElementTree;

public class CompilerAccessException extends CompilerException {
	public CompilerAccessException(Location location, SpaceInfo caller, SymbolInfo type) {
		super(location,"cannot access " + type + " from " + caller);
	}
}
