package c32.compiler.parser.ast.type;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ExprTree;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class DeclTypeElementTree extends TypeElementTree {
	private final Token keyword;
	private final Token openRound;
	private final ExprTree expression;
	private final Token closeRound;
	public DeclTypeElementTree(@Nullable Token _mut, @Nullable Token _const, @Nullable Token _restrict, Token keyword, Token openRound, ExprTree expression, Token closeRound) {
		super(_mut, _const, _restrict);
		this.keyword = keyword;
		this.openRound = openRound;
		this.expression = expression;
		this.closeRound = closeRound;
	}

	@Override
	public Location getLocation() {
		return Location.between(keyword.location,closeRound.location);
	}

	@Override
	protected ObjectNode applyJson(ObjectMapper mapper, ObjectNode root) {
		root.put("keyword",keyword.text);
		root.put("open",openRound.text);
		root.set("expr",expression.toJson(mapper));
		root.put("close",closeRound.text);
		return root;
	}
}
