package c32.compiler.logical.tree.statement;

import c32.compiler.logical.tree.FunctionImplementationInfo;
import c32.compiler.logical.tree.VariableInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class VariableDeclarationStatement implements Statement {
	private final FunctionImplementationInfo function;
	private final BlockStatement container;

	private final Collection<VariableInfo> variables;
}
