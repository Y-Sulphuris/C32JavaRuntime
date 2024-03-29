package c32.compiler.parser.ast.declaration;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.ModifierTree;
import c32.compiler.parser.ast.declarator.DeclaratorTree;
import c32.compiler.parser.ast.type.TypeElementTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;

@Data
public class ValuedDeclarationTree implements DeclarationTree<DeclaratorTree> {
	private final List<ModifierTree> modifiers;
	/**
	 * return type
	 */
	private final TypeElementTree typeElement;
	private final List<DeclaratorTree> declarators;
	private final Token endLine;
	private final Location location;




	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();

		ArrayNode modifiersNode = mapper.createArrayNode();
		for (ModifierTree modifier : getModifiers())
			modifiersNode.add(modifier.toJson(mapper));
		node.set("modifiers",modifiersNode);

		if (typeElement != null) node.set("typeElement",typeElement.toJson(mapper));

		ArrayNode declsNode = mapper.createArrayNode();
		for (DeclaratorTree declarator : declarators) {
			declsNode.add(declarator.toJson(mapper));
		}
		node.set("declarators",declsNode);

		if (endLine != null) node.put("endLine",endLine.text);

		node.set("location",getLocation().toJson(mapper));

		return node;
	}
}
