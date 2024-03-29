package c32.compiler.parser.ast.expr;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ExprTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class ReferenceExprTree implements LValueExprTree {
	private final Token identifier;

	public ReferenceExprTree(Token identifier) {
		this.identifier = identifier;
	}

	@Override
	public Location getLocation() {
		return identifier.location;
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("identifier",identifier.text);
		node.set("location",identifier.location.toJson(mapper));
		return node;
	}
}
