package c32.compiler.parser.ast.type;

import c32.compiler.Location;
import c32.compiler.parser.ast.Tree;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;

@Data
public final class StaticElementReferenceTree implements Tree {
	private final List<ReferenceExprTree> references;
	private final Location location;

	public StaticElementReferenceTree(List<ReferenceExprTree> references) {
		this.references = references;
		this.location = Location.between(references.get(0).getLocation(),references.get(references.size()-1).getLocation());
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();

		ArrayNode refsNode = mapper.createArrayNode();
		for (ReferenceExprTree reference : references) {
			refsNode.add(reference.toJson(mapper));
		}
		node.set("references",refsNode);

		return node;
	}
}

