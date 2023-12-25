package c32.compiler.logical;

import c32.compiler.logical.tree.*;
import c32.compiler.parser.ast.CompilationUnitTree;
import c32.compiler.parser.ast.PackageTree;
import c32.compiler.parser.ast.declaration.DeclarationTree;
import c32.compiler.parser.ast.declaration.NamespaceDeclaration;
import c32.compiler.parser.ast.declaration.ValuedDeclarationTree;
import c32.compiler.parser.ast.declarator.DeclaratorTree;
import c32.compiler.parser.ast.declarator.FunctionDeclaratorTree;
import c32.compiler.parser.ast.declarator.FunctionDefinitionTree;
import c32.compiler.parser.ast.declarator.NamespaceDeclarator;
import c32.compiler.parser.ast.expr.ReferenceExprTree;

import java.util.Collection;
import java.util.List;

public class TreeBuilder {
	public SpaceInfo buildNamespace(Collection<CompilationUnitTree> units) {
		SpaceInfo root = new NamespaceInfo("",null);

		for (CompilationUnitTree unit : units) {
			SpaceInfo current = root;
			if (unit.getPackageTree() != null) {
				PackageTree pk = unit.getPackageTree();
				for (ReferenceExprTree pkName : pk.getName().getReferences()) {
					current = current.addNamespace(new NamespaceInfo(pkName.getIdentifier().text,current));
				}
			}

			fillNamespace(current,unit.getDeclarations());
		}

		return root;
	}

	private void fillNamespace(SpaceInfo current, List<DeclarationTree<?>> declarations) {
		for (DeclarationTree<?> declaration : declarations) {
			if (declaration instanceof ValuedDeclarationTree) {
				ValuedDeclarationTree decl = (ValuedDeclarationTree) declaration;
				for (DeclaratorTree declarator : decl) {
					if (declarator instanceof FunctionDeclaratorTree) {
						current.addFunction(new FunctionDeclarationInfo(current, decl.getModifiers(), decl.getTypeElement(), (FunctionDeclaratorTree) declarator));
					} else if (declarator instanceof FunctionDefinitionTree) {
						current.addFunction(buildFunctionImpl(current,decl,(FunctionDefinitionTree)declarator));
					}else throw new UnsupportedOperationException(declarator.toString());
				}
			} else if (declaration instanceof NamespaceDeclaration) {
				NamespaceDeclaration decl = (NamespaceDeclaration) declaration;
				switch (decl.getKeyword().text) {
					case "namespace":
						for (NamespaceDeclarator namespaceDeclarator : decl) {
							current.addNamespace(buildNamespace(current,namespaceDeclarator));
						}
				}
			} else throw new UnsupportedOperationException(declaration.toString());
		}
	}

	private NamespaceInfo buildNamespace(SpaceInfo current, NamespaceDeclarator decl) {
		assert decl.getName() != null;
		NamespaceInfo space = new NamespaceInfo(decl.getName().text,current);
		fillNamespace(space,decl.getDeclarations());
		return space;
	}
	private FunctionImplementationInfo buildFunctionImpl(SpaceInfo current, ValuedDeclarationTree decl, FunctionDefinitionTree definition) {
		return new FunctionImplementationInfo(
				new FunctionDeclarationInfo(current, decl.getModifiers(), decl.getTypeElement(), definition.getDeclarator())
		);
	}
}
