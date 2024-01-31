package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.logical.tree.TypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TernaryExpression implements Expression {
    private final Expression cond;
    private final Expression ifTrue;
    private final Expression ifFalse;

    @Override
    public TypeInfo getReturnType() {
        return ifTrue.getReturnType();
    }

    @Override
    public Location getLocation() {
        return Location.between(cond.getLocation(),ifFalse.getLocation());
    }
}
