package c32.compiler.parser.ast.expr;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class BinaryExprTree implements ExprTree {
	private final ExprTree lhs;
	private final Token operator;
	private final ExprTree rhs;

	@Override
	public Location getLocation() {
		return Location.between(lhs.getLocation(),rhs.getLocation());
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.set("lhs",lhs.toJson(mapper));
		node.put("operator",operator.text);
		node.set("rhs",rhs.toJson(mapper));
		return node;
	}
}
