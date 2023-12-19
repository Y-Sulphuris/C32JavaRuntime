package c32.compiler.parser.ast.declarator;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.Tree;
import lombok.Data;


@Data
public abstract class DeclaratorTree implements Tree {
	protected final Token name;
	protected final Location location;

	@Override
	public Location getLocation() {
		return location;
	}

	public abstract boolean isLineDeclarator();
}
