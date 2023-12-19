package c32.compiler.parser.ast.statement;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ExprTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class ReturnStatementTree implements StatementTree {
	private final Token keyword;
	private final ExprTree expression;
	private final Token endLine;

	@Override
	public Location getLocation() {
		return keyword.location;
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("keyword",keyword.text);
		if (expression != null) node.set("expression",expression.toJson(mapper));
		node.put("endLine",endLine.text);
		return node;
	}
}
