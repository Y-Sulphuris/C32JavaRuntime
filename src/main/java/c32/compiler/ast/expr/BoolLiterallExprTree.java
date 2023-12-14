package c32.compiler.ast.expr;

import c32.compiler.ast.type.TypeTree;
import c32.compiler.tokenizer.Token;
import lombok.Getter;

public class BoolLiterallExprTree extends ExprTree {
	private final boolean value;
	@Getter
	private final Token token;

	public BoolLiterallExprTree(boolean value, Token token) {
		super(TypeTree.BOOL);
		this.value = value;
		this.token = token;
	}

	public boolean getValue() {
		return value;
	}

	@Override
	public String brewJava() {
		return String.valueOf(value);
	}
}
