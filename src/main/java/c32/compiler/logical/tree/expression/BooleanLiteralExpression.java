package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.logical.tree.TypeInfo;
import lombok.Getter;

@Getter
public class BooleanLiteralExpression implements LiteralExpression {
	private final TypeInfo returnType;
	private final boolean value;
	private final Location location;

	public BooleanLiteralExpression(Token literal, TypeInfo returnType) {
		this.location = literal.location;
		if (returnType != null && !returnType.canBeImplicitlyCastTo(TypeInfo.PrimitiveTypeInfo.BOOL))
			throw new CompilerException(literal.location,"cannot implicit cast 'bool' to '" + returnType.getCanonicalName() + "'");
		this.returnType = TypeInfo.PrimitiveTypeInfo.BOOL;//returnType;
		this.value = literal.text.equals("true");
	}
}
