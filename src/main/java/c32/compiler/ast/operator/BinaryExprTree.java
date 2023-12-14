package c32.compiler.ast.operator;

import c32.compiler.ast.expr.ExprTree;
import c32.compiler.ast.type.PrimitiveNumericTypeTree;
import c32.compiler.ast.type.TypeTree;

public class BinaryExprTree extends ExprTree {
	final ExprTree operand1, operand2;
	private final BinaryOperator operator;
	public BinaryExprTree(ExprTree operand1, ExprTree operand2, BinaryOperator operator) {
		super(operator.getReturnType(operand1,operand2));
		operator.checkAllowed(operand1,operand2,this.getRetType());
		this.operand1 = operand1;
		this.operand2 = operand2;
		this.operator = operator;
	}
	public BinaryExprTree(ExprTree operand1, ExprTree operand2, BinaryOperator operator, TypeTree retType) {
		super(retType);
		operator.checkAllowed(operand1,operand2,retType);
		this.operand1 = operand1;
		this.operand2 = operand2;
		this.operator = operator;
	}

	@Override
	public String brewJava() {
		TypeTree op1Ret = operand1.getRetType();
		if (op1Ret instanceof PrimitiveNumericTypeTree && ((PrimitiveNumericTypeTree) op1Ret).isUnsigned()) {
			String methodName = null;
			if (operator == BinaryOperator.DIVIDE) {
				methodName = "divideUnsigned";
			}
			if (operator == BinaryOperator.MOD) {
				methodName = "remainderUnsigned";
			}
			if (methodName != null) {
				PrimitiveNumericTypeTree ret = (PrimitiveNumericTypeTree) getRetType();
				String base = ret.getSize() > 4 ? "java.lang.Long." : "java.lang.Integer.";
				return "((" + getRetType().getJavaType().toString() + ")" + base + methodName + "(" + operand1.brewJava() + "," + operand2.brewJava() + "))";
			}
		}
		return operator.brewJava(operand1,operand2);
	}

	@Override
	public boolean isLeftValue() {
		return operator.isLeftValue(this);
	}

	static {
		BinaryOperator.clinit();
	}
}

