package c32.compiler.parser.ast.type;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

@Getter
public class DynamicArrayTypeElementTree extends RuntimeTypeElementTree {
	private final RuntimeTypeElementTree elementType;
	private final Token openSquare;
	private final Token closeSquare;

	public DynamicArrayTypeElementTree(Token _const, Token _restrict, RuntimeTypeElementTree elementType, Token openSquare, Token closeSquare) {
		super(_const, _restrict);
		this.elementType = elementType;
		this.openSquare = openSquare;
		this.closeSquare = closeSquare;
	}


	@Override
	public Location getLocation() {
		return Location.between(elementType.getLocation(),closeSquare.location);
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
		root.set("arrayElementType",elementType.toJson(mapper));
		root.put("openSquare",openSquare.text);
		root.put("closeSquare",closeSquare.text);
		return root;
	}
}
