package c32.compiler.parser.ast.expr;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class IndexExpr implements LValueExprTree {
	private final ExprTree expr;
	private final Token openSquare;
	private final ExprTree index;
	private final Token closeSquare;

	@Override
	public Location getLocation() {
		return Location.between(expr.getLocation(),closeSquare.location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.set("expr",expr.toJson(mapper));
		node.put("openSquare",openSquare.text);
		node.set("index",index.toJson(mapper));
		node.put("closeSquare",closeSquare.text);
		return node;
	}

}
