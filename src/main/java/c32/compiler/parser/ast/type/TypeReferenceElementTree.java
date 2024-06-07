package c32.compiler.parser.ast.type;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

@Getter
public class TypeReferenceElementTree extends TypeElementTree {
	private final StaticElementReferenceTree reference;
	private final Location location;

	public TypeReferenceElementTree(Token _mut, Token _const, Token _restrict, StaticElementReferenceTree reference, Location location) {
		super(_mut, _const, _restrict);
		this.reference = reference;
		this.location = location;
	}

	@Override
	protected ObjectNode applyJson(ObjectMapper mapper, ObjectNode root) {
		return root.set("reference",reference.toJson(mapper));
	}
}
