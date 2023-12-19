package c32.compiler.parser.ast.declarator;

import c32.compiler.Location;
import c32.compiler.parser.ast.FunctionDeclaratorTree;
import c32.compiler.parser.ast.statement.BlockStatementTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

@Getter
public class FunctionDefinitionTree extends DeclaratorTree {
	private final FunctionDeclaratorTree declarator;
	private final BlockStatementTree blockStatement;

	public FunctionDefinitionTree(FunctionDeclaratorTree declarator, BlockStatementTree blockStatement, Location location) {
		super(declarator.getName(),location);
		this.declarator = declarator;
		this.blockStatement = blockStatement;
	}


	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.set("declarator",declarator.toJson(mapper));
		node.set("blockStatement",blockStatement.toJson(mapper));
		node.set("location",getLocation().toJson(mapper));
		return node;
	}

	@Override
	public boolean isLineDeclarator() {
		return false;
	}
}
