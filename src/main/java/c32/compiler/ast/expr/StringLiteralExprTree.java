package c32.compiler.ast.expr;

import c32.compiler.CompilerException;
import c32.compiler.ast.type.TypeTree;
import c32.compiler.tokenizer.Token;

public class StringLiteralExprTree extends ExprTree {
	private final Token token;
	public StringLiteralExprTree(Token token, TypeTree retType) {
		super(retType);
		this.token = token;
	}

	public String getText() {
		return token.text;
	}

	@Override
	public String brewJava() {
		StringBuilder builder = new StringBuilder().append("new char[]{");
		for (int i = 0; i < getText().length(); i++) {
			builder.append("((char)").append((int)getText().charAt(i)).append(")");
			if (i != getText().length() - 1) builder.append(',');
		}System.out.println();
		return builder.append('}').toString();
	}
}
