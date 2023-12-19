package c32.compiler.parser.ast.type;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PointerTypeElementTree extends RuntimeTypeElementTree {
	private final RuntimeTypeElementTree elementType;
	private final Token star;

	public PointerTypeElementTree(Token _const, Token _restrict, RuntimeTypeElementTree elementType, Token star) {
		super(_const,_restrict);
		this.elementType = elementType;
		this.star = star;
	}


	@Override
	public Location getLocation() {
		return Location.between(elementType.getLocation(),star.location);
	}

	@Override
	public Token getKeyword() {
		return elementType.getKeyword();
	}

	@Override
	public ReferenceExprTree getTypeReference() {
		return elementType.getTypeReference();
	}

	@Override
	protected ObjectNode applyJson(ObjectMapper mapper, ObjectNode root) {
		root.set("pointerTargetType",elementType.toJson(mapper));
		root.put("star",star.text);
		return root;
	}
}
