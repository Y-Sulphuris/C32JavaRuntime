package c32.compiler.ast.expr;

import c32.compiler.ast.type.TypeTree;

public class BoolExprTree extends ExprTree{
	private final boolean value;
	private BoolExprTree(boolean value) {
		super(TypeTree.BOOL);
		this.value = value;
	}

	public boolean getValue() {
		return value;
	}

	public static BoolExprTree get(boolean value) {
		return value ? TRUE : FALSE;
	}

	public static final BoolExprTree TRUE = new BoolExprTree(true), FALSE = new BoolExprTree(false);

	@Override
	public String brewJava() {
		return String.valueOf(value);
	}
}
