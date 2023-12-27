package c32.compiler.logical.tree.statement;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.BinaryOperator;
import c32.compiler.logical.tree.FunctionImplementationInfo;
import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;

@Getter
public class AssignStatement implements Statement{
	private final FunctionImplementationInfo function;
	private final BlockStatement container;

	private final Expression lvalue;
	private final Expression rvalue;

	private final BinaryOperator parentOperator;

	public AssignStatement(Location location, FunctionImplementationInfo function, BlockStatement container, Expression lvalue, Expression rvalue, String parentOperator) {
		this.function = function;
		this.container = container;
		this.lvalue = lvalue;
        this.parentOperator = BinaryOperator.findOperator(location,lvalue,parentOperator,rvalue);
        if (!lvalue.isAssignable()) throw new CompilerException(location,lvalue + " is not an lvalue");
		this.rvalue = rvalue;
	}
}
