package c32.compiler.logical.tree.expression;

import c32.compiler.except.CompilerException;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;

@Getter
@RequiredArgsConstructor
public class NumericLiteralExpression implements Expression {

    private final BigInteger number;
    private final TypeInfo returnType;

    public NumericLiteralExpression(Token token) {
        this(token,null);
    }

    public NumericLiteralExpression(Token token, final TypeInfo returnType0) {
        String number = token.text;
        TypeInfo.NumericPrimitiveTypeInfo returnType = null;

        int postfixNum = 0;
        switch (number.charAt(number.length()-1)) {
            case 'l': {
                returnType = TypeInfo.PrimitiveTypeInfo.LONG;
                ++postfixNum;
            } break;
            case 'i': {
                returnType = TypeInfo.PrimitiveTypeInfo.INT;
                ++postfixNum;
            } break;
            case 's': {
                returnType = TypeInfo.PrimitiveTypeInfo.SHORT;
                ++postfixNum;
            } break;
            case 'b': {
                returnType = TypeInfo.PrimitiveTypeInfo.BYTE;
                ++postfixNum;
            } break;
            case 'f': {
                returnType = TypeInfo.PrimitiveTypeInfo.FLOAT;
                ++postfixNum;
            } break;
            case 'd': {
                returnType = TypeInfo.PrimitiveTypeInfo.DOUBLE;
                ++postfixNum;
            } break;
        }
        if (postfixNum != 0) {
            number = number.substring(0,number.length()-postfixNum);
        }
        if (returnType == null)
            returnType = TypeInfo.PrimitiveTypeInfo.INT;

        if (number.charAt(number.length()-1) == 'u') {
            if (!(returnType instanceof TypeInfo.IntegerPrimitiveTypeInfo))
                throw new CompilerException(token.location, "no unsigned type for " + returnType);
            returnType = returnType.getUnsigned();
	        number = number.substring(0,number.length()-1);
        }
		assert returnType != null;
        this.returnType = returnType;

        if (returnType0 != null && !this.returnType.canBeImplicitCastTo(returnType0))
            throw new CompilerException(token.location,"cannot apply type '" + returnType.getName() + "' to '" + returnType0.getCanonicalName() + '\'');

        try {
	        this.number = new BigInteger(number);
        } catch (NumberFormatException e) {
            throw new CompilerException(token.location,"invalid number format",e);
        }
    }
}
