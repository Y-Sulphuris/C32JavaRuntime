package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
@Getter
public class VariableInfo implements SymbolInfo {
	private final String name;
	private final TypeRefInfo typeRef;
	@Nullable private final Expression initializer;

	public VariableInfo(String name, TypeRefInfo typeRef) {
		this.name = name;
		this.typeRef = typeRef;
		this.initializer = null;
	}
}
