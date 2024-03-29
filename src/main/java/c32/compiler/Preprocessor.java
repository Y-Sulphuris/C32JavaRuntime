package c32.compiler;

import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.lexer.tokenizer.TokenType;

import java.util.Collection;
import java.util.Iterator;

public class Preprocessor {/*
	private static final java.lang.ThreadLocal<Integer> $REGISTER$ = new java.lang.ThreadLocal<>();
	public static Integer $REGISTER$GET$() {
		if ($REGISTER$.get() == null) $REGISTER$.set(new Integer(5));
		return $REGISTER$.get();
	}*/

	public static void preprocess(Collection<Token>  tokens) {
		tokens.removeIf(token -> token.type == TokenType.COMMENT);
		ITERATOR:
		for (Iterator<Token> iterator = tokens.iterator(); iterator.hasNext(); ) {
			Token token = iterator.next();
			if (token.type == TokenType.DIRECTIVE) {
				switch (token.text) {
					case "pragma":
					{
						token = iterator.next();
						switch (token.text) {
							case "ignore":
								tokens.clear();
								break ITERATOR;
						}
					} break;
				}
			}
		}
	}
}
