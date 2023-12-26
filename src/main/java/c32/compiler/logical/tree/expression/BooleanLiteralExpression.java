package c32.compiler.logical.tree.expression;

import c32.compiler.except.CompilerException;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import lombok.Getter;

@Getter
public class BooleanLiteralExpression implements Expression {
	private final TypeRefInfo returnType;
	private final boolean value;

	public BooleanLiteralExpression(Token literal, TypeRefInfo returnType) {
		if (!returnType.getType().canBeImplicitCastTo(TypeInfo.PrimitiveTypeInfo.BOOL))
			throw new CompilerException(literal.location,"cannot implicit cast 'bool' to '" + returnType.getType().getCanonicalName() + "'");
		this.returnType = returnType;
		this.value = literal.text.equals("true");
	}
}
