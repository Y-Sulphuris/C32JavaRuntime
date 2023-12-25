package c32.compiler.parser.ast.declarator;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.type.StaticElementReferenceTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class ImportDeclaratorTree extends DeclaratorTree {
	private final StaticElementReferenceTree symbol;
	@Nullable private final Token assign;

	public ImportDeclaratorTree(@Nullable Token alias, StaticElementReferenceTree symbol, @Nullable Token assign, Location location) {
		super(alias, location);
		this.symbol = symbol;
		this.assign = assign;
	}

	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();


		node.set("symbol", symbol.toJson(mapper));
		if (assign != null) {
			node.put("assign",assign.text);
			assert name != null;
			node.put("aliasName",name.text);
		}

		return node;
	}

	@Override
	public boolean isLineDeclarator() {
		return true;
	}
}
