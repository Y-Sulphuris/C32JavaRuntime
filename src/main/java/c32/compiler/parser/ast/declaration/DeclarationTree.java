package c32.compiler.parser.ast.declaration;

import c32.compiler.parser.ast.ModifierTree;
import c32.compiler.parser.ast.Tree;
import c32.compiler.parser.ast.declarator.DeclaratorTree;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public interface DeclarationTree<T extends DeclaratorTree> extends Tree, Iterable<T> {
	List<ModifierTree> getModifiers();
	List<T> getDeclarators();

	@NotNull
	@Override
	default Iterator<T> iterator() {
		return getDeclarators().iterator();
	}
}
