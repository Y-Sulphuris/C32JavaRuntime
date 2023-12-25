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
public class InitializerListExprTree implements ExprTree {
	private final Token open;
	private final List<ExprTree> initializers;
	private final Token close;

	@Override
	public Location getLocation() {
		return Location.between(open.location,close.location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("open",open.text);
		ArrayNode inits = mapper.createArrayNode();
		for (ExprTree initializer : initializers) {
			inits.add(initializer.toJson(mapper));
		}
		node.set("initializers",inits);
		node.put("close",close.text);
		return node;
	}
}
