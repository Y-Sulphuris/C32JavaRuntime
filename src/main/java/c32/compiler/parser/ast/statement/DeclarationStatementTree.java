package c32.compiler.parser.ast.statement;

import c32.compiler.Location;
import c32.compiler.parser.ast.declaration.DeclarationTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class DeclarationStatementTree implements StatementTree {
	private final DeclarationTree<?> declaration;
	@Override
	public Location getLocation() {
		return declaration.getLocation();
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.set("declaration",declaration.toJson(mapper));
		return node;
	}
}
