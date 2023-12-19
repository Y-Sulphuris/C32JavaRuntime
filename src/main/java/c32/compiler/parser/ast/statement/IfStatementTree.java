package c32.compiler.parser.ast.statement;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ExprTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class IfStatementTree implements StatementTree {
	private final Token keyword;
	private final Token openRound;
	private final ExprTree condition;
	private final Token closeRound;
	private final StatementTree statement;
	@Nullable private final Token elseKeyword;
	@Nullable private final StatementTree elseStatement;

	@Override
	public Location getLocation() {
		if (elseStatement == null) return Location.between(keyword.location,statement.getLocation());
		return Location.between(keyword.location,elseStatement.getLocation());
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("keyword",keyword.text);
		node.put("open", openRound.text);
		node.set("condition",condition.toJson(mapper));
		node.put("close", closeRound.text);
		node.set("statement",statement.toJson(mapper));
		if (elseKeyword != null) node.put("elseKeyword",elseKeyword.text);
		if (elseStatement != null) node.set("elseStatement",elseStatement.toJson(mapper));
		return node;
	}
}
