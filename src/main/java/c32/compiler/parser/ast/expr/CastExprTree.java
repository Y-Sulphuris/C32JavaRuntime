package c32.compiler.parser.ast.expr;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.type.TypeElementTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class CastExprTree implements LValueExprTree {
	private final Token openRound;
	private final TypeElementTree targetType;
	private final Token closeRound;
	private final ExprTree expression;

	@Override
	public Location getLocation() {
		return Location.between(openRound.location,expression.getLocation());
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("openRound",openRound.text);
		node.set("targetType",targetType.toJson(mapper));
		node.put("closeRound",closeRound.text);
		node.set("expression",expression.toJson(mapper));
		return node;
	}

}
