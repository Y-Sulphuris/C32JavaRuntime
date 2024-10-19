package c32.compiler.lexer.tokenizer;

import java.io.File;
import java.util.*;

public class ConfigurableTokenizer implements Tokenizer {

	private final Set<String> keywords = new HashSet<>();

	public ConfigurableTokenizer addKeywords(String... words) {
		Collections.addAll(keywords, words);
		return this;
	}

	public ConfigurableTokenizer addKeywords(Collection<String> words) {
		keywords.addAll(words);
		return this;
	}

	private final Set<String> operators = new HashSet<>();

	public ConfigurableTokenizer addOperators(String... operators) {
		Collections.addAll(this.operators, operators);
		return this;
	}

	private char endLine = ';';

	public ConfigurableTokenizer setEndLine(char endLine) {
		this.endLine = endLine;
		return this;
	}

	private static final String numeric = "0123456789";
	private static final String numericHex = numeric + "abcdef";

	public ConfigurableTokenizer() {
	}

	private File file;

	private String source = null;
	private int pos = 0;
	private int line = 1;

	private Exception exception = null;

	@Override
	public synchronized Stack<Token> tokenize(String source, File file) {
		this.source = source;
		this.file = file;
		pos = 0;
		line = 1;
		exception = null;

		Stack<Token> tokens = new Stack<>();
		Token token = nextToken();
		try {
			tokens.push(token);
			while (token.type != TokenType.EOF) {
				tokens.push(token = nextToken());
				if (token.type == TokenType.ERROR)
					throw new UnexpectedTokenException(token, "Invalid token", exception);
			}
		} catch (UnexpectedTokenException e) {
			Token errorToken = e.getToken();
			if (errorToken != null) {
				if (errorToken.text != null) System.err.println(errorToken.text);
				String str = source.substring(errorToken.location.getStartPos(), errorToken.location.getEndPos());
				System.err.println("Invalid token at line: " + errorToken.location.getStartLine());
				System.err.println(str);
				for (int i = 0; i < str.length(); i++) System.err.print('~');
				System.err.println();
			}
			throw e;
		}

		return tokens;
	}

	private Token nextToken() {
		char ch = nextChar();
		if (ch == endLine) return new Token(TokenType.ENDLINE, String.valueOf(endLine), pos - 1, pos, line, file);

		//skip
		while (ch == ' ' || ch == '\n' || ch == '\t') {
			if (ch == '\n') line++;
			ch = nextChar();
		}

		if (ch == '\0') return new Token(TokenType.EOF, pos - 1, pos, line, file);

		//comments
		if (ch == '/') {
			if (seeNextChar() == '/') {
				ch = nextChar();
				int startpos = pos;
				ch = nextChar();
				StringBuilder builder = new StringBuilder();
				while (ch != '\n') {
					builder.append(ch);
					ch = nextChar();
				}
				pos--;
				return new Token(TokenType.COMMENT, builder.toString(), startpos, pos, line, file);
			} else if (seeNextChar() == '*') {
				int startpos = pos;
				ch = nextChar();
				StringBuilder builder = new StringBuilder();
				while (!(ch == '*' && seeNextChar() == '/')) {
					if (ch == '\n') line++;
					builder.append(ch);
					ch = nextChar();
				}
				pos++;
				return new Token(TokenType.COMMENT, builder.toString(), startpos, pos, line, file);
			} else {
				//exception = new Exception();
				//return new Token(TokenType.ERROR,pos-1,pos,line);
			}
		}


		//string literals
		Token stringToken = readLiteral('"', ch, TokenType.STRING);
		if (stringToken != null) return stringToken;


		//char literals
		Token charsToken = readLiteral('\'', ch, TokenType.CHARS);
		if (charsToken != null) return charsToken;


		//operators
		if (isOperatorChar(ch)) {
			int startpos = pos - 1;
			StringBuilder builder = new StringBuilder();
			String operator = "";
			while (isOperatorChar(ch)) {
				builder.append(ch);
				if (operators.contains(builder.toString())) {
					operator = builder.toString();
				} else {
					pos--;
					if (!operator.isEmpty()) return new Token(TokenType.OPERATOR, operator, startpos, pos, line, file);
					else return new Token(TokenType.ERROR, operator, startpos, pos, line, file);
				}
				ch = nextChar();
			}
			pos--;
			return new Token(TokenType.OPERATOR, operator, startpos, pos, line, file);
		}

		//numbers
		if (isNumeric(ch)) {
			int startpos = pos - 1;
			StringBuilder builder = new StringBuilder();
			while (isValidNameChar(ch)) {
				builder.append(ch);
				ch = nextChar();
			}
			pos--;
			return new Token(TokenType.NUMBER, builder.toString(), startpos, pos, line, file);
		}

		//text/keywords
		if (isValidNameChar(ch)) {
			int startpos = pos - 1;
			StringBuilder builder = new StringBuilder();
			while (isValidNameChar(ch)) {
				builder.append(ch);
				ch = nextChar();
			}
			pos--;
			String value = builder.toString();
			return new Token(isKeyword(value) ? TokenType.KEYWORD : TokenType.IDENTIFIER, value, startpos, pos, line, file);
		}
		if (ch == '#') {
			int startpos = pos - 1;
			ch = nextChar();
			StringBuilder builder = new StringBuilder();
			while (isValidNameChar(ch)) {
				builder.append(ch);
				ch = nextChar();
			}
			pos--;
			String value = builder.toString();
			return new Token(TokenType.DIRECTIVE, value, startpos, pos, line, file);
		}

		TokenType type;
		switch (ch) {
			case '(':
				type = TokenType.OPENROUND;
				break;
			case ')':
				type = TokenType.CLOSEROUND;
				break;
			case '[':
				type = TokenType.OPENSQUARE;
				break;
			case ']':
				type = TokenType.CLOSESQUARE;
				break;
			case '{':
				type = TokenType.OPEN;
				break;
			case '}':
				type = TokenType.CLOSE;
				break;
			default:
				type = TokenType.ERROR;
				exception = new Exception();
		}
		return new Token(type, String.valueOf(ch), pos - 1, pos, line, file);
	}

	private Token readLiteral(char separators, char ch, TokenType type) {
		if (ch == separators) {
			int startpos = pos;
			ch = nextChar();
			StringBuilder builder = new StringBuilder();
			while (ch != separators) {
				if (ch == '\\') {
					ch = nextChar();
					switch (ch) {
						case '\\':
							builder.append('\\');
							break;
						case '\'':
							builder.append('\'');
							break;
						case '"':
							builder.append('"');
							break;
						case 'n':
							builder.append('\n');
							break;
						case 't':
							builder.append('\t');
							break;
						case 'u': {
							char character = (char) Integer.parseInt(String.valueOf(nextChar()) + nextChar() + nextChar() + nextChar());
							builder.append(character);
							break;
						}
						default: {
							if (numeric.contains(String.valueOf(ch))) {
								char character = (char) Integer.parseInt(String.valueOf(ch) + nextChar());
								builder.append(character);
								break;
							}
						}
					}
				} else {
					if (ch == '\n')
						throw new UnexpectedTokenException(new Token(type, builder.toString(), startpos - 1, pos, line, file), "Unexpected '\\n'");
					builder.append(ch);
				}
				ch = nextChar();
			}
			return new Token(type, builder.toString(), startpos - 1, pos, line, file);
		}
		return null;
	}

	private char nextChar() {
		if (pos >= source.length()) return '\0';
		return source.charAt(pos++);
	}

	private char seeNextChar() {
		if (pos >= source.length()) return '\0';
		return source.charAt(pos);
	}


	private boolean isKeyword(String str) {
		return keywords.contains(str);
	}

	private boolean isOperatorChar(char ch) {
		for (String operator : operators) {
			if (operator.contains(String.valueOf(ch))) return true;
		}
		return false;
	}

	private boolean isNumeric(char ch) {
		return numeric.contains(String.valueOf(ch));
	}

	private boolean isValidNameChar(char ch) {
		return ch == '_' || isNumeric(ch) || Character.isAlphabetic(ch);
	}
}
