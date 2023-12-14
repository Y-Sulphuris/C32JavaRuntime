package c32.compiler.ast;

import c32.compiler.ast.expr.ExprTree;
import c32.compiler.ast.type.TypeTree;

public class NumberExprTree extends ExprTree {
	private final String text;
	public NumberExprTree(TypeTree retType, String text) {
		super(retType);
		this.text = text;
	}

	@Override
	public String brewJava() {
		return "("+getRetType().getJavaType().toString()+")"+ text;
	}
}
