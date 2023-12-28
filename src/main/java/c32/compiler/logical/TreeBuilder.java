package c32.compiler.logical;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.logical.tree.expression.NumericLiteralExpression;
import c32.compiler.logical.tree.statement.BlockStatement;
import c32.compiler.logical.tree.statement.Statement;
import c32.compiler.logical.tree.statement.VariableDeclarationStatement;
import c32.compiler.parser.ast.CompilationUnitTree;
import c32.compiler.parser.ast.PackageTree;
import c32.compiler.parser.ast.declaration.DeclarationTree;
import c32.compiler.parser.ast.declaration.NamespaceDeclaration;
import c32.compiler.parser.ast.declaration.ValuedDeclarationTree;
import c32.compiler.parser.ast.declarator.*;
import c32.compiler.parser.ast.expr.ExprTree;
import c32.compiler.parser.ast.expr.LiteralExprTree;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import c32.compiler.parser.ast.statement.BlockStatementTree;
import c32.compiler.parser.ast.statement.DeclarationStatementTree;
import c32.compiler.parser.ast.statement.IfStatementTree;
import c32.compiler.parser.ast.statement.StatementTree;
import lombok.var;

import java.util.*;

public class TreeBuilder {

	private final HashMap<FunctionImplementationInfo, BlockStatementTree> implementations = new HashMap<>();

	public SpaceInfo buildNamespace(Collection<CompilationUnitTree> units) {
		NamespaceInfo root = new NamespaceInfo("",null);

		for (CompilationUnitTree unit : units) {
			NamespaceInfo current = root;
			if (unit.getPackageTree() != null) {
				PackageTree pk = unit.getPackageTree();
				for (ReferenceExprTree pkName : pk.getName().getReferences()) {
					NamespaceInfo space = current.getNamespace(pkName.getIdentifier().text);
					if (space == null)
						current = current.addNamespace(new NamespaceInfo(pkName.getIdentifier().text,current));
					else current = space;
				}
			}

			fillNamespace(current,unit.getDeclarations());
		}
		implementations.forEach((func, impl) -> {
			for (StatementTree statement : impl.getStatements()) {
				func.getImplementation().addStatement(Statement.build(func,func.getImplementation(),statement));
			}
		});

		//resolve all

		return root;
	}




	private void fillNamespace(NamespaceInfo current, List<DeclarationTree<?>> declarations) {
		final Set<DeclarationTree<? extends DeclaratorTree>> forRemoval = new HashSet<>();

		//add functions
		final Set<DeclaratorTree> forRemovalDeclarators = new HashSet<>();
		for (DeclarationTree<?> declaration : declarations) {
			if (declaration instanceof ValuedDeclarationTree) {
				ValuedDeclarationTree decl = (ValuedDeclarationTree) declaration;
				for (DeclaratorTree declarator : decl) {
					if (declarator instanceof FunctionDeclaratorTree) {
						current.addFunction(new FunctionDeclarationInfo(current, decl.getModifiers(), decl.getTypeElement(), (FunctionDeclaratorTree) declarator,false));
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
							current.addNamespace(buildNamespace(current,namespaceDeclarator));
						}
						break;
					default:
						throw new CompilerException(decl.getLocation(),decl.getKeyword().text + " are not supported yet");
				}
			} else throw new UnsupportedOperationException(declaration.toString());
		}
	}

	private FieldInfo buildField(SpaceInfo container, ValuedDeclarationTree decl,VariableDeclaratorTree declarator) {
		Objects.requireNonNull(declarator.getName());
		TypeRefInfo type = new TypeRefInfo(
				decl.getTypeElement().get_const() != null, decl.getTypeElement().get_restrict() != null, container.resolveType(container,decl.getTypeElement())
		);
		Expression init = null;
		if (declarator.getInitializer() != null) init = Expression.build(container,declarator.getInitializer(),type.getType());
		return new FieldVariableInfo(
				buildVariableInfo(declarator.getName().text,type,container,init), container
		);
	}
	private VariableInfo buildVariableInfo(String name, TypeRefInfo type, SpaceInfo container, Expression init) {
		return new VariableInfo(name, type, init);
	}

	private NamespaceInfo buildNamespace(SpaceInfo current, NamespaceDeclarator decl) {
		assert decl.getName() != null;
		NamespaceInfo space = new NamespaceInfo(decl.getName().text,current);
		fillNamespace(space,decl.getDeclarations());
		return space;
	}
	private FunctionImplementationInfo buildFunctionImpl(SpaceInfo current, ValuedDeclarationTree decl, FunctionDefinitionTree definition) {
		FunctionImplementationInfo info = new FunctionImplementationInfo(
				new FunctionDeclarationInfo(current, decl.getModifiers(), decl.getTypeElement(), definition.getDeclarator(),true)
		);
		implementations.put(info,definition.getBlockStatement());
		return info;
	}
}
