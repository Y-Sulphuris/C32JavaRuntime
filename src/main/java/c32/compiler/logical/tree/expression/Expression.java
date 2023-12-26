package c32.compiler.logical.tree.expression;

import c32.compiler.logical.tree.SpaceInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import c32.compiler.parser.ast.expr.ExprTree;
import c32.compiler.parser.ast.expr.LiteralExprTree;
import c32.compiler.parser.ast.expr.ReferenceExprTree;

public interface Expression {
    TypeRefInfo getReturnType();

	static Expression build(SpaceInfo container, ExprTree exprTree, TypeRefInfo returnType) {
		if (exprTree instanceof LiteralExprTree) {
			switch (((LiteralExprTree) exprTree).getType()) {
				case BOOLEAN_LITERAL:
					return new BooleanLiteralExpression(((LiteralExprTree) exprTree).getLiteral(),returnType);
				case CHAR_LITERAL:
				case STRING_LITERAL:
					throw new UnsupportedOperationException();
				case INTEGER_LITERAL:
					return new NumericLiteralExpression(((LiteralExprTree) exprTree).getLiteral(),returnType);
			}
		} else if (exprTree instanceof ReferenceExprTree) {
			return container.resolveVariable((ReferenceExprTree)exprTree);
		}
		throw new UnsupportedOperationException(exprTree.getClass().getName());
	}
}
