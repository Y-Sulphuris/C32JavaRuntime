package c32.compiler.ast.operator;

import c32.compiler.CompilerException;
import c32.compiler.ast.expr.ExprTree;
import c32.compiler.ast.StructTypeTree;
import c32.compiler.ast.type.TypeTree;

import java.util.HashMap;

class BitBinaryOperator extends BinaryOperator{

	BitBinaryOperator(String operator, int pr) {
		super(operator, pr);
	}

	@Override
	public boolean isAllowed(ExprTree operand1, ExprTree operand2, TypeTree retType) {
		return operand1.getRetType().canBeImplicitCastTo(TypeTree.LONG) && operand2.getRetType().canBeImplicitCastTo(TypeTree.LONG);
	}
}

class SelectBinaryOperator extends BinaryOperator {

	SelectBinaryOperator(String operator, int pr) {
		super(operator, pr);
	}

	@Override
	public boolean isLeftValue(BinaryExprTree binaryExprTree) {
		return binaryExprTree.operand2.isLeftValue();
	}

	@Override
	public TypeTree getReturnType(ExprTree operand1, ExprTree operand2) {
		return operand2.getRetType();
	}

	@Override
	public String brewJava(ExprTree operand1, ExprTree operand2) {
		if (operand1.getRetType() instanceof StructTypeTree) {
			return "_" + operand1.brewJava() + "_" + operand2.brewJava();
		}
		throw new AssertionError();
	}
}

class MutableLeftBinaryOperator extends BinaryOperator {
	MutableLeftBinaryOperator(String operator, int pr) {
		super(operator, pr);
	}

	@Override
	public boolean isAllowed(ExprTree operand1, ExprTree operand2, TypeTree retType) {
		return super.isAllowed(operand1, operand2, retType) && operand1.isLeftValue();
	}
}
public class BinaryOperator {

	private static final HashMap<String, BinaryOperator> operators = new HashMap<>();
	private static final HashMap<String, Integer> priority = new HashMap<>();


	public static final BinaryOperator
			NAMESPACE_SELECT = new SelectBinaryOperator("::", 120),

			SELECT = new SelectBinaryOperator(".", 100),
			POINTER_SELECT = new SelectBinaryOperator("->", 100),

			MULTIPLY = new BinaryOperator("*", 40),
			DIVIDE = new BinaryOperator("/", 40),
			MOD = new BinaryOperator("%", 40),

			PLUS = new BinaryOperator("+", 35),
			MINUS = new BinaryOperator("-", 35),

			SHL = new BinaryOperator("<<", 30),
			RHL = new BinaryOperator(">>", 30),

			SMALLER = new BinaryOperator("<", 25, TypeTree.BOOL),
			SMALLER_OR_EQUAL = new BinaryOperator("<=", 25, TypeTree.BOOL),
			GREATER = new BinaryOperator(">", 25, TypeTree.BOOL),
			GREATER_OR_EQUAL = new BinaryOperator(">=", 25, TypeTree.BOOL),

			EQUALS = new BinaryOperator("==", 20, TypeTree.BOOL),
			NOT_EQUALS = new BinaryOperator("!=", 20, TypeTree.BOOL),

			BIT_AND = new BitBinaryOperator("&", 15),
			BIT_XOR = new BitBinaryOperator("^", 15),
			BIT_OR = new BitBinaryOperator("|", 15),
			AND = new BinaryOperator("&&", 15, TypeTree.BOOL),
			OR = new BinaryOperator("||", 15, TypeTree.BOOL),

			ASSIGN = new MutableLeftBinaryOperator("=", 10),
			ASSIGN_MUL = new MutableLeftBinaryOperator("*=", 10),
			ASSIGN_DIV = new MutableLeftBinaryOperator("/=", 10),
			ASSIGN_MOD = new MutableLeftBinaryOperator("+=", 10),
			ASSIGN_SUM = new MutableLeftBinaryOperator("-=", 10),
			ASSIGN_SHL = new MutableLeftBinaryOperator("<<=", 10),
			ASSIGN_RHL = new MutableLeftBinaryOperator(">>=", 10),
			ASSIGN_BIT_AND = new MutableLeftBinaryOperator("&=", 10),
			ASSIGN_BIT_OR = new MutableLeftBinaryOperator("|=", 10),
			ASSIGN_BIT_XOR = new MutableLeftBinaryOperator("^=", 10);


	private final TypeTree forceType;
	private final String operator;

	public String getText() {
		return operator;
	}

	public static void clinit() {
	}


	BinaryOperator(String operator, int pr) {
		this(operator, pr, null);
	}



	BinaryOperator(String operator, int pr, TypeTree forceType) {
		this.operator = operator;
		this.forceType = forceType;
		operators.put(operator, this);
		priority.put(operator, pr);
	}

	public static int getPriority(String operator) {
		if (priority.containsKey(operator))
			return priority.get(operator);
		return -1;
	}

	public static BinaryOperator getOperator(String operator) {
		return operators.get(operator);
	}

	public String brewJava(ExprTree operand1, ExprTree operand2) {
		return '(' + operand1.brewJava() + ") " + operator + " (" + operand2.brewJava() + ')';
	}

	public boolean isAllowed(ExprTree operand1, ExprTree operand2, TypeTree retType) {
		return true;
	}

	public TypeTree getReturnType(ExprTree operand1, ExprTree operand2) {
		if (forceType != null)
			return forceType;
		return operand1.getRetType();
	}

	public void checkAllowed(ExprTree operand1, ExprTree operand2, TypeTree retType) {
		if (isAllowed(operand1,operand2,retType)) return;
		throw new CompilerException("Invalid operator");
	}

	public boolean isLeftValue(BinaryExprTree binaryExprTree) {
		return false;
	}
}
