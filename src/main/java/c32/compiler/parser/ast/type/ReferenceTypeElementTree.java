package c32.compiler.parser.ast.type;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ReferenceTypeElementTree extends TypeElementTree {
	private final TypeElementTree elementType;
	private final Token ampersand;

	public ReferenceTypeElementTree(Token _mut, Token _const, Token _restrict, TypeElementTree elementType, Token ampersand) {
		super(_mut, _const, _restrict);
		this.elementType = elementType;
		this.ampersand = ampersand;
	}

	@Override
	public Location getLocation() {
		return elementType.getLocation();
	}

	@Override
	protected ObjectNode applyJson(ObjectMapper mapper, ObjectNode root) {
		root.set("referenceTargetType",elementType.toJson(mapper));
		root.put("ampersand",ampersand.text);
		return root;
	}
}
