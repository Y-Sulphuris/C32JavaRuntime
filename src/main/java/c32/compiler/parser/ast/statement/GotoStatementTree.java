package c32.compiler.parser.ast.statement;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class GotoStatementTree implements StatementTree {
	private final Token keyword;
	private final Token labelName;
	private final Token endLine;

	@Override
	public Location getLocation() {
		return Location.between(keyword.location,endLine.location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("keyword",keyword.text);
		node.put("labelName",labelName.text);
		node.put("endLine",endLine.text);
		return node;
	}
}
