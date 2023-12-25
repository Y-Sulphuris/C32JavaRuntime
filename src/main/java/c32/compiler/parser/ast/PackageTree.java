package c32.compiler.parser.ast;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.type.StaticElementReferenceTree;
import c32.compiler.parser.ast.type.TypeElementTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;

@Data
public class PackageTree implements Tree {
	private final List<ModifierTree> modifiersTree;
	private final Token keyword;
	private final StaticElementReferenceTree name;
	private final Token endLine;
	private final Location location;

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();

		node.put("keyword",keyword.text);
		node.set("name",name.toJson(mapper));
		node.put("endLine",endLine.text);
		node.set("location",location.toJson(mapper));

		return node;
	}
}
