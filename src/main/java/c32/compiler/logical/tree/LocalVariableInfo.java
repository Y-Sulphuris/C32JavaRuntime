package c32.compiler.logical.tree;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LocalVariableInfo implements SymbolInfo {
	private final String name;
	private final TypeRefInfo typeRef;
}
