package c32.compiler.ast;

import c32.compiler.CompilerException;
import c32.compiler.ast.type.TypeTree;

public class NoUnsignedTypeException extends CompilerException {
	public NoUnsignedTypeException(TypeTree type) {
		super("no unsigned type found: " + type.getName());
	}
}
