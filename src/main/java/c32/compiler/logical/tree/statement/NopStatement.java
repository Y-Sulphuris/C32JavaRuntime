package c32.compiler.logical.tree.statement;

import c32.compiler.Location;
import c32.compiler.logical.tree.FunctionImplementationInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NopStatement implements Statement {
	private final FunctionImplementationInfo function;
	private final BlockStatement container;
	private final Location location;
	private final boolean explicit;
}
