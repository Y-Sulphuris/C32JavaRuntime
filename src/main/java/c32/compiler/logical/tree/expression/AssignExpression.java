package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.IllegalOperatorException;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.VariableInfo;
import c32.compiler.logical.tree.Weak;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Getter
public class AssignExpression implements Expression {

    private final Expression lvalue;
    @Nullable
    private final BinaryOperator parentOperator;
    private final Expression rvalue;
	private final Location location;

	@Override
	public Set<Weak<VariableInfo>> collectChangeVariables() {
		Set<Weak<VariableInfo>> write = new HashSet<>();
		Expression lvalue = this.lvalue;
		while(true) {
			if (lvalue instanceof VariableRefExpression) {
				write.add(((VariableRefExpression) lvalue).getVariable().weakReference());
				break;
			} else if (lvalue instanceof IndexExpression) {
				lvalue = ((IndexExpression) lvalue).getArray();
			} else break;
		}
		write.addAll(this.lvalue.collectChangeVariables());
		write.addAll(this.rvalue.collectChangeVariables());
		return write;
	}

	@Override
	public void forEachSubExpression(Consumer<Expression> act) {
		act.accept(lvalue);
		act.accept(rvalue);
	}

	public AssignExpression(Location location, Expression lvalue, String parentOperator, Expression rvalue) {
		this.location = location;
        this.lvalue = lvalue;
        if (!lvalue.isAssignable())
            throw new CompilerException(location,lvalue + " is not assignable");
        if (parentOperator.isEmpty())
            this.parentOperator = null;
        else this.parentOperator = BinaryOperator.findOperator(location,lvalue,parentOperator,rvalue);

        if (this.parentOperator != null) {
            if (!this.parentOperator.getReturnType().equals(this.parentOperator.getLeftType())
                    || !this.parentOperator.getLeftType().canBeImplicitlyCastTo(lvalue.getReturnType()))
                throw new IllegalOperatorException(location,lvalue.getReturnType(),parentOperator,rvalue.getReturnType(),true);
        }

        this.rvalue = rvalue;
    }

    @Override
    public TypeInfo getReturnType() {
        return parentOperator == null ? lvalue.getReturnType() : parentOperator.getReturnType();
    }

}
