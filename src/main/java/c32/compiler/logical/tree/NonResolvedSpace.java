package c32.compiler.logical.tree;

import c32.compiler.except.CompilerException;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class NonResolvedSpace implements SpaceInfo, NonResolved<SpaceInfo> {
	private final SpaceInfo caller;
	private final ReferenceExprTree reference;

	@Override
	public SpaceInfo getParent() {
		return null;
	}

	@Override
	public Set<FunctionInfo> getFunctions() {
		return Collections.emptySet();
	}

	@Override
	public FunctionInfo addFunction(FunctionInfo function) {
		return null;
	}

	@Override
	public Set<NamespaceInfo> getNamespaces() {
		return null;
	}

	@Override
	public NamespaceInfo addNamespace(NamespaceInfo namespace) {
		return null;
	}

	@Override
	public Set<FieldInfo> getFields() {
		return null;
	}

	@Override
	public FieldInfo addField(FieldInfo field) {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public CompilerException fail() {
		return new CompilerException(reference.getLocation(), "cannot find anything from '" + caller.getCanonicalName() + "' for '" + reference + "'");
	}

	@Override
	public SpaceInfo tryToResolve() {
		return caller.resolveSpace(caller,reference);
	}
}
