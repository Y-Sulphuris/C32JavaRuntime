package c32.compiler.logical.tree;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.parser.ast.ModifierTree;
import c32.compiler.parser.ast.declaration.ParameterDeclaration;
import c32.compiler.parser.ast.declarator.FunctionDeclaratorTree;
import c32.compiler.parser.ast.declarator.FunctionDefinitionTree;
import c32.compiler.parser.ast.type.TypeElementTree;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class FunctionDeclarationInfo implements FunctionInfo {
	private final String name;
	private final TypeRefInfo returnType;
	private final List<LocalVariableInfo> args = new ArrayList<>();
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
			args.add(new LocalVariableInfo(argName,parent.resolveType(parent,param.getTypeElement())));
		}
		if (definition.getThrowsExceptions() != null)
			for (TypeElementTree exceptionType : definition.getThrowsExceptions().getExceptionTypes())
				throwTypes.add(parent.resolveType(parent,exceptionType));

		if (!modifiers.isEmpty()) throw new CompilerException(
				Location.between(modifiers.get(0).getLocation(),modifiers.get(modifiers.size()-1).getLocation()),
				"unknown modifiers:" + modifiers
		);
	}

	@Override
	public Set<FunctionInfo> getFunctions() {
		return null;
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
	public boolean isAccessibleFrom(SpaceInfo namespace) {
		return true;
	}
}
