package c32.compiler.parser.ast;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.type.TypeElementTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;

@Data
public class ThrowsTree implements Tree {
	private final Token keyword;
	private final List<TypeElementTree> exceptionTypes;

	@Override
	public Location getLocation() {
		if (exceptionTypes.isEmpty()) return keyword.location;
		return Location.between(keyword.location,exceptionTypes.get(exceptionTypes.size()-1).getLocation());
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();

		node.put("keyword",keyword.text);

		ArrayNode exceptionTypesNode = mapper.createArrayNode();
		for (TypeElementTree type : exceptionTypes) {
			exceptionTypesNode.add(type.toJson(mapper));
		}
		node.set("exceptionTypes",exceptionTypesNode);

		return node;
	}
}
