package c32.compiler.parser.ast.type;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TypeKeywordElementTree extends RuntimeTypeElementTree {
	private final Token keyword;

	public TypeKeywordElementTree(Token _const, Token _restrict, Token keyword) {
		super(_const,_restrict);
		this.keyword = keyword;
	}

	@Override
	public Token getKeyword() {
		return keyword;
	}

	@Override
	public ReferenceExprTree getTypeReference() {
		return null;
	}

	@Override
	public Location getLocation() {
		if (get_const() == null) return keyword.location;
		return Location.between(get_const().location,keyword.location);
	}

	@Override
	protected ObjectNode applyJson(ObjectMapper mapper, ObjectNode root) {
		return root.put("keyword",keyword.text);
	}
}
