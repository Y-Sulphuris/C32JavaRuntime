package c32.compiler.lexer.tokenizer;

import c32.compiler.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;

public class Token {
	public final TokenType type;
	public final String text;
	public final Location location;

	public Token(TokenType type, String text, int startpos, int endpos, int line, File file) {
		this.type = type;
		this.text = text;
		this.location = new Location(startpos,endpos,line,line,file);
	}
	public Token(TokenType type,int startpos,int endpos, int line, File file) {
		this(type,"\0",startpos,endpos,line,file);
	}

	@Override
	public String toString() {
		return "Token{" + type +
			(text != null ?" = '" + text + '\'' : "")+
			", startpos=" + location.getStartPos() +
			", endpos=" + location.getEndPos() +
			", line=" + location.getStartLine() +
			'}';
	}

	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("text",text);
		node.put("type",type.name());
		node.set("location",location.toJson(mapper));
		return node;
	}
}
