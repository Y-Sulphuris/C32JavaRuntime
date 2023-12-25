package c32.compiler.parser.ast.expr;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.Tree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class NewExprTree implements ExprTree {
	private final Token keyword;
	@Nullable private final Tree args;
	private final ExprTree expression;

	@Override
	public Location getLocation() {
		return Location.between(keyword.location,expression.getLocation());
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();

		node.put("keyword",keyword.text);
		if (args != null) node.set("args",args.toJson(mapper));
		node.set("expression",expression.toJson(mapper));

		return node;
	}
}
