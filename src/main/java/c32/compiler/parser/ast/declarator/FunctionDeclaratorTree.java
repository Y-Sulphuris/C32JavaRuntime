package c32.compiler.parser.ast.declarator;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.ModifierTree;
import c32.compiler.parser.ast.ParameterListTree;
import c32.compiler.parser.ast.ThrowsTree;
import c32.compiler.parser.ast.declarator.DeclaratorTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class FunctionDeclaratorTree extends DeclaratorTree {
	private final ParameterListTree parameterList;
	private final List<ModifierTree> postModifiers;
	@Nullable private final ThrowsTree throwsExceptions;
	public FunctionDeclaratorTree(Token name, ParameterListTree parameterList, List<ModifierTree> postModifiers, ThrowsTree throwsExceptions) {
		super(name, Location.between(name.location,parameterList.getLocation()));
		this.parameterList = parameterList;
		this.postModifiers = postModifiers;
		this.throwsExceptions = throwsExceptions;
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();

		if (name != null) node.put("name", name.text);

		node.set("parameterList",parameterList.toJson(mapper));

		ArrayNode postModifiersNode = mapper.createArrayNode();
		for (ModifierTree token : postModifiers) {
			postModifiersNode.add(token.toJson(mapper));
		}
		node.set("postModifiers",postModifiersNode);

		if (throwsExceptions != null) node.set("throws",throwsExceptions.toJson(mapper));

		node.set("location", location.toJson(mapper));

		return node;
	}

	@Override
	public boolean isLineDeclarator() {
		return true;
	}
}
