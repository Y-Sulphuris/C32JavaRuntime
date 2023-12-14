package c32.compiler.ast;

import c32.compiler.ast.expr.ExprTree;
import com.squareup.javapoet.CodeBlock;

import java.util.Collection;
import java.util.Collections;

public class ReturnTree implements StatementTree {
	private final ExprTree ret;
	public ReturnTree(ExprTree ret) {
		this.ret = ret;
	}

	@Override
	public Collection<CodeBlock> brewJava() {
		return Collections.singleton(CodeBlock.builder().add("return " + ret.brewJava()).build());
	}
}
