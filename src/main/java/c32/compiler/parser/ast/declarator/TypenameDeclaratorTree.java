package c32.compiler.parser.ast.declarator;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.type.TypeElementTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

@Getter
public class TypenameDeclaratorTree extends DeclaratorTree {
	private final Token assign;
	private final TypeElementTree targetType;

	public TypenameDeclaratorTree(Token name, Token assign, TypeElementTree targetType, Location location) {
		super(name, location);
		this.assign = assign;
		this.targetType = targetType;
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("name",name.text);
		node.put("assign",assign.text);
		node.set("targetType",targetType.toJson(mapper));
		node.set("location",location.toJson(mapper));
		return node;
	}

	@Override
	public boolean isLineDeclarator() {
		return true;
	}
}
