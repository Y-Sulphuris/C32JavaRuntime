package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.logical.tree.FunctionInfo;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public class CallExpression implements Expression {
	private final FunctionInfo function;
	private final List<Expression> args;
	private final Location location;

	@Override
	public void forEachSubExpression(Consumer<Expression> act) {
		args.forEach(act);
	}

	@Override
	public TypeInfo getReturnType() {
		return function.getReturnType();
	}
}
