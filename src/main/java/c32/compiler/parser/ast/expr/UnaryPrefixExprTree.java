package c32.compiler.parser.ast.expr;

import c32.compiler.Compiler;
import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class UnaryPrefixExprTree implements LValueExprTree {
	private final Token operator;
	private final ExprTree expression;

	@Override
	public Location getLocation() {
		return Location.between(operator.location, expression.getLocation());
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("operator",operator.text);
		node.set("expression",expression.toJson(mapper));
		return node;
	}

	public int getPriority() {
		return Compiler.getPrefixOperatorPriority(operator.text);
	}
}
