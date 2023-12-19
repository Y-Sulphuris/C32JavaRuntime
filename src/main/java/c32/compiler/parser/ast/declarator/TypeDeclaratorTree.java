package c32.compiler.parser.ast.declarator;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;

public abstract class TypeDeclaratorTree extends DeclaratorTree {
	public TypeDeclaratorTree(Token name, Location location) {
		super(name, location);
	}
}
