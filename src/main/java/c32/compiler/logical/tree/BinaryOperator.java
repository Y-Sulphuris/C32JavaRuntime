package c32.compiler.logical.tree;

import c32.compiler.Location;
import c32.compiler.logical.IllegalOperatorException;
import c32.compiler.logical.tree.expression.Expression;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import static c32.compiler.logical.tree.TypeInfo.PrimitiveTypeInfo.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class BinaryOperator {
    private final TypeInfo leftType;
    private final String op;
    private final TypeInfo rightType;
    private final TypeInfo returnType;

    private static final Set<BinaryOperator> registeredOperators = new HashSet<>();
    private static void registerBinary(TypeInfo left, String op, TypeInfo right, TypeInfo returnType) {
        registeredOperators.add(new BinaryOperator(left,op,right,returnType));
        registeredOperators.add(new BinaryOperator(left,op+'=',right,returnType));
    }
    static {
        registerBinary(INT,"+", INT, INT);
        registerBinary(INT,"-", INT, INT);
        registerBinary(INT,"*", INT, INT);
        registerBinary(INT,"/", INT, INT);
        registerBinary(INT,"%", INT, INT);
    }
    public static BinaryOperator findOperator(Location location, Expression lvalue, String operator, Expression rvalue) {
        for (BinaryOperator op : registeredOperators) {
            if (!op.getOp().equals(operator)) continue;
            if (op.getLeftType().equals(lvalue.getReturnType()) && op.getRightType().equals(rvalue.getReturnType())) {
                return op;
            }
        }
        for (BinaryOperator op : registeredOperators) {
            if (!op.getOp().equals(operator)) continue;
            if (op.getLeftType().canBeImplicitCastTo(lvalue.getReturnType()) && op.getRightType().canBeImplicitCastTo(rvalue.getReturnType())) {
                return op;
            }
        }
        throw new IllegalOperatorException(location, lvalue.getReturnType(),operator,rvalue.getReturnType());
    }
}
