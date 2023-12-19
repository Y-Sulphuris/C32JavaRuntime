package c32.compiler.parser.ast.type;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ReferenceTypeElementTree extends RuntimeTypeElementTree {
	private final RuntimeTypeElementTree elementType;
	private final Token ampersand;

	public ReferenceTypeElementTree(Token _const, Token _restrict, RuntimeTypeElementTree elementType, Token ampersand) {
		super(_const, _restrict);
		this.elementType = elementType;
		this.ampersand = ampersand;
	}

	@Override
	public Location getLocation() {
		return elementType.getLocation();
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
		root.set("referenceTargetType",elementType.toJson(mapper));
		root.put("ampersand",ampersand.text);
		return root;
	}
}
