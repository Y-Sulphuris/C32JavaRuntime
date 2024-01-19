package c32.compiler.logical.tree.expression;

import c32.compiler.except.CompilerException;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.logical.tree.TypeInfo;
import lombok.Getter;

@Getter
public class BooleanLiteralExpression implements LiteralExpression {
	private final TypeInfo returnType;
	private final boolean value;

	public BooleanLiteralExpression(Token literal, TypeInfo returnType) {
		if (returnType != null && !returnType.canBeImplicitlyCastTo(TypeInfo.PrimitiveTypeInfo.BOOL))
			throw new CompilerException(literal.location,"cannot implicit cast 'bool' to '" + returnType.getCanonicalName() + "'");
		this.returnType = returnType;
		this.value = literal.text.equals("true");
	}
}
