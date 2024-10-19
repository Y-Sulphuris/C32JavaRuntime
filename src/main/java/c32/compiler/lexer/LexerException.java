package c32.compiler.lexer;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;

public class LexerException extends CompilerException {
	public LexerException(Location location, String message) {
		super(location, message);
	}
}
