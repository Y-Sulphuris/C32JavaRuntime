package c32.compiler.parser.ast.statement;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ExprTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class ThrowStatement implements StatementTree {
	private final Token keyword;
	private final ExprTree throwableExpr;
	private final Token endLine;

	@Override
	public Location getLocation() {
		return Location.between(keyword.location,endLine.location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("keyword",keyword.text);
		node.set("expression",throwableExpr.toJson(mapper));
		node.put("endLine",endLine.text);
		return node;
	}
}
