package c32.compiler.codegen;

import c32.compiler.logical.tree.NamespaceInfo;
import c32.compiler.logical.tree.SpaceInfo;

public interface Generator {
	void generate(NamespaceInfo space);
}
