package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.logical.tree.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class VariableRefExpression implements Expression {
	private final VariableInfo variable;
	private final Location location;

	@Override
	public Set<Weak<VariableInfo>> collectUsingVariables() {
		return Collections.singleton(variable.weakReference());
	}

	@Override
	public TypeInfo getReturnType() {
		return variable.getTypeRef().getType();
	}

	@Override
	public boolean isAssignable() {
		if (variable.getTypeRef().getType() instanceof TypeArrayInfo) {
			return !((TypeArrayInfo) variable.getTypeRef().getType()).getElementType().is_const();
		}
		return !variable.getTypeRef().is_const();
	}
}
