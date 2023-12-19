package c32.compiler.parser.ast.expr;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class UnaryPostfixExprTree implements ExprTree {
	private final ExprTree expression;
	private final Token operator;

	@Override
	public Location getLocation() {
		return Location.between(expression.getLocation(), operator.location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.set("expression",expression.toJson(mapper));
		node.put("operator",operator.text);
		return node;
	}
}
