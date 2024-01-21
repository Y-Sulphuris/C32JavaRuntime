package c32.compiler.logical.tree;

import c32.compiler.Location;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class NamespaceInfo extends AbstractSpaceInfo {
	private final String name;
	private final SpaceInfo parent;
	private final boolean _static;

	@Override
	public boolean isAccessibleFrom(SpaceInfo namespace) {
		return true;
	}

	@Override
	public String toString() {
		return super.toString();
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object x) {
		return super.equals(x);
	}
}
