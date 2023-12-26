package c32.compiler.logical.tree.statement;

import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
@Getter
public class IfStatement implements Statement {
	private final Expression condition;
	private final Statement statement;
	@Nullable private final Statement elseStatement;
}
