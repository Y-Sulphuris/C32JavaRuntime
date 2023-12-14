package c32.compiler.tokenizer;

public class Token {
	public final TokenType type;
	public final String text;
	public final int startpos, endpos;
	public final int line;

	public Token(TokenType type, String text, int startpos, int endpos, int line) {
		this.type = type;
		this.text = text;
		this.startpos = startpos;
		this.endpos = endpos;
		this.line = line;
	}
	public Token(TokenType type,int startpos,int endpos, int line) {
		this(type,"\0",startpos,endpos,line);
	}

	@Override
	public String toString() {
		return "Token{" + type +
			(text != null ?" = '" + text + '\'' : "")+
			", startpos=" + startpos +
			", endpos=" + endpos +
			", line=" + line +
			'}';
	}
}
