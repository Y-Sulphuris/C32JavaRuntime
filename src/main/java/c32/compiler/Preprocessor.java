package c32.compiler;

import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.lexer.tokenizer.TokenType;
import c32.compiler.lexer.tokenizer.UnexpectedTokenException;
import lombok.Data;

import java.util.*;

public class Preprocessor {/*
	private static final java.lang.ThreadLocal<Integer> $REGISTER$ = new java.lang.ThreadLocal<>();
	public static Integer $REGISTER$GET$() {
		if ($REGISTER$.get() == null) $REGISTER$.set(new Integer(5));
		return $REGISTER$.get();
	}*/

	private final Stack<Token> newTokens = new Stack<>();
	private final Collection<Token> tokens;
	private Iterator<Token> iterator;
	private Token nextToken() {
		return iterator.next();
	}
	private boolean hasNextToken() {
		return iterator.hasNext();
	}

	private final Map<String, Macro> macros = new HashMap<>();

	public Preprocessor(Collection<Token> tokens) {
		this.tokens = tokens;
	}
	private Stack<Token> preprocess() {
		tokens.removeIf(token -> token.type == TokenType.COMMENT);
		ITERATOR:
		for (iterator = tokens.iterator(); hasNextToken(); ) {
			Token token = nextToken();
			if (token.type == TokenType.DIRECTIVE) {
				switch (token.text) {
					case "pragma":
					{
						token = nextToken();
						switch (token.text) {
							case "ignore":
								return new Stack<>();
						}
					} break;
					case "macro":
					{
						Macro macro = parseMacro();
						System.out.println(macro);
						macros.put(macro.getName().text, macro);
					} break;
				}
			} else if (token.type == TokenType.IDENTIFIER && macros.containsKey(token.text)) {
				token = nextToken();
			} else {
				newTokens.add(token);
			}
		}
		return newTokens;
	}

	private Macro parseMacro() {
		Token name = nextToken();
		if (name.type != TokenType.IDENTIFIER)
			throw new UnexpectedTokenException(name, "macro name expected");

		Collection<Token> macroText = new ArrayList<>();
		Token token = nextToken();

		//noinspection ConditionalBreakInInfiniteLoop
		while (true) {
			if (token.type == TokenType.DIRECTIVE && token.text.equals("unfolds"))
				break;
			macroText.add(token);
			token = nextToken();
			iterator.remove();
		}
		// token = #unfolds there
		Collection<Token> unfolds = new ArrayList<>();
		token = nextToken();

		//noinspection ConditionalBreakInInfiniteLoop
		while (true) {
			if (token.type == TokenType.DIRECTIVE && token.text.equals("end_macro"))
				break;
			unfolds.add(token);
			token = nextToken();
		}

		// token = #end_macro there
		return new Macro(name, macroText, unfolds);
	}





	public static Stack<Token> preprocess(Collection<Token> tokens) {
		return new Preprocessor(tokens).preprocess();
	}

}
@Data
class Macro {
	private final Token name;
	private final Collection<Token> macroText;
	private final Collection<Token> unfolds;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("#macro ").append(name.text).append('\n');
		for (Token token : macroText) {
			builder.append(token.text).append(' ');
		}
		builder.append("\n#unfolds\n");
		for (Token unfold : unfolds) {
			builder.append(unfold.text).append(' ');
		}
		builder.append("\n#end_macro");
		return builder.toString();
	}
}
