package c32.compiler.parser.ast.expr;

import c32.compiler.Location;
import c32.compiler.parser.ast.type.TypeElementTree;
import c32.compiler.parser.ast.type.TypeReferenceElementTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SizeOfExprTree implements ExprTree {
	private final Location location;
	private final TypeElementTree type;


	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.set("type", type.toJson(mapper));
		node.set("location",location.toJson(mapper));
		return node;
	}
}
