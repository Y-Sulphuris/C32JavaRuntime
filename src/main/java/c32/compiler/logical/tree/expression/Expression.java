package c32.compiler.logical.tree.expression;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.VariableNotFoundException;
import c32.compiler.logical.tree.NamespaceInfo;
import c32.compiler.logical.tree.SpaceInfo;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import c32.compiler.parser.ast.expr.*;

import java.util.ArrayList;
import java.util.List;

public interface Expression {
    TypeInfo getReturnType();
	default Expression calculate() {
		return this;
	}

	static Expression build(SpaceInfo container, ExprTree exprTree, TypeInfo returnType) {
		if (exprTree instanceof LiteralExprTree) {
			switch (((LiteralExprTree) exprTree).getType()) {
				case BOOLEAN_LITERAL:
					return new BooleanLiteralExpression(((LiteralExprTree) exprTree).getLiteral(),returnType);
				case STRING_LITERAL:
					return new StringLiteralExpression(((LiteralExprTree) exprTree).getLiteral(),returnType);
				case CHAR_LITERAL:
					return new CharLiteralExpression(((LiteralExprTree) exprTree).getLiteral(),returnType);
				case INTEGER_LITERAL:
					return new NumericLiteralExpression(((LiteralExprTree) exprTree).getLiteral(),returnType);
			}
		} else if (exprTree instanceof ReferenceExprTree) {
			VariableRefExpression var = container.resolveVariable(container, (ReferenceExprTree)exprTree);
			if (returnType != null) {
				if (!var.getReturnType().canBeImplicitCastTo(returnType)){
					throw new CompilerException(exprTree.getLocation(), "cannot implicit cast '" + var.getReturnType().getCanonicalName() + "' to '" + returnType.getCanonicalName() + ";");
				}
			}
			return var;
		} else if (exprTree instanceof BinaryExprTree) {
			if (((BinaryExprTree) exprTree).getOperator().text.equals(".") && ((BinaryExprTree) exprTree).getLhs() instanceof ReferenceExprTree) {
				SpaceInfo space = container.resolveSpace(container,((ReferenceExprTree) ((BinaryExprTree) exprTree).getLhs()));
				return Expression.build(space,((BinaryExprTree) exprTree).getRhs(),returnType);
			}
			return new BinaryExpression(exprTree.getLocation(),
					Expression.build(container, ((BinaryExprTree) exprTree).getLhs(), null),
					((BinaryExprTree) exprTree).getOperator().text,
					Expression.build(container, ((BinaryExprTree) exprTree).getRhs(), null),
					returnType
			).calculate();
		} else if (exprTree instanceof CallExprTree) {
			List<Expression> args = new ArrayList<>();
			for (ExprTree argument : ((CallExprTree) exprTree).getArgumentList().getArguments()) {
				args.add(Expression.build(container,argument,null));
			}
			return new CallExpression(container.resolveFunction(container, (CallExprTree)exprTree, args), args);
		} else if (exprTree instanceof UnaryPrefixExprTree) {
			Expression expression = Expression.build(container,((UnaryPrefixExprTree) exprTree).getExpression(),null);
			return new UnaryPrefixExpression(expression,((UnaryPrefixExprTree) exprTree).getOperator().text);
		}

		throw new UnsupportedOperationException(exprTree.getClass().getName());
	}

	default boolean isAssignable() {
		return false;
	}
}
