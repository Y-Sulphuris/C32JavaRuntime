package c32.compiler.parser.ast;

import c32.compiler.Location;
import c32.compiler.parser.ast.declaration.DeclarationTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.*;

@Data
public class CompilationUnitTree implements Tree {
	private final String fileName;
	String packageName = "";

	private final List<DeclarationTree<?>> declarations;
	public CompilationUnitTree(String fileName, List<DeclarationTree<?>> declarations) {
		this.fileName = fileName;
		this.declarations = declarations;
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		ArrayNode declarationsNode = mapper.createArrayNode();
		for (DeclarationTree<?> declaration : declarations) {
			declarationsNode.add(declaration.toJson(mapper));
		}
		node.set("declarations",declarationsNode);
		return node;
	}

	@Override
	public Location getLocation() {
		return new Location(-1,-1,-1,-1);
	}
}
