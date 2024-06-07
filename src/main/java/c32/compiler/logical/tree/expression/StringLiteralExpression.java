package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.logical.tree.TypeArrayInfo;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import lombok.Getter;

@Getter
public class StringLiteralExpression implements LiteralExpression {
	private final TypeInfo returnType;
	private final String string;
	private final Location location;
	public StringLiteralExpression(Token literal, TypeInfo returnType) {
		this.location = literal.location;

		TypeInfo arrType = TypeArrayInfo.arrayOf(-1,new TypeRefInfo(false, false,false,TypeInfo.PrimitiveTypeInfo.CHAR));
		if (returnType != null && !returnType.canBeImplicitlyCastTo(arrType))
			throw new CompilerException(literal.location,"cannot implicit cast 'const char[]' to '" + returnType.getCanonicalName() + "'");
		this.returnType = returnType != null ? returnType : arrType;
		this.string = literal.text;
	}
}
