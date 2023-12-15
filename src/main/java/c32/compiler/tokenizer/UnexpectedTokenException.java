package c32.compiler.tokenizer;


import c32.compiler.CompilerException;

public class UnexpectedTokenException extends CompilerException {
	private final Token token;

	public Token getToken() {
		return token;
	}

	public UnexpectedTokenException(Token token, String msg, Exception e) {
		super(token.toString() + " ("+msg+")",e);
		this.token = token;
	}
	public UnexpectedTokenException(Token token, char expected) {
		this(token,"'" + expected + "' expected");
	}
	public UnexpectedTokenException(Token token, String msg) {
		super(token + " ("+msg+")");
		this.token = token;
	}
	public UnexpectedTokenException(Token token) {
		super(String.valueOf(token));
		this.token = token;
	}
}
