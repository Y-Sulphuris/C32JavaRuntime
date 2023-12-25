package c32.compiler.parser.ast.statement;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.ParameterListTree;
import c32.compiler.parser.ast.Tree;
import c32.compiler.parser.ast.declaration.DeclarationTree;
import c32.compiler.parser.ast.expr.ArgumentListTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class TryCatchFinallyStatementTree implements StatementTree {
	private final Token tryKeyword;
	private final StatementTree tryBlock;
	private final List<CatchBlock> catches;
	@Nullable private final Token finallyKeyword;
	@Nullable private final StatementTree finallyBlock;

	@Override
	public Location getLocation() {
		if (finallyBlock != null) return Location.between(tryKeyword.location,finallyBlock.getLocation());
		return Location.between(tryKeyword.location,catches.get(catches.size()-1).getLocation());
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("tryKeyword",tryKeyword.text);
		node.set("tryBlock",tryBlock.toJson(mapper));
		ArrayNode catchesNode = mapper.createArrayNode();
		for (CatchBlock catchBlock : catches) {
			catchesNode.add(catchBlock.toJson(mapper));
		}
		node.set("catches",catchesNode);
		if (finallyKeyword != null) {
			assert finallyBlock != null;
			node.put("finallyKeyword",finallyKeyword.text);
			node.set("finallyBlock",finallyBlock.toJson(mapper));
		}
		return node;
	}


	@Data
	public static class CatchBlock implements Tree {
		private final Token catchKeyword;
		private final ParameterListTree catchExceptions;
		private final StatementTree catchBlock;

		@Override
		public Location getLocation() {
			return Location.between(catchKeyword.location,catchBlock.getLocation());
		}

		@Override
		public JsonNode toJson(ObjectMapper mapper) {
			ObjectNode node = mapper.createObjectNode();

			node.put("keyword",catchKeyword.text);
			node.set("catchExceptions",catchExceptions.toJson(mapper));
			node.set("statement",catchBlock.toJson(mapper));

			return node;
		}
	}
}
