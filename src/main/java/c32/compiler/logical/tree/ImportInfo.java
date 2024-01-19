package c32.compiler.logical.tree;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;


@RequiredArgsConstructor
@Getter
public class ImportInfo extends AbstractSymbolInfo {
	private final SpaceInfo parent;
	private final SymbolInfo imported;
	@Nullable private final String alias;
	private final boolean allIn;//star

	@Override
	public String getName() {
		return alias;
	}

	@Override
	public boolean isAccessibleFrom(SpaceInfo space) {
		return space == parent;
	}
}
