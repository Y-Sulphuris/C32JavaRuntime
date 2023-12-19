package c32.compiler.parser.ast.statement;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ExprTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class DoWhileStatement implements StatementTree {
	private final Token doKeyword;
	private final StatementTree statement;
	private final Token whileKeyword;
	private final Token openRound;
	private final ExprTree condition;
	private final Token closeRound;
	private final Token endLine;

	@Override
	public Location getLocation() {
		return Location.between(doKeyword.location,endLine.location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("doKeyword",doKeyword.text);
		node.set("statement",statement.toJson(mapper));
		node.put("whileKeyword",whileKeyword.text);
		node.put("openRound",openRound.text);
		node.set("condition",condition.toJson(mapper));
		node.put("closeRound",closeRound.text);
		node.put("endLine",endLine.text);
		return node;
	}
}
