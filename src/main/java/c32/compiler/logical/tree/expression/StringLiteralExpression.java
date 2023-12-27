package c32.compiler.logical.tree.expression;

import c32.compiler.except.CompilerException;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.logical.tree.TypeArrayInfo;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import lombok.Getter;

@Getter
public class StringLiteralExpression implements Expression {
	private final TypeInfo returnType;
	private final String string;
	public StringLiteralExpression(Token literal, TypeInfo returnType) {
		TypeInfo arrType = TypeArrayInfo.arrayOf(new TypeRefInfo(true,false,TypeInfo.PrimitiveTypeInfo.CHAR));
		if (returnType != null && !returnType.canBeImplicitCastTo(arrType))
			throw new CompilerException(literal.location,"cannot implicit cast 'const char[]' to '" + returnType.getCanonicalName() + "'");
		this.returnType = returnType != null ? returnType : arrType;
		this.string = literal.text;
	}
}
