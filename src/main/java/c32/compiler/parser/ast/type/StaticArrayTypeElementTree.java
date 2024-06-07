package c32.compiler.parser.ast.type;

import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ExprTree;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

@Getter
public class StaticArrayTypeElementTree extends ArrayTypeElementTree {
	private final ExprTree size;

	public StaticArrayTypeElementTree(Token _mut, Token _const, Token _restrict, TypeElementTree elementType, Token openSquare, ExprTree size, Token closeSquare) {
		super(_mut, _const, _restrict, elementType, openSquare, closeSquare);
		this.size = size;
	}

	@Override
	protected ObjectNode applyJson(ObjectMapper mapper, ObjectNode root) {
		root.set("staticArrayElementType",getElementType().toJson(mapper));
		root.put("openSquare",getOpenSquare().text);
		root.set("size",size.toJson(mapper));
		root.put("closeSquare",getCloseSquare().text);
		return root;
	}
}
