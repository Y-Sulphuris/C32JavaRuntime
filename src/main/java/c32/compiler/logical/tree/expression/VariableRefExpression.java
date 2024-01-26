package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.logical.tree.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Set;

@Getter
public class VariableRefExpression extends SymbolRefExpression {
	public VariableRefExpression(VariableInfo symbol, Location location) {
		super(symbol, location);
	}

	@Override
	public VariableInfo get() {
		return (VariableInfo) super.get();
	}

	public VariableInfo getVariable() {
		return get();
	}

	@Override
	public Set<Weak<VariableInfo>> collectUsingVariables() {
		return Collections.singleton(getVariable().weakReference());
	}

	@Override
	public TypeInfo getReturnType() {
		return getVariable().getTypeRef().getType();
	}

	@Override
	public boolean isAssignable() {
		VariableInfo variable = getVariable();
		if (variable.getTypeRef().getType() instanceof TypeArrayInfo) {
			return !((TypeArrayInfo) variable.getTypeRef().getType()).getElementType().is_const();
		}
		return !variable.getTypeRef().is_const();
	}
}
