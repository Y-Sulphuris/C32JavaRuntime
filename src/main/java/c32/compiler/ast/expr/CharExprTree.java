package c32.compiler.ast.expr;

import c32.compiler.ast.type.TypeTree;

public class CharExprTree extends ExprTree {
	private final String text;
	public CharExprTree(TypeTree retType, String text) {
		super(retType == null ? TypeTree.CHAR : retType);
		this.text = text;
	}

	@Override
	public String brewJava() {
		return "'" + text + "'";
	}
}
