package c32.compiler.parser.ast.declaration;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.declarator.DeclaratorTree;
import c32.compiler.parser.ast.type.RuntimeTypeElementTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;

@Data
public class ValuedDeclarationTree implements DeclarationTree<DeclaratorTree> {
	private final List<Token> modifiers;
	private final RuntimeTypeElementTree typeElement;
	private final List<DeclaratorTree> declarators;
	private final Token endLine;
	private final Location location;




	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		for (Token modifier : getModifiers()) {
			node.put(modifier.text,modifier.text);
		}
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
