package c32.compiler.parser.ast.type;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

@Getter
public class PointerTypeElementTree extends TypeElementTree {
	private final TypeElementTree elementType;
	private final Token star;

	public PointerTypeElementTree(Token _mut, Token _const, Token _restrict, TypeElementTree elementType, Token star) {
		super(_mut,_const,_restrict);
		this.elementType = elementType;
		this.star = star;
	}


	@Override
	public Location getLocation() {
		return Location.between(elementType.getLocation(),star.location);
	}

	@Override
	protected ObjectNode applyJson(ObjectMapper mapper, ObjectNode root) {
		root.set("pointerTargetType",elementType.toJson(mapper));
		root.put("star",star.text);
		return root;
	}
}
