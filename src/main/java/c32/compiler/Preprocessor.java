package c32.compiler;

import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.lexer.tokenizer.TokenType;

import java.util.Collection;

public class Preprocessor {/*
	private static final java.lang.ThreadLocal<Integer> $REGISTER$ = new java.lang.ThreadLocal<>();
	public static Integer $REGISTER$GET$() {
		if ($REGISTER$.get() == null) $REGISTER$.set(new Integer(5));
		return $REGISTER$.get();
	}*/

	public static void preprocess(Collection<Token>  tokens) {
		tokens.removeIf(token -> token.type == TokenType.COMMENT);
	}
}
