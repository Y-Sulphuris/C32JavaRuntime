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
public class ArgumentListTree implements Tree {
	private final Token openRound;
	private final List<ExprTree> arguments;
	private final Token closeRound;

	@Override
	public Location getLocation() {
		return Location.between(openRound.location,closeRound.location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("openRound",openRound.text);
		ArrayNode args = mapper.createArrayNode();
		for (ExprTree initializer : arguments) {
			args.add(initializer.toJson(mapper));
		}
		node.set("arguments",args);
		node.put("closeRound",closeRound.text);
		return node;
	}
}
