package c32.compiler.parser.ast.expr;

import c32.compiler.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class CallExprTree implements LValueExprTree {
	private final ReferenceExprTree reference;
	private final ArgumentListTree argumentList;

	@Override
	public Location getLocation() {
		return Location.between(reference.getLocation(),argumentList.getLocation());
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.set("reference",reference.toJson(mapper));
		node.set("argumentList",argumentList.toJson(mapper));
		return node;
	}
}
