package c32.compiler.logical;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.AbstractSpaceInfo;
import c32.compiler.logical.tree.FunctionInfo;
import c32.compiler.logical.tree.SpaceInfo;

public class DuplicatedFunctionException extends CompilerException {
	public DuplicatedFunctionException(FunctionInfo function) {
		super(function.getLocation(),"multiple declaration of '" + function.getFullNameEx());
	}
}
