package c32.compiler.logical.tree.statement;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.FunctionImplementationInfo;
import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;

@Getter
public class AssignStatement implements Statement{
	private final FunctionImplementationInfo function;
	private final BlockStatement container;

	private final Expression lvalue;
	private final Expression rvalue;

	public AssignStatement(Location location, FunctionImplementationInfo function, BlockStatement container, Expression lvalue, Expression rvalue) {
		this.function = function;
		this.container = container;
		this.lvalue = lvalue;
		if (!lvalue.isAssignable()) throw new CompilerException(location,lvalue + " is not an lvalue");
		this.rvalue = rvalue;
	}
}
