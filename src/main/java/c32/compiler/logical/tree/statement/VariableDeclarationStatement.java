package c32.compiler.logical.tree.statement;

import c32.compiler.logical.tree.VariableInfo;
import lombok.Getter;

import java.util.Collection;

@Getter
public class VariableDeclarationStatement implements Statement {
	private final Collection<VariableInfo> variable;

	public VariableDeclarationStatement(Collection<VariableInfo> variable) {
		this.variable = variable;
	}
}
