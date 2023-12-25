package c32.compiler.logical.tree.statement;

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
}
