package c32.compiler.parser.ast.declaration;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.ModifierTree;
import c32.compiler.parser.ast.declarator.TypenameDeclaratorTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;

@Data
public class TypenameDeclarationTree implements DeclarationTree<TypenameDeclaratorTree>{
	private final List<ModifierTree> modifiers;
	private final Token keyword;
	private final List<TypenameDeclaratorTree> declarators;
	private final Token endLine;
	private final Location location;

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		ArrayNode modifiersNode = mapper.createArrayNode();
		for (ModifierTree modifier : modifiers)
			modifiersNode.add(modifier.toJson(mapper));
		node.set("modifiers",modifiersNode);
		node.put("keyword",keyword.text);
		ArrayNode declsNode = mapper.createArrayNode();
		for (TypenameDeclaratorTree declarator : declarators) {
			declsNode.add(declarator.toJson(mapper));
		}
		node.set("declarators",declsNode);
		node.set("location",getLocation().toJson(mapper));
		node.put("endLine",endLine.text);
		return node;
	}
}
