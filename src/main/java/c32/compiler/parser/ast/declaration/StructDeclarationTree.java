package c32.compiler.parser.ast.declaration;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.declarator.StructDeclaratorTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;

@Data
public class StructDeclarationTree implements TypeDeclarationTree<StructDeclaratorTree> {
	private final List<Token> modifiers;
	private final List<StructDeclaratorTree> declarators;

	private final Location location;
	
	private final Token keyword;

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		for (Token modifier : modifiers) {
			node.put(modifier.text,modifier.text);
		}
		node.put("keyword",keyword.text);
		ArrayNode declsNode = mapper.createArrayNode();
		for (StructDeclaratorTree declarator : declarators) {
			declsNode.add(declarator.toJson(mapper));
		}
		node.set("declarators",declsNode);
		node.set("location",getLocation().toJson(mapper));
		return node;
	}
}
