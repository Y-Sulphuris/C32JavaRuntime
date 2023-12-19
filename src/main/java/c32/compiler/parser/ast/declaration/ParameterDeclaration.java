package c32.compiler.parser.ast.declaration;

import c32.compiler.Location;
import c32.compiler.parser.ast.Tree;
import c32.compiler.parser.ast.declarator.DeclaratorTree;
import c32.compiler.parser.ast.type.RuntimeTypeElementTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class ParameterDeclaration implements Tree {
	private final RuntimeTypeElementTree typeElement;
	private final DeclaratorTree declarator;

	@Override
	public Location getLocation() {
		return Location.between(typeElement.getLocation(),declarator.getLocation());
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.set("typeElement",typeElement.toJson(mapper));
		node.set("declarator",declarator.toJson(mapper));
		return node;
	}
}
