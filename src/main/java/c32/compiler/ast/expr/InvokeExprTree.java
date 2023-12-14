package c32.compiler.ast.expr;


import c32.compiler.IntrinsicFunctionTable;
import c32.compiler.ast.FunctionDeclarationTree;
import c32.compiler.ast.expr.ExprTree;

public class InvokeExprTree extends ExprTree {
	private final FunctionDeclarationTree function;
	private final ExprTree[] args;
	public InvokeExprTree(FunctionDeclarationTree function, ExprTree[] args) {
		super(function.getReturnType());
		this.function = function;
		this.args = args;
	}

	public FunctionDeclarationTree getFunction() {
		return function;
	}

	@Override
	public String brewJava() {
		String fname = function.getJavaFunctionName();
		if (function.getModifiers().is_extern()) {
			fname = IntrinsicFunctionTable.getReplacement(function,fname);
		}
		StringBuilder builder = new StringBuilder(fname).append('(');
		if (args.length == 0) {
			return builder.append(')').toString();
		}
		for (int i = 0; ; i++) {
			builder.append(args[i].brewJavaAsArgument());
			if (i == args.length - 1) return builder.append(')').toString();
			builder.append(",");
		}
	}
}
