package c32.compiler.logical.tree.expression;

import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypePointerInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
//todo: re-code
public class UnaryPrefixExpression implements Expression {
    private final Expression expr;
    private final String operator;//операторы надо будет полностью переделать, а то это бред какой-то

    @Override
    public TypeInfo getReturnType() {
        if (operator.equals("*")) {
            return ((TypePointerInfo) expr.getReturnType()).getTargetType().getType();
        }
        return expr.getReturnType();
    }
}
