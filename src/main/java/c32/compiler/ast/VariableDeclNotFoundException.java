package c32.compiler.ast;

import c32.compiler.CompilerException;

public class VariableDeclNotFoundException extends CompilerException {
	public VariableDeclNotFoundException(VariableContainer container, String name) {
		super("'" + name + "' in " + container);
	}
}
