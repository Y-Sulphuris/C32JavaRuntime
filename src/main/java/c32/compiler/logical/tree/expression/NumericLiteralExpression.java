package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.logical.tree.TypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;

@Getter
@RequiredArgsConstructor
public class NumericLiteralExpression implements LiteralExpression {

    private final BigInteger number;
    private final TypeInfo returnType;
	private final Location location;

    public NumericLiteralExpression(Token token) {
        this(token,null);
    }

    public NumericLiteralExpression(Token token, final TypeInfo returnType0) {
		this.location = token.location;
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

        if (returnType0 != null && !this.returnType.canBeImplicitlyCastTo(returnType0))
            throw new CompilerException(token.location,"cannot apply type '" + returnType.getName() + "' to '" + returnType0.getCanonicalName() + '\'');

		int radix = 10;
		if (number.startsWith("0x")) {
			number = number.substring(2);
			radix = 16;
		} else if (number.startsWith("0b")) {
			number = number.substring(1);
			radix = 2;
		}

        try {
	        this.number = new BigInteger(number, radix);
        } catch (NumberFormatException e) {
            throw new CompilerException(token.location,"invalid number format",e);
        }

		rangeCheck();
    }
	private void rangeCheck() {
		long max = 0, min = 0;
		if (getReturnType() == TypeInfo.PrimitiveTypeInfo.BYTE) {
			max = Byte.MAX_VALUE;
			min = Byte.MIN_VALUE;
		} else if (getReturnType() == TypeInfo.PrimitiveTypeInfo.UBYTE) {
			max = 0xFF;
		} else if (getReturnType() == TypeInfo.PrimitiveTypeInfo.SHORT) {
			max = Short.MAX_VALUE;
			min = Short.MIN_VALUE;
		} else if (getReturnType() == TypeInfo.PrimitiveTypeInfo.USHORT) {
			max = 0xFFFF;
		} else if (getReturnType() == TypeInfo.PrimitiveTypeInfo.INT) {
			max = Integer.MIN_VALUE;
			min = Integer.MIN_VALUE;
		} else if (getReturnType() == TypeInfo.PrimitiveTypeInfo.UINT) {
			max = 0xFFFFFFFFL;
		} else if (getReturnType() == TypeInfo.PrimitiveTypeInfo.LONG) {
			max = Long.MAX_VALUE;
			min = Long.MIN_VALUE;
		} else if (getReturnType() == TypeInfo.PrimitiveTypeInfo.ULONG) {
			if (number.compareTo(new BigInteger("0xFFFFFFFFFFFFFFFF")) > 0)
				throw new CompilerException(location, "ulong overflow");
		}
		if (max != min) {
			if(number.longValue() > max || number.longValue() < min)
				throw new CompilerException(location,getReturnType().getCanonicalName() + " overflow");
		}
	}
}
