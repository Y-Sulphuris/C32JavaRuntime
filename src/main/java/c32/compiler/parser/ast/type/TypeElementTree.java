package c32.compiler.parser.ast.type;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.Tree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

public abstract class TypeElementTree implements Tree {
	public abstract Token getKeyword();

	@EqualsAndHashCode(callSuper = true)
	@Data
	public static class StructTypeElementTree extends TypeElementTree {
		private final Token keyword;

		@Override
		public Location getLocation() {
			return keyword.location;
		}

		@Override
		public JsonNode toJson(ObjectMapper mapper) {
			return mapper.createObjectNode().put("keyword",keyword.text);
		}
	}
}

