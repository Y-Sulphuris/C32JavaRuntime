package c32.compiler.logical.tree.statement;

import c32.compiler.logical.tree.SpaceInfo;
import c32.compiler.parser.ast.statement.BlockStatementTree;
import c32.compiler.parser.ast.statement.StatementTree;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashSet;

@RequiredArgsConstructor
@Getter
public class BlockStatement implements Statement {
	private final Collection<Statement> statements = new HashSet<>();
	public Statement addStatement(Statement statement) {
		this.statements.add(statement);
		return statement;
	}

	public static BlockStatement build(SpaceInfo container, BlockStatementTree statement) {
		BlockStatement block = new BlockStatement();
		for (StatementTree state : statement.getStatements()) {
			block.addStatement(Statement.build(container,state));
		}
		return block;
	}
}
