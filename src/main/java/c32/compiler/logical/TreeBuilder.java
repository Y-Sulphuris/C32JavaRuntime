package c32.compiler.logical;

import c32.compiler.except.CompilerException;
import c32.compiler.lexer.tokenizer.TokenType;
import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.logical.tree.statement.BlockStatement;
import c32.compiler.parser.ast.CompilationUnitTree;
import c32.compiler.parser.ast.PackageTree;
import c32.compiler.parser.ast.declaration.*;
import c32.compiler.parser.ast.declarator.*;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import c32.compiler.parser.ast.statement.BlockStatementTree;
import c32.compiler.parser.ast.type.StaticElementReferenceTree;
import lombok.var;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TreeBuilder {

	private static final Map<FunctionImplementationInfo, BlockStatementTree> implementations = new ConcurrentHashMap<>();

	public NamespaceInfo buildNamespace(Collection<CompilationUnitTree> units) {
		HashMap<SpaceInfo, List<DeclarationTree<?>>> namespacesToFill = new HashMap<>();

		NamespaceInfo root = new NamespaceInfo("",null,true);

		for (CompilationUnitTree unit : units) {
			if (unit == null) continue;
			NamespaceInfo current = root;
			if (unit.getPackageTree() != null) {
				PackageTree pk = unit.getPackageTree();
				for (ReferenceExprTree pkName : pk.getName().getReferences()) {
					NamespaceInfo space = current.getNamespace(pkName.getIdentifier().text);
					if (space == null)
						current = current.addNamespace(new NamespaceInfo(pkName.getIdentifier().text,current,true));
					else current = space;
				}
			}

			if (namespacesToFill.containsKey(current)) {
				namespacesToFill.get(current).addAll(unit.getDeclarations());
			} else {
				namespacesToFill.put(current,unit.getDeclarations());
			}
		}

		performNamespaces(namespacesToFill);
		performTypenames(namespacesToFill);
		performFunctionDeclarations(namespacesToFill);
		performFields(namespacesToFill);
		performImports(namespacesToFill);


		//add function impl
		while (!implementations.isEmpty()) {
			List<FunctionImplementationInfo> implemented = new LinkedList<>();
			implementations.forEach((func, impl) -> {
				BlockStatement.build(func.getImplementation(), func, impl);
				implemented.add(func);
			});
			implemented.forEach(implementations::remove);
		}


		//resolve all

		return root;
	}

	private void performImports(HashMap<SpaceInfo, List<DeclarationTree<?>>> namespacesToFill) {
		for (Map.Entry<SpaceInfo, List<DeclarationTree<?>>> entry : namespacesToFill.entrySet()) {
			fillSpaceImports(entry.getKey(),entry.getValue());
		}
	}

	private void performFields(HashMap<SpaceInfo, List<DeclarationTree<?>>> namespacesToFill) {
		for (Map.Entry<SpaceInfo, List<DeclarationTree<?>>> entry : namespacesToFill.entrySet()) {
			fillSpaceFields(entry.getKey(),entry.getValue());
		}
	}

	private void performFunctionDeclarations(HashMap<SpaceInfo, List<DeclarationTree<?>>> namespacesToFill) {
		for (Map.Entry<SpaceInfo, List<DeclarationTree<?>>> entry : namespacesToFill.entrySet()) {
			fillSpaceFunctionDeclarations(entry.getKey(),entry.getValue());
		}
	}


	private void performNamespaces(HashMap<SpaceInfo, List<DeclarationTree<?>>> namespacesToFill) {
		HashMap<SpaceInfo, List<DeclarationTree<?>>> subSpaces = new HashMap<>();
		for (Map.Entry<SpaceInfo,List<DeclarationTree<?>>> entry : namespacesToFill.entrySet()) {
			var spaces = fillSpaceNamespaces(entry.getKey(),entry.getValue());
			if (!spaces.isEmpty()) {
				performNamespaces(spaces);
				subSpaces.putAll(spaces);
			}
		}

		for (Map.Entry<SpaceInfo, List<DeclarationTree<?>>> spaceInfoListEntry : subSpaces.entrySet()) {
			if (namespacesToFill.containsKey(spaceInfoListEntry.getKey())) {
				namespacesToFill.get(spaceInfoListEntry.getKey()).addAll(spaceInfoListEntry.getValue());
			} else
				namespacesToFill.put(spaceInfoListEntry.getKey(),spaceInfoListEntry.getValue());
		}
	}
	private void performTypenames(HashMap<SpaceInfo, List<DeclarationTree<?>>> namespacesToFill) {
		for (Map.Entry<SpaceInfo,List<DeclarationTree<?>>> entry : namespacesToFill.entrySet()) {
			fillSpaceTypenames(entry.getKey(),entry.getValue());
		}
	}


	private HashMap<SpaceInfo, List<DeclarationTree<?>>> fillSpaceNamespaces(SpaceInfo current, List<DeclarationTree<?>> declarations) {
		HashMap<SpaceInfo, List<DeclarationTree<?>>> namespaces = new HashMap<>();
		for (DeclarationTree<?> declaration : declarations) {
			if (declaration instanceof NamespaceDeclaration) {
				NamespaceDeclaration decl = (NamespaceDeclaration) declaration;
				switch (decl.getKeyword().text) {
					case "namespace":
						for (NamespaceDeclarator declarator : decl) {
							NamespaceInfo space = declarator.getName() != null ? current.getNamespace(declarator.getName().text) : null;
							boolean news = space == null;
							if (news) {
								space = buildNamespace_noFill(current,decl,declarator);
							}
							namespaces.put(space,declarator.getDeclarations());
							if (news) {
								current.addNamespace(space);
							}
						}
						break;
					/*case "struct":
						for (NamespaceDeclarator structDeclarator : decl) {
							current.addStruct(buildStruct(current,decl,structDeclarator));
						}
						break;*/
					default:
						throw new CompilerException(decl.getLocation(),decl.getKeyword().text + " are not supported yet");
				}
			}
		}
		return namespaces;
	}

	private void fillSpaceTypenames(SpaceInfo current, List<DeclarationTree<?>> declarations) {
		final Set<DeclarationTree<? extends DeclaratorTree>> forRemoval = new HashSet<>();

		for (DeclarationTree<? extends DeclaratorTree> declaration : declarations) {
			if (declaration instanceof TypenameDeclarationTree) {
				for (TypenameDeclaratorTree typenameDeclaratorTree : ((TypenameDeclarationTree) declaration)) {
					assert current instanceof AbstractSpaceInfo;
					assert typenameDeclaratorTree.getName() != null;
					TypenameInfo typename = new TypenameInfo(typenameDeclaratorTree.getName().text,
							current.resolveType(current, typenameDeclaratorTree.getTargetType()),
							current,
							typenameDeclaratorTree.getLocation());
					current.addTypename(typename);
				}
				forRemoval.add(declaration);
			}
		}
		declarations.removeAll(forRemoval);
	}

	private void fillSpaceFunctionDeclarations(SpaceInfo current, List<DeclarationTree<?>> declarations) {
		final Set<DeclarationTree<? extends DeclaratorTree>> forRemoval = new HashSet<>();

		for (DeclarationTree<?> declaration : declarations) {
			if (declaration instanceof ValuedDeclarationTree) {
				final Set<DeclaratorTree> forRemovalDeclarators = new HashSet<>();
				ValuedDeclarationTree decl = (ValuedDeclarationTree) declaration;
				for (DeclaratorTree declarator : decl) {
					if (declarator instanceof FunctionDeclaratorTree) {
						current.addFunction(new FunctionDeclarationInfo(current, decl, decl.getTypeElement(), (FunctionDeclaratorTree) declarator,false));
						forRemovalDeclarators.add(declarator);
					} else if (declarator instanceof FunctionDefinitionTree) {
						current.addFunction(buildFunctionImpl(current,decl,(FunctionDefinitionTree)declarator));
						forRemovalDeclarators.add(declarator);
					}
				}
				decl.getDeclarators().removeAll(forRemovalDeclarators);
			}
			if (declaration.getDeclarators().isEmpty())
				forRemoval.add(declaration);
		}
		declarations.removeAll(forRemoval);
	}


	private void fillSpaceImports(SpaceInfo current, List<DeclarationTree<?>> declarations) {
		final Set<DeclarationTree<? extends DeclaratorTree>> forRemoval = new HashSet<>();

		final Set<DeclaratorTree> forRemovalDeclarators = new HashSet<>();
		for (DeclarationTree<?> declaration : declarations) {
			if (declaration instanceof ImportDeclarationTree) {
				ImportDeclarationTree decl = (ImportDeclarationTree) declaration;
				for (ImportDeclaratorTree declarator : decl) {
					current.addImport(buildImport(current,decl,declarator));
					forRemovalDeclarators.add(declarator);
				}
				decl.getDeclarators().removeAll(forRemovalDeclarators);
				forRemovalDeclarators.clear();
			}
			if (declaration.getDeclarators().isEmpty())
				forRemoval.add(declaration);
		}
		declarations.removeAll(forRemoval);
		forRemoval.clear();
	}

	public static void fillSpaceFields(SpaceInfo current, List<DeclarationTree<?>> declarations) {
		final Set<DeclarationTree<? extends DeclaratorTree>> forRemoval = new HashSet<>();

		final Set<DeclaratorTree> forRemovalDeclarators = new HashSet<>();

		//add fields
		for (DeclarationTree<?> declaration : declarations) {
			if (declaration instanceof ValuedDeclarationTree) {
				ValuedDeclarationTree decl = (ValuedDeclarationTree) declaration;
				for (DeclaratorTree declarator : decl) {
					if (declarator instanceof VariableDeclaratorTree) {
						current.addField(buildField(current,decl,(VariableDeclaratorTree)declarator));
						forRemovalDeclarators.add(declarator);
					}
				}
				decl.getDeclarators().removeAll(forRemovalDeclarators);
				forRemovalDeclarators.clear();
			}
			if (declaration.getDeclarators().isEmpty())
				forRemoval.add(declaration);
		}
		declarations.removeAll(forRemoval);
		forRemoval.clear();


	}

	public static ImportInfo buildImport(SpaceInfo current, DeclarationTree<?> decl, ImportDeclaratorTree declarator) {
		StaticElementReferenceTree symbol = declarator.getSymbol();
		boolean star = false;
		List<SymbolInfo> imported = new LinkedList<>();
		if (symbol.getReferences().get(symbol.getReferences().size()-1).getIdentifier().type == TokenType.OPERATOR) {
			star = true;
			symbol.getReferences().remove(symbol.getReferences().size()-1);
			SpaceInfo parent = current.resolveSpace(current, symbol);
			imported.addAll(parent.getImports());
			imported.addAll(parent.getFunctions());
			imported.addAll(parent.getNamespaces());
			imported.addAll(parent.getTypenames());
		} else {
			String last = symbol.getReferences().remove(symbol.getReferences().size()-1).getIdentifier().text;
			SpaceInfo parent = current.resolveSpace(current, symbol);
			for (ImportInfo anImport : parent.getImports()) {
				if (last.equals(anImport.getName()) && anImport.isAccessibleFrom(current))
					imported.add(anImport);
			}
			for (FunctionInfo function : parent.getFunctions()) {
				if (last.equals(function.getName()) && function.isAccessibleFrom(current))
					imported.add(function);
			}
			for (NamespaceInfo namespace : parent.getNamespaces()) {
				if (last.equals(namespace.getName()) && namespace.isAccessibleFrom(current))
					imported.add(namespace);
			}
			for (TypenameInfo typename : parent.getTypenames()) {
				if (last.equals(typename.getName()) && typename.isAccessibleFrom(current))
					imported.add(typename);
			}
		}
		String alias = null;
		if (declarator.getName() != null)
			alias = declarator.getName().text;
		return new ImportInfo(current,imported,alias,star,declarator.getLocation());
	}

	public static TypeStructInfo buildStruct(SpaceInfo current, NamespaceDeclaration decl, NamespaceDeclarator declarator) {
		Objects.requireNonNull(declarator.getName());
		TypeStructInfo struct = new TypeStructInfo(declarator.getName().text,current,
				new NamespaceInfo(declarator.getName().text,current,true)
		);
		fillSpaceFields(struct,declarator.getDeclarations());
		return struct;
	}

	public static NamespaceInfo buildNamespace_noFill(SpaceInfo current, NamespaceDeclaration decl, NamespaceDeclarator declarator) {
		Objects.requireNonNull(declarator.getName());
		NamespaceInfo space = new NamespaceInfo(declarator.getName().text,current,decl.hasModifier("static"));
		return space;
	}


	public static FieldInfo buildField(SpaceInfo container, ValuedDeclarationTree decl,VariableDeclaratorTree declarator) {
		Objects.requireNonNull(declarator.getName());
		boolean _mut = decl.getTypeElement().get_mut() != null;
		TypeInfo type = container.resolveType(container,decl.getTypeElement());


		Expression init = null;
		if (declarator.getInitializer() != null) {
			init = Expression.build(container,container,declarator.getInitializer(),type);
			if (type == null) {
				type = init.getReturnType();
			}
		} else {
			if (!_mut)
				throw new CompilerException(declarator.getLocation(), "non-mut variables must have an initializer");
		}

		if (type == null) {
			throw new CompilerException(decl.getTypeElement().getLocation(), "'auto' is not allowed here");
		}

		TypeRefInfo typeRef = new TypeRefInfo(
				_mut, decl.getTypeElement().get_const() != null,decl.getTypeElement().get_restrict() != null, type
		);
		boolean _static = decl.hasModifier("static");
		return new FieldInfo(declarator.getLocation(), declarator.getName().text, typeRef, init, _static, false, container);
	}
	/*public static VariableInfo buildVariableInfo(VariableDeclaratorTree declarator, TypeRefInfo type, SpaceInfo container, Expression init, boolean _static) {
		return new VariableInfo(declarator.getLocation(), declarator.getName().text, type, init, _static, false);
	}*/

	public static FunctionImplementationInfo buildFunctionImpl(SpaceInfo current, ValuedDeclarationTree decl, FunctionDefinitionTree definition) {
		FunctionImplementationInfo info = new FunctionImplementationInfo(
				new FunctionDeclarationInfo(current, decl, decl.getTypeElement(), definition.getDeclarator(),true)
		);
		implementations.put(info,definition.getBlockStatement());
		return info;
	}
}
