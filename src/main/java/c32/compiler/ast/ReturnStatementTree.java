package c32.compiler.ast;

import c32.compiler.ast.expr.ExprTree;
import c32.compiler.tokenizer.Token;
import com.squareup.javapoet.CodeBlock;
import lombok.Data;

import java.util.Collection;
import java.util.Collections;

@Data
public class ReturnStatementTree implements StatementTree {
	private final Token keyword;
	private final ExprTree ret;
	private final Token endLine;

	@Override
	public Collection<CodeBlock> brewJava() {
		if (ret == null)
			return Collections.singleton(CodeBlock.builder().add("return").build());
		return Collections.singleton(CodeBlock.builder().add("return " + ret.brewJava()).build());
	}
}
