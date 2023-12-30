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

	default ModifierTree getModifier(String mod) {
		for (ModifierTree modifier : getModifiers()) {
			if (modifier.getKeyword().text.equals(mod)) {
				return modifier;
			}
		}
		return null;
	}

	default boolean hasModifier(String mod) {
		return getModifier(mod) != null;
	}

	default ModifierTree eatModifier(String mod) {
		ModifierTree ret = null;
		for (ModifierTree modifier : getModifiers()) {
			if (modifier.getKeyword().text.equals(mod)) {
				ret = modifier;
				break;
			}
		}
		getModifiers().remove(ret);
		return ret;
	}
}
