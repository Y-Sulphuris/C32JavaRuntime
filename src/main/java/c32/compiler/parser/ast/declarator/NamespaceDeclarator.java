package c32.compiler.parser.ast.declarator;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.declaration.DeclarationTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class NamespaceDeclarator extends DeclaratorTree {
	private final Token open;
	private final List<DeclarationTree<?>> declarations;
	private final Token close;


	public NamespaceDeclarator(@Nullable Token name, Token open, List<DeclarationTree<?>> declarations, Token close) {
		super(name, Location.between(open.location,close.location));
		this.open = open;
		this.declarations = declarations;
		this.close = close;
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		if (name != null) node.put("name",name.text);
		ArrayNode declarationsNode = mapper.createArrayNode();
		for (DeclarationTree<?> declaration : declarations) {
			declarationsNode.add(declaration.toJson(mapper));
		}
		node.set("declarations",declarationsNode);
		node.set("location",location.toJson(mapper));
		return node;
	}

	@Override
	public boolean isLineDeclarator() {
		return false;
	}
}
