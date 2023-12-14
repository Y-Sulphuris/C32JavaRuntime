package c32.compiler.ast.expr;

import c32.compiler.ast.StatementTree;
import com.squareup.javapoet.CodeBlock;

import java.util.Collection;
import java.util.Collections;

public class ExprStatementTree implements StatementTree {
	private final ExprTree expr;
	public ExprStatementTree(ExprTree expr) {
		this.expr = expr;
	}

	@Override
	public Collection<CodeBlock> brewJava() {
		return Collections.singleton(CodeBlock.builder().add(expr.brewJava()).build());
	}
}
