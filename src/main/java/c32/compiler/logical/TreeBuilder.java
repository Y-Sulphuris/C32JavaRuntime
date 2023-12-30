package c32.compiler.logical;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.logical.tree.statement.Statement;
import c32.compiler.parser.ast.CompilationUnitTree;
import c32.compiler.parser.ast.ModifierTree;
import c32.compiler.parser.ast.PackageTree;
import c32.compiler.parser.ast.declaration.DeclarationTree;
import c32.compiler.parser.ast.declaration.NamespaceDeclaration;
import c32.compiler.parser.ast.declaration.ValuedDeclarationTree;
import c32.compiler.parser.ast.declarator.*;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import c32.compiler.parser.ast.statement.BlockStatementTree;
import c32.compiler.parser.ast.statement.StatementTree;

import java.util.*;

public class TreeBuilder {

	private final HashMap<FunctionImplementationInfo, BlockStatementTree> implementations = new HashMap<>();

	public NamespaceInfo buildNamespace(Collection<CompilationUnitTree> units) {
		NamespaceInfo root = new NamespaceInfo("",null,true);

		for (CompilationUnitTree unit : units) {
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

			fillSpace(current,unit.getDeclarations());
		}
		implementations.forEach((func, impl) -> {
			for (StatementTree statement : impl.getStatements()) {
				func.getImplementation().addStatement(Statement.build(func,func.getImplementation(),statement));
			}
		});

		//resolve all

		return root;
	}




	private void fillSpace(SpaceInfo current, List<DeclarationTree<?>> declarations) {
		final Set<DeclarationTree<? extends DeclaratorTree>> forRemoval = new HashSet<>();

		//add functions
		final Set<DeclaratorTree> forRemovalDeclarators = new HashSet<>();
		for (DeclarationTree<?> declaration : declarations) {
			if (declaration instanceof ValuedDeclarationTree) {
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
				forRemovalDeclarators.clear();
			}
			if (declaration.getDeclarators().isEmpty())
				forRemoval.add(declaration);
		}
		declarations.removeAll(forRemoval);
		forRemoval.clear();

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

		//add namespaces
		for (DeclarationTree<?> declaration : declarations) {
			if (declaration instanceof NamespaceDeclaration) {
				NamespaceDeclaration decl = (NamespaceDeclaration) declaration;
				switch (decl.getKeyword().text) {
					case "namespace":
						for (NamespaceDeclarator namespaceDeclarator : decl) {
							current.addNamespace(buildNamespace(current,decl,namespaceDeclarator));
						}
						break;
					case "struct":
						for (NamespaceDeclarator structDeclarator : decl) {
							current.addStruct(buildStruct(current,decl,structDeclarator));
						}
						break;
					default:
						throw new CompilerException(decl.getLocation(),decl.getKeyword().text + " are not supported yet");
				}
			} else throw new UnsupportedOperationException(declaration.toString());
		}
	}

	private TypeStructInfo buildStruct(SpaceInfo current, NamespaceDeclaration decl, NamespaceDeclarator declarator) {
		Objects.requireNonNull(declarator.getName());
		TypeStructInfo struct = new TypeStructInfo(declarator.getName().text,current,
				new NamespaceInfo(declarator.getName().text,current,true)
		);
		fillSpace(struct,declarator.getDeclarations());
		return struct;
	}

	private NamespaceInfo buildNamespace(SpaceInfo current, NamespaceDeclaration decl, NamespaceDeclarator declarator) {
		Objects.requireNonNull(declarator.getName());
		NamespaceInfo space = new NamespaceInfo(declarator.getName().text,current,decl.hasModifier("static"));
		fillSpace(space,declarator.getDeclarations());
		return space;
	}


	private FieldInfo buildField(SpaceInfo container, ValuedDeclarationTree decl,VariableDeclaratorTree declarator) {
		Objects.requireNonNull(declarator.getName());
		boolean _const = decl.getTypeElement().get_const() != null;
		TypeRefInfo type = new TypeRefInfo(
				_const, decl.getTypeElement().get_restrict() != null, container.resolveType(container,decl.getTypeElement())
		);
		Expression init = null;
		if (declarator.getInitializer() != null) {
			init = Expression.build(container,container,declarator.getInitializer(),type.getType());
		} else
			if (_const) throw new CompilerException(declarator.getLocation(), "const variables must have an initializer");

		boolean _static = decl.hasModifier("static");
		return new FieldVariableInfo(
				buildVariableInfo(declarator.getName().text,type,container,init,_static), container
		);
	}
	private VariableInfo buildVariableInfo(String name, TypeRefInfo type, SpaceInfo container, Expression init, boolean _static) {
		return new VariableInfo(name, type, init, _static, false);
	}

	private FunctionImplementationInfo buildFunctionImpl(SpaceInfo current, ValuedDeclarationTree decl, FunctionDefinitionTree definition) {
		FunctionImplementationInfo info = new FunctionImplementationInfo(
				new FunctionDeclarationInfo(current, decl, decl.getTypeElement(), definition.getDeclarator(),true)
		);
		implementations.put(info,definition.getBlockStatement());
		return info;
	}
}
