package c32.compiler.parser.ast.declarator;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.Tree;
import lombok.Data;
import org.jetbrains.annotations.Nullable;


@Data
public abstract class DeclaratorTree implements Tree {
	@Nullable protected final Token name;
	protected final Location location;

	@Override
	public Location getLocation() {
		return location;
	}

	public abstract boolean isLineDeclarator();
}
