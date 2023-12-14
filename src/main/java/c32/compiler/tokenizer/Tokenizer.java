package c32.compiler.tokenizer;

import java.util.Collection;

public interface Tokenizer {

	Collection<Token> tokenize(String source);
}
