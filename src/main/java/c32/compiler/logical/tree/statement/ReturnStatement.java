package c32.compiler.logical.tree.statement;

import c32.compiler.Location;
import c32.compiler.logical.tree.FunctionImplementationInfo;
import c32.compiler.logical.tree.FunctionInfo;
import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public class ReturnStatement implements Statement {
	private final FunctionImplementationInfo function;
	private final BlockStatement container;
	@Nullable private final Expression expr;
	private final Location location;
}
