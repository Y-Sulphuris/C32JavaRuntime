package c32.compiler.parser.ast;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class ModifierTree implements Tree {
	private final Token keyword;
	@Nullable private final Token openSquare;
	@Nullable private final List<Token> attributes;
	@Nullable private final Token closeSquare;

	public ModifierTree(Token keyword) {
		this.keyword = keyword;
		this.openSquare = null;
		this.attributes = null;
		this.closeSquare = null;
	}

	@SuppressWarnings("NullableProblems")
	public ModifierTree(Token keyword, Token openSquare, List<Token> attributes, Token closeSquare) {
		this.keyword = keyword;
		this.openSquare = openSquare;
		this.attributes = attributes;
		this.closeSquare = closeSquare;
	}

	@Override
	public Location getLocation() {
		if (closeSquare == null) return keyword.location;
		else return Location.between(keyword.location,closeSquare.location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();

		node.put("keyword",keyword.text);

		if (attributes != null) {
			node.put("openSquare",openSquare.text);

			ArrayNode attributesNode = mapper.createArrayNode();
			for(Token token : attributes)
				attributesNode.add(token.text);

			node.put("closeSquare",closeSquare.text);
		}

		return node;
	}
}
