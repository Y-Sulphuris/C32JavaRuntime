package c32.compiler.parser.ast.expr;

import c32.compiler.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class IndexExprTree implements LValueExprTree {
	private final ExprTree expr;
	private final IndexListTree args;

	@Override
	public Location getLocation() {
		return Location.between(expr.getLocation(), args.getLocation());
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.set("expr",expr.toJson(mapper));
		node.set("index", args.toJson(mapper));
		return node;
	}

}
