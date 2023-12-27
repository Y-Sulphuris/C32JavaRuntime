package c32.compiler.logical.tree;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.parser.ast.ModifierTree;
import c32.compiler.parser.ast.declaration.ParameterDeclaration;
import c32.compiler.parser.ast.declarator.FunctionDeclaratorTree;
import c32.compiler.parser.ast.type.TypeElementTree;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class FunctionDeclarationInfo implements FunctionInfo {
	private final String name;
	private final TypeInfo returnType;
	private final List<VariableInfo> args = new ArrayList<>();
	private final List<TypeInfo> throwTypes = new ArrayList<>();
	private final SpaceInfo parent;

	public FunctionDeclarationInfo(SpaceInfo parent, List<ModifierTree> modifiers, TypeElementTree retType, FunctionDeclaratorTree declarator, boolean hasAnImplementation) {
		this.parent = parent;
		assert declarator.getName() != null;
		this.name = declarator.getName().text;
		this.returnType = parent.resolveType(parent,retType);
		if (retType.get_const() != null)
			throw new CompilerException(retType.get_const().location, "modifier 'const' has no affect there");

		if (retType.get_restrict() != null)
			throw new CompilerException(retType.get_restrict().location, "modifier 'restrict' has no affect there");

		int arg_i = 0;
		for (ParameterDeclaration param : declarator.getParameterList().getParameters()) {
			String argName;
			if (param.getDeclarator() != null) {
				assert param.getDeclarator().getName() != null;
				argName = param.getDeclarator().getName().text;
			} else argName = "$arg" + arg_i;
			args.add(new VariableInfo(argName,new TypeRefInfo(param.getTypeElement().get_const() != null,param.getTypeElement().get_restrict() != null,parent.resolveType(parent,param.getTypeElement())),null));
		}
		if (declarator.getThrowsExceptions() != null)
			for (TypeElementTree exceptionType : declarator.getThrowsExceptions().getExceptionTypes())
				throwTypes.add(parent.resolveType(parent,exceptionType));



		Location modLocation = retType.getLocation();
		if (!modifiers.isEmpty()) modLocation = Location.between(modifiers.get(0).getLocation(),modifiers.get(modifiers.size()-1).getLocation());
		boolean _extern = false;
		boolean _native = false;
		{
			Set<ModifierTree> forRemoval = new HashSet<>();
			for (ModifierTree mod : modifiers) {
				switch (mod.getKeyword().text) {
					case "extern": {
						if (mod.getAttributes() != null)
							throw new CompilerException(mod.getAttributes().get(0).location,"unknown attribute");
						_extern = true;
						forRemoval.add(mod);
					} break;
					case "native": {
						if (mod.getAttributes() != null)
							throw new CompilerException(mod.getAttributes().get(0).location,"unknown attribute");
						_native = true;
						forRemoval.add(mod);
					} break;
				}
			}
			modifiers.removeAll(forRemoval);
		}//	delete forRemoval;
		if (!modifiers.isEmpty()) throw new CompilerException(
				modLocation,
				"unknown modifiers:" + modifiers
		);
		if (hasAnImplementation) {
			if (_extern) throw new CompilerException(modLocation,"'extern' modifier are not allowed for implemented function");
			if (_native) throw new CompilerException(modLocation,"'native' modifier are not allowed for implemented function");
		} else {
			if (!_extern && !_native) {
				throw new CompilerException(declarator.getLocation(), "implementation expected");
			}
		}
		this._extern = _extern;
		this._native = _native;


		boolean _pure = false;
		boolean _noexcept = false;
		{
			Set<ModifierTree> forRemoval = new HashSet<>();
			for (ModifierTree post : declarator.getPostModifiers()) {
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
			declarator.getPostModifiers().removeAll(forRemoval);
		}//	delete forRemoval;
		if (!declarator.getPostModifiers().isEmpty()) {
			throw new CompilerException(
					declarator.getPostModifiers().get(0).getLocation(),
					"illegal post modifiers: " + declarator.getPostModifiers().stream().map(m -> m.getKeyword().text).collect(Collectors.joining())
			);
		}
		this._pure = _pure;
		this._noexcept = _noexcept;
	}

	@Override
	public boolean isAccessibleFrom(SpaceInfo namespace) {
		return true;
	}
	private final boolean _extern;
	private final boolean _native;

	private final boolean _pure;
	private final boolean _noexcept;
}
