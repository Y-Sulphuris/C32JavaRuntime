package c32.compiler.logical.tree.statement;

import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.VariableRefExpression;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import c32.compiler.parser.ast.statement.BlockStatementTree;
import c32.compiler.parser.ast.statement.StatementTree;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
@Getter
public class BlockStatement extends AbstractSpaceInfo implements Statement, SpaceInfo {
	private final FunctionImplementationInfo function;
	private final BlockStatement container;

	private final Collection<Statement> statements = new ArrayList<>();

	public Statement addStatement(Statement statement) {
		this.statements.add(statement);
		return statement;
	}

	public static BlockStatement build(FunctionImplementationInfo function, BlockStatement container, BlockStatementTree statement) {
		BlockStatement block = new BlockStatement(function,container);

		for (StatementTree state : statement.getStatements()) {
			block.addStatement(Statement.build(function,block,state));
		}

		return block;
	}

	@Override
	public SpaceInfo getParent() {
		return container == null ? function : container;
	}

	@Override
	public FunctionInfo addFunction(FunctionInfo function) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NamespaceInfo addNamespace(NamespaceInfo namespace) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldInfo addField(FieldInfo field) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return getParent().getName();
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
						return new VariableRefExpression(variableInfo);
				}
			}
		}
		return getParent().resolveVariable(caller, reference);
	}
}