package c32.compiler.parser.ast.statement;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;

@Data
public class BlockStatementTree implements StatementTree {
	private final Token open;
	private final List<StatementTree> statements;
	private final Token close;
	@Override
	public Location getLocation() {
		return Location.between(open.location,close.location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("open",open.text);
		ArrayNode statementsNode = mapper.createArrayNode();
		for(StatementTree statement : statements) {
			statementsNode.add(statement.toJson(mapper));
		}
		node.set("statements",statementsNode);
		node.put("close",close.text);
		return node;
	}
}
