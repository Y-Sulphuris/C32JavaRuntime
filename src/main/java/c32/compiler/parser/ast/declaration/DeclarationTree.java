package c32.compiler.parser.ast.declaration;

import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.Tree;
import c32.compiler.parser.ast.declarator.DeclaratorTree;

import java.util.List;

public interface DeclarationTree<T extends DeclaratorTree> extends Tree {
	List<Token> getModifiers();
	List<T> getDeclarators();
}
