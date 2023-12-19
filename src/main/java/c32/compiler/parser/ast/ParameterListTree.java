package c32.compiler.parser.ast;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.declaration.ParameterDeclaration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;

@Data
public class ParameterListTree implements Tree {
	private final Token open;

	private final List<ParameterDeclaration> parameters;

	private final Token close;
	@Override
	public Location getLocation() {
		return Location.between(open.location,close.location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("open", open.text);
		ArrayNode parametersNode = mapper.createArrayNode();
		for(ParameterDeclaration param : parameters) {
			parametersNode.add(param.toJson(mapper));
		}
		node.set("parameters", parametersNode);
		node.put("close", close.text);
		return node;
	}
}
