package c32.compiler.parser.ast.type;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TypeReferenceElementTree extends RuntimeTypeElementTree {

	private final ReferenceExprTree typeReference;

	public TypeReferenceElementTree(Token _const, Token _restrict, ReferenceExprTree typeReference) {
		super(_const, _restrict);
		this.typeReference = typeReference;
	}

	@Override
	public Token getKeyword() {
		return null;
	}

	@Override
	public ReferenceExprTree getTypeReference() {
		return typeReference;
	}

	@Override
	public Location getLocation() {
		return Location.between(get_const().location,typeReference.getLocation());
	}

	@Override
	protected ObjectNode applyJson(ObjectMapper mapper, ObjectNode root) {
		return root.set("typeReference",typeReference.toJson(mapper));
	}
}
