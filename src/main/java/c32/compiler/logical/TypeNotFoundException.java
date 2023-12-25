package c32.compiler.logical;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.SpaceInfo;
import c32.compiler.parser.ast.type.TypeElementTree;

public class TypeNotFoundException extends CompilerException {
	public TypeNotFoundException(SpaceInfo caller, TypeElementTree type) {
		super(type.getLocation(),"cannot found " + type + " from " + caller);
	}
}
