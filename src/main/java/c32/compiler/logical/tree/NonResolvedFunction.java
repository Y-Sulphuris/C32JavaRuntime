package c32.compiler.logical.tree;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.FunctionNotFoundException;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.parser.ast.expr.CallExprTree;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class NonResolvedFunction implements FunctionInfo, NonResolved<FunctionInfo> {
	private final SpaceInfo caller;
	private final CallExprTree call;
	private final List<Expression> args;

	@Override
	public String getName() {
		return null;
	}

	@Override
	public TypeInfo getReturnType() {
		return null;
	}

	@Override
	public List<VariableInfo> getArgs() {
		return null;
	}

	@Override
	public List<TypeInfo> getThrowTypes() {
		return null;
	}

	@Override
	public SpaceInfo getParent() {
		return null;
	}

	@Override
	public boolean is_pure() {
		return false;
	}

	@Override
	public boolean is_noexcept() {
		return false;
	}

	@Override
	public boolean is_extern() {
		return false;
	}

	@Override
	public boolean is_native() {
		return false;
	}

	@Override
	public CompilerException fail() {
		return new FunctionNotFoundException(call,args);
	}

	@Override
	public FunctionInfo tryToResolve() {
		return caller.resolveFunction(caller,call,args);
	}
}
