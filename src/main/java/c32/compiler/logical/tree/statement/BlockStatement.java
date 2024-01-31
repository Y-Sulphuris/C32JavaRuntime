package c32.compiler.logical.tree.statement;

import c32.compiler.Location;
import c32.compiler.logical.TreeBuilder;
import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.VariableRefExpression;
import c32.compiler.parser.ast.declaration.DeclarationTree;
import c32.compiler.parser.ast.declaration.ImportDeclarationTree;
import c32.compiler.parser.ast.declaration.ValuedDeclarationTree;
import c32.compiler.parser.ast.declarator.DeclaratorTree;
import c32.compiler.parser.ast.declarator.FunctionDefinitionTree;
import c32.compiler.parser.ast.declarator.ImportDeclaratorTree;
import c32.compiler.parser.ast.declarator.VariableDeclaratorTree;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import c32.compiler.parser.ast.statement.BlockStatementTree;
import c32.compiler.parser.ast.statement.DeclarationStatementTree;
import c32.compiler.parser.ast.statement.StatementTree;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Getter
public class BlockStatement extends AbstractSpaceInfo implements Statement, SpaceInfo {
	private final FunctionImplementationInfo function;
	private final BlockStatement container;
	private final Location location;

	private final Collection<Statement> statements = new ArrayList<>();

	@Override
	public void resolveAll() {
		for (Statement statement : statements) {
			statement.resolveAll();
		}
	}

	public Statement addStatement(Statement statement) {
		if (statement == null) return null;
		this.statements.add(statement);
		return statement;
	}

	public static BlockStatement build(FunctionImplementationInfo function, BlockStatement container, BlockStatementTree statement) {
		BlockStatement block = new BlockStatement(function,container,statement.getLocation());
		return build(block, function, statement);
	}
	public static BlockStatement build(BlockStatement block, FunctionImplementationInfo func, BlockStatementTree st) {
		for (StatementTree statement : st.getStatements()) {
			if (statement instanceof DeclarationStatementTree) {
				DeclarationTree<?> decl = ((DeclarationStatementTree) statement).getDeclaration();
				if (decl instanceof ImportDeclarationTree) {
					for (ImportDeclaratorTree declarator : ((ImportDeclarationTree) decl).getDeclarators()) {
						func.addImport(TreeBuilder.buildImport(func, (DeclarationTree<?>) decl,declarator));
					}
					continue;
				} else if (decl instanceof ValuedDeclarationTree) {
					for (DeclaratorTree declarator : ((ValuedDeclarationTree) decl).getDeclarators()) {
						if (declarator instanceof FunctionDefinitionTree) {
							func.addFunction(TreeBuilder.buildFunctionImpl(func, (ValuedDeclarationTree) decl, (FunctionDefinitionTree) declarator));
						} else if (declarator instanceof VariableDeclaratorTree) {
							if(decl.hasModifier("static"))
								func.addField(TreeBuilder.buildField(func, (ValuedDeclarationTree) decl,(VariableDeclaratorTree)declarator));
						}
					}
					continue;
				}
			}
		}
		for (StatementTree statement : st.getStatements()) {
			block.addStatement(Statement.build(func,block,statement));
		}
		block.resolveAll();

		return block;
	}

	@Override
	public SpaceInfo getParent() {
		return container == null ? function : container;
	}


	@Override
	public String getName() {
		return "_" + getParent().getName() + "$impl_";
	}

	@Override
	public boolean isAccessibleFrom(SpaceInfo space) {
		return true;
	}

	@Override
	public VariableRefExpression resolveVariable(SpaceInfo caller, ReferenceExprTree reference) {
		for (Statement statement : getStatements()) {
			if (statement instanceof VariableDeclarationStatement) {
				for (VariableInfo variableInfo : ((VariableDeclarationStatement) statement).getVariables()) {
					if (variableInfo.getName().equals(reference.getIdentifier().text))
						return new VariableRefExpression(variableInfo,reference.getLocation());
				}
			}
		}

		return getParent().resolveVariable(caller, reference);
	}

	public VariableInfo getLocalVariable(String name) {
		for (Statement statement : getStatements()) {
			if (statement instanceof VariableDeclarationStatement) {
				for (VariableInfo variableInfo : ((VariableDeclarationStatement) statement).getVariables()) {
					if (variableInfo.getName().equals(name))
						return variableInfo;
				}
			}
		}
		return null;
	}

	public VariableInfo getVisibleLocalVariable(String name) {
		VariableInfo var = getLocalVariable(name);
		if (var == null && getParent() instanceof BlockStatement)
			return ((BlockStatement) getParent()).getLocalVariable(name);
		return var;
	}

	public Collection<VariableInfo> collectLocalVariables() {
		List<VariableInfo> variables = new LinkedList<>();
		forEachLocalVariableDeclaration(variables::add);
		return variables;
	}

	public void forEachLocalVariableDeclaration(Consumer<VariableInfo> consumer) {
		for (Statement statement : statements) {
			if (statement instanceof VariableDeclarationStatement) for (VariableInfo variable : ((VariableDeclarationStatement) statement).getVariables())
					consumer.accept(variable);
			else if (statement instanceof BlockStatement) {
				((BlockStatement) statement).forEachLocalVariableDeclaration(consumer);
			}
		}
	}
}
