package c32.compiler.parser.ast;

import c32.compiler.Location;
import c32.compiler.parser.ast.declaration.DeclarationTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

@Data
public class CompilationUnitTree implements Tree {
	private final String fileName;
	@Nullable private final PackageTree packageTree;

	private final List<DeclarationTree<?>> declarations;
	public CompilationUnitTree(String fileName, @Nullable PackageTree packageTree, List<DeclarationTree<?>> declarations) {
		this.fileName = fileName;
		this.packageTree = packageTree;
		this.declarations = declarations;
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();

		if (packageTree != null) node.set("package",packageTree.toJson(mapper));

		ArrayNode declarationsNode = mapper.createArrayNode();
		for (DeclarationTree<?> declaration : declarations) {
			declarationsNode.add(declaration.toJson(mapper));
		}
		node.set("declarations",declarationsNode);

		return node;
	}

	@Override
	public Location getLocation() {
		return new Location(-1,-1,-1,-1,new File(fileName));
	}

	@Override
	public String toString() {
		return "CompilationUnit(" + fileName + ")";
	}
}
