package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.IllegalOperatorException;
import c32.compiler.logical.tree.BinaryOperator;
import c32.compiler.logical.tree.TypeInfo;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class AssignExpression implements Expression {

    private final Expression lvalue;
    @Nullable
    private final BinaryOperator parentOperator;
    private final Expression rvalue;

    public AssignExpression(Location location, Expression lvalue, String parentOperator, Expression rvalue) {
        this.lvalue = lvalue;
        if (!lvalue.isAssignable())
            throw new CompilerException(location,lvalue + " is not an lvalue");
        if (parentOperator.isEmpty())
            this.parentOperator = null;
        else this.parentOperator = BinaryOperator.findOperator(location,lvalue,parentOperator,rvalue);

        if (this.parentOperator != null) {
            if (!this.parentOperator.getReturnType().equals(this.parentOperator.getLeftType())
                    || !this.parentOperator.getLeftType().canBeImplicitCastTo(lvalue.getReturnType()))
                throw new IllegalOperatorException(location,lvalue.getReturnType(),parentOperator,rvalue.getReturnType(),true);
        }

        this.rvalue = rvalue;
    }

    @Override
    public TypeInfo getReturnType() {
        return parentOperator.getReturnType();
    }
}
