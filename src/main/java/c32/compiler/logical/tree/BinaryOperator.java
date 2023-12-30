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

	@Override
	public String toString() {
		return "BinaryOperator{" + returnType.getCanonicalName() + "(" + leftType.getCanonicalName() + op + rightType.getCanonicalName() + ")";
	}

	private static final Set<BinaryOperator> registeredOperators = new HashSet<>();
    private static void registerBinary(TypeInfo left, String op, TypeInfo right, TypeInfo returnType) {
        registeredOperators.add(new BinaryOperator(left,op,right,returnType));

        //это костыль
        registeredOperators.add(new BinaryOperator(left,op+'=',right,returnType));
        //это костыль
    }
    static {
        TypeInfo.PrimitiveTypeInfo.forEachNumeric(TYPE -> {
            registerBinary(TYPE,"+", TYPE, TYPE);
            registerBinary(TYPE,"-", TYPE, TYPE);
            registerBinary(TYPE,"*", TYPE, TYPE);
            registerBinary(TYPE,"/", TYPE, TYPE);
            registerBinary(TYPE,"%", TYPE, TYPE);
            registerBinary(TYPE,"^^", TYPE, TYPE);

            registerBinary(TYPE,">=",TYPE,BOOL);
            registerBinary(TYPE,"<=",TYPE,BOOL);
            registerBinary(TYPE,">",TYPE,BOOL);
            registerBinary(TYPE,"<",TYPE,BOOL);
        });
        TypeInfo.PrimitiveTypeInfo.forEachValued(TYPE -> {
            registerBinary(TYPE,"==",TYPE,BOOL);
            registerBinary(TYPE,"!=",TYPE,BOOL);
        });

        TypeInfo.PrimitiveTypeInfo.forEachInteger(TYPE -> {
            registerBinary(TYPE,"<<", TYPE, TYPE);
            registerBinary(TYPE,"<<<", TYPE, TYPE);
            registerBinary(TYPE,"<<<<", TYPE, TYPE);
            registerBinary(TYPE,">>", TYPE, TYPE);
            registerBinary(TYPE,">>>", TYPE, TYPE);
            registerBinary(TYPE,">>>>", TYPE, TYPE);

            registerBinary(TYPE,"|", TYPE, TYPE);
            registerBinary(TYPE,"^", TYPE, TYPE);
            registerBinary(TYPE,"&", TYPE, TYPE);
        });
        registerBinary(BOOL,"||", BOOL, BOOL);
        registerBinary(BOOL,"&&", BOOL, BOOL);
    }
    public static BinaryOperator findOperator(Location location, Expression lvalue, String operator, Expression rvalue) {
		Set<BinaryOperator> possiblyOperators = new HashSet<>();
	    for (BinaryOperator op : registeredOperators) {
		    if (!op.getOp().equals(operator)) continue;
		    possiblyOperators.add(op);
	    }

        for (BinaryOperator op : possiblyOperators) {
            if (lvalue.getReturnType().equals(op.getLeftType()) && rvalue.getReturnType().equals(op.getRightType())) {
				/*System.out.println("EE operator for " +
						lvalue.getReturnType().getCanonicalName() +
						" " + operator + " " +
						rvalue.getReturnType().getCanonicalName() + " = " + op);*/
                return op;
            }
        }
	    for (BinaryOperator op : possiblyOperators) {
		    if (lvalue.getReturnType().canBeImplicitCastTo(op.getLeftType()) && rvalue.getReturnType().equals(op.getRightType())) {
			    /*System.out.println("IE operator for " +
					    lvalue.getReturnType().getCanonicalName() +
					    " " + operator + " " +
					    rvalue.getReturnType().getCanonicalName() + " = " + op);*/
			    return op;
		    }
	    }
	    for (BinaryOperator op : possiblyOperators) {
		    if (lvalue.getReturnType().equals(op.getLeftType()) && rvalue.getReturnType().canBeImplicitCastTo(op.getRightType())) {
			    /*System.out.println("EI operator for " +
					    lvalue.getReturnType().getCanonicalName() +
					    " " + operator + " " +
					    rvalue.getReturnType().getCanonicalName() + " = " + op);*/
			    return op;
		    }
	    }
        for (BinaryOperator op : possiblyOperators) {
            if (lvalue.getReturnType().canBeImplicitCastTo(op.getLeftType()) && rvalue.getReturnType().canBeImplicitCastTo(op.getRightType())) {
	            /*System.out.println("II operator for " +
			            lvalue.getReturnType().getCanonicalName() +
			            " " + operator + " " +
			            rvalue.getReturnType().getCanonicalName() + " = " + op);*/
                return op;
            }
        }
        throw new IllegalOperatorException(location, lvalue.getReturnType(),operator,rvalue.getReturnType(),false);
    }
}
