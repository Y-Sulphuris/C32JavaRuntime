package c32.compiler.lexer.tokenizer;

import java.io.File;
import java.util.Collection;

public interface Tokenizer {

	Collection<Token> tokenize(String source, File file);
}
