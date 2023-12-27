package c32.compiler.logical.tree.expression;

import c32.compiler.except.CompilerException;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.logical.tree.TypeInfo;
import lombok.Getter;

@Getter
public class CharLiteralExpression implements Expression {
    private final TypeInfo returnType;
    private final char ch;
    public CharLiteralExpression(Token literal, TypeInfo returnType) {
        this.returnType = returnType;
        if (literal.text.length() != 1)
            throw new CompilerException(literal.location, "char expected");
        this.ch = literal.text.charAt(0);
    }
}
