package c32.compiler.ast;

import com.squareup.javapoet.CodeBlock;

import java.util.Collection;

public interface StatementTree extends Tree {
	Collection<CodeBlock> brewJava();
}

