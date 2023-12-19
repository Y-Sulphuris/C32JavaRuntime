package c32.compiler.parser.ast.declarator;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ExprTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

@Getter
public class VariableDeclaratorTree extends DeclaratorTree {
	private final Token assignOperator;
	private final ExprTree initializer;

	public VariableDeclaratorTree(Token name, Token assignOperator, ExprTree initializer, Location location) {
		super(name, location);
		this.assignOperator = assignOperator;
		this.initializer = initializer;
	}

	public VariableDeclaratorTree(Token name,  Location location) {
		this(name,null,null, location);
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("name", name.text);
		if (assignOperator != null) node.put("assignOperator", assignOperator.text);
		if (initializer != null) node.set("initializer", initializer.toJson(mapper));
		node.set("location", location.toJson(mapper));
		return node;
	}

	@Override
	public boolean isLineDeclarator() {
		return true;
	}
}
