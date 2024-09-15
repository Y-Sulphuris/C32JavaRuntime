package c32.compiler.codegen;

import c32.compiler.logical.tree.NamespaceInfo;
import c32.compiler.logical.tree.SpaceInfo;

import java.io.File;

public interface Generator {
	void generate(NamespaceInfo space, File outputDirectory);
}
