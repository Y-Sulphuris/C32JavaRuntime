package c32.compiler.logical.tree.expression;

import c32.compiler.logical.tree.TypeRefInfo;
import c32.compiler.logical.tree.VariableInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VariableRefExpression implements Expression {
	private final VariableInfo variable;
	@Override
	public TypeRefInfo getReturnType() {
		return variable.getTypeRef();
	}
}
