package c32.compiler.parser.ast;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.declarator.DeclaratorTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

@Getter
public class FunctionDeclaratorTree extends DeclaratorTree {
	private final ParameterListTree parameterList;
	public FunctionDeclaratorTree(Token name, ParameterListTree parameterList) {
		super(name, Location.between(name.location,parameterList.getLocation()));
		this.parameterList = parameterList;
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("name", name.text);
		node.set("parameterList",parameterList.toJson(mapper));
		node.set("location", location.toJson(mapper));
		return node;
	}

	@Override
	public boolean isLineDeclarator() {
		return true;
	}
}
