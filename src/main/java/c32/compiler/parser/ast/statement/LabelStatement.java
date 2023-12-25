package c32.compiler.parser.ast.statement;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class LabelStatement implements StatementTree {
	private final Token labelName;
	private final Token colon;

	@Override
	public Location getLocation() {
		return Location.between(labelName.location,colon.location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		final ObjectNode node = mapper.createObjectNode();
		node.put("labelName", labelName.text);
		node.put("colon", colon.text);
		return node;
	}
}
