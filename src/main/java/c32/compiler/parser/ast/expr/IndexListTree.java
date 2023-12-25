package c32.compiler.parser.ast.expr;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.Tree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;

@Data
public class IndexListTree implements Tree {
	private final Token openSquare;
	private final List<ExprTree> indexes;
	private final Token closeSquare;

	@Override
	public Location getLocation() {
		return Location.between(openSquare.location,closeSquare.location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("openSquare",openSquare.text);
		ArrayNode indexesNode = mapper.createArrayNode();
		for(ExprTree index : indexes) {
			indexesNode.add(index.toJson(mapper));
		}
		node.set("args",indexesNode);
		node.put("closeSquare",closeSquare.text);
		return node;
	}
}
