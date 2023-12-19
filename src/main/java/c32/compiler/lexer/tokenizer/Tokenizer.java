package c32.compiler.lexer.tokenizer;

import java.util.Collection;

public interface Tokenizer {

	Collection<Token> tokenize(String source);
}
