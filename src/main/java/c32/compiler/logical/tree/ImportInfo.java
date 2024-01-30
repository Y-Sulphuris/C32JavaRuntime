package c32.compiler.logical.tree;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;


@Getter
public class ImportInfo extends AbstractSymbolInfo {
	private final SpaceInfo parent;
	private final Collection<SymbolInfo> imported;
	@Nullable private final String alias;
	private final Location location;

	public ImportInfo(SpaceInfo parent, Collection<SymbolInfo> imported, @Nullable String alias, boolean allIn, Location location) {
		if (allIn && alias != null)
			throw new CompilerException(location, "cannot make alias for '*' import");
		this.parent = parent;
		this.imported = imported;
		this.alias = alias;
		this.location = location;
	}


	@Override
	public String getName() {
		return alias;
	}

	@Override
	public boolean isAccessibleFrom(SpaceInfo space) {
		return space == parent;
	}
}
