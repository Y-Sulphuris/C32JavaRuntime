package c32.compiler.lexer.tokenizer;

public enum TokenType {
	EOF,
	ERROR,
	IDENTIFIER,
	KEYWORD,
	COMMENT,
	DIRECTIVE,
	STRING,
	CHARS,
	NUMBER,
	OPENROUND,
	CLOSEROUND,
	OPEN,
	CLOSE,
	OPENSQUARE,
	CLOSESQUARE,
	ENDLINE,
	OPERATOR,
}