package c32.compiler.ast.statement;

import c32.compiler.ast.StatementTree;
import c32.compiler.ast.expr.ExprTree;
import c32.compiler.tokenizer.Token;
import com.squareup.javapoet.CodeBlock;
import lombok.Data;

import java.util.Collection;
import java.util.Collections;

@Data
public final class IfStatement implements StatementTree {
	private final Token keyword;
	private final ExprTree assert_;
	private final StatementTree action;

	@Override
	public Collection<CodeBlock> brewJava() {
		CodeBlock.Builder builder = CodeBlock.builder().add("if(" + assert_.brewJava() + ") {");
		for (CodeBlock codeBlock : action.brewJava()) {
			builder.add(codeBlock);
			builder.add(";");
		}
		builder.add("}");
		return Collections.singleton(builder.build());
	}
}
