package c32.compiler.parser.ast.expr;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ExprTree;
import c32.compiler.parser.ast.expr.LiteralType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class LiteralExprTree implements ExprTree {
	private final Token literal;
	private final LiteralType type;

	@Override
	public Location getLocation() {
		return literal.location;
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("literal",literal.text);
		node.put("type",type.name());
		return node;
	}
}
