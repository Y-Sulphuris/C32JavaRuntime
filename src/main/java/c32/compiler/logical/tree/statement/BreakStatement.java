package c32.compiler.logical.tree.statement;

import c32.compiler.Location;
import c32.compiler.logical.tree.FunctionImplementationInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BreakStatement implements Statement {
	private final FunctionImplementationInfo function;
	private final BlockStatement container;
	private final Location location;
}
