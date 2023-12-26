package c32.compiler.logical.tree;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.parser.ast.ModifierTree;
import c32.compiler.parser.ast.declaration.ParameterDeclaration;
import c32.compiler.parser.ast.declarator.FunctionDeclaratorTree;
import c32.compiler.parser.ast.type.TypeElementTree;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class FunctionDeclarationInfo implements FunctionInfo {
	private final String name;
	private final TypeRefInfo returnType;
	private final List<VariableInfo> args = new ArrayList<>();
	private final List<TypeRefInfo> throwTypes = new ArrayList<>();
	private final SpaceInfo parent;

	public FunctionDeclarationInfo(SpaceInfo parent, List<ModifierTree> modifiers, TypeElementTree retType, FunctionDeclaratorTree definition) {
		this.parent = parent;
		assert definition.getName() != null;
		this.name = definition.getName().text;
		this.returnType = parent.resolveType(parent,retType);
		int arg_i = 0;
		for (ParameterDeclaration param : definition.getParameterList().getParameters()) {
			String argName;
			if (param.getDeclarator() != null) {
				assert param.getDeclarator().getName() != null;
				argName = param.getDeclarator().getName().text;
			} else argName = "$arg" + arg_i;
			args.add(new VariableInfo(argName,parent.resolveType(parent,param.getTypeElement()),null));
		}
		if (definition.getThrowsExceptions() != null)
			for (TypeElementTree exceptionType : definition.getThrowsExceptions().getExceptionTypes())
				throwTypes.add(parent.resolveType(parent,exceptionType));

		if (!modifiers.isEmpty()) throw new CompilerException(
				Location.between(modifiers.get(0).getLocation(),modifiers.get(modifiers.size()-1).getLocation()),
				"unknown modifiers:" + modifiers
		);


		boolean _pure = false;
		boolean _noexcept = false;
		{
			Set<ModifierTree> forRemoval = new HashSet<>();
			for (ModifierTree post : definition.getPostModifiers()) {
				switch (post.getKeyword().text) {
					case "pure": {
						if (post.getAttributes() != null)
							throw new CompilerException(post.getAttributes().get(0).location,"unknown attribute");
						_pure = true;
						forRemoval.add(post);
					} break;
					case "noexcept": {
						if (post.getAttributes() != null)
							throw new CompilerException(post.getAttributes().get(0).location,"unknown attribute");
						_noexcept = true;
						forRemoval.add(post);
					} break;
				}
			}
			definition.getPostModifiers().removeAll(forRemoval);
		}//	delete forRemoval;
		if (!definition.getPostModifiers().isEmpty()) {
			throw new CompilerException(
					definition.getPostModifiers().get(0).getLocation(),
					"illegal post modifiers: " + definition.getPostModifiers().stream().map(m -> m.getKeyword().text).collect(Collectors.joining())
			);
		}
		this._pure = _pure;
		this._noexcept = _noexcept;
	}

	@Override
	public Set<FunctionInfo> getFunctions() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FunctionInfo addFunction(FunctionInfo function) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<NamespaceInfo> getNamespaces() {
		throw new UnsupportedOperationException();
	}

	@Override
	public NamespaceInfo addNamespace(NamespaceInfo namespace) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<FieldInfo> getFields() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldInfo addField(FieldInfo field) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAccessibleFrom(SpaceInfo namespace) {
		return true;
	}

	private final boolean _pure;
	private final boolean _noexcept;
}
