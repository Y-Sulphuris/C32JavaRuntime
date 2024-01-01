package c32.compiler.logical.tree.expression;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.SpaceInfo;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.VariableInfo;
import c32.compiler.logical.tree.Weak;
import c32.compiler.parser.ast.expr.*;
import c32.compiler.parser.ast.type.TypeElementTree;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface Expression {
    TypeInfo getReturnType();

	//if it can be calculated in compile time, returns literal, else returns nullptr
	default Expression asCompileTimeLiteralExpression() {
		return null;
	}
	default Expression calculate() {
		return this;
	}

	default Set<Weak<VariableInfo>> collectUsingVariables() {
		Set<Weak<VariableInfo>> using = new HashSet<>();
		forEachSubExpression(expression -> {
			using.addAll(expression.collectUsingVariables());
		});
		return using;
	}

	default Set<Weak<VariableInfo>> collectChangeVariables() {
		Set<Weak<VariableInfo>> write = new HashSet<>();
		forEachSubExpression(expression -> {
			write.addAll(expression.collectChangeVariables());
		});
		return write;
	}

	default void forEachSubExpression(Consumer<Expression> act) {

	}

	default void addUsingVariable(VariableInfo var) {
		if (var == null) return;
		throw new UnsupportedOperationException();
	}
	default void addChangeVariable(VariableInfo var) {
		if (var == null) return;
		throw new UnsupportedOperationException();
	}

	static Expression build(SpaceInfo caller, SpaceInfo container, ExprTree exprTree, TypeInfo returnType) {
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
				Expression ret = Expression.build(caller,space,((BinaryExprTree) exprTree).getRhs(),returnType);
				space.addUsage(ret);
				return ret;
			}//это наверное надо будет педелелать, но это не точно


			Expression lhs = Expression.build(caller, container, ((BinaryExprTree) exprTree).getLhs(), null),
			rhs = Expression.build(caller,container, ((BinaryExprTree) exprTree).getRhs(), null);
			if (((BinaryExprTree) exprTree).getOperator().text.endsWith("=")) {
				switch (((BinaryExprTree) exprTree).getOperator().text) {
					case "==":
					case "!=":
					case "<=":
					case ">=":
					case "===":
					case "!==":
						break;
					default:
						String op = ((BinaryExprTree) exprTree).getOperator().text;
						op = op.substring(0,op.length()-1);
						return new AssignExpression(exprTree.getLocation(), lhs, op, rhs);
				}
			}
			return new BinaryExpression(exprTree.getLocation(), lhs, ((BinaryExprTree) exprTree).getOperator().text, rhs, returnType).calculate();
		} else if (exprTree instanceof CallExprTree) {
			List<Expression> args = new ArrayList<>();
			for (ExprTree argument : ((CallExprTree) exprTree).getArgumentList().getArguments()) {
				args.add(Expression.build(caller,caller,argument,null));
			}
			return new CallExpression(container.resolveFunction(container, (CallExprTree)exprTree, args), args);
		} else if (exprTree instanceof UnaryPrefixExprTree) {
			Expression expression = Expression.build(caller,container,((UnaryPrefixExprTree) exprTree).getExpression(),null);
			return new UnaryPrefixExpression(expression,((UnaryPrefixExprTree) exprTree).getOperator().text);
		} else if (exprTree instanceof CastExprTree) {
			TypeInfo type = container.resolveType(container,((CastExprTree) exprTree).getTargetType());
			return new ExplicitCastExpression(exprTree.getLocation(),type,Expression.build(caller,container,((CastExprTree) exprTree).getExpression(),null));
		} else if (exprTree instanceof IndexExprTree) {
			Expression array = Expression.build(caller,container,((IndexExprTree) exprTree).getExpr(), null);
			List<Expression> args = ((IndexExprTree) exprTree).getArgs().getIndexes()
					.stream().map(a ->
							Expression.build(caller,container,a,null))
					.collect(Collectors.toList());
			return new IndexExpression(exprTree.getLocation(),array,args);
		} else if (exprTree instanceof InitializerListExprTree) {
			TypeElementTree listTypeTree = ((InitializerListExprTree) exprTree).getExplicitType();
			TypeInfo listType = null;
			if (listTypeTree != null) {
				listType = caller.resolveType(caller,listTypeTree);
				if (returnType != null && !listType.canBeImplicitCastTo(returnType)) {
					throw new CompilerException(listTypeTree.getLocation(),returnType.getCanonicalName() + " cannot be implicitly cast to " + listType.getCanonicalName());
				}
			}
			List<Expression> expressions = new ArrayList<>();
			for (ExprTree initializer : ((InitializerListExprTree) exprTree).getInitializers()) {
				expressions.add(Expression.build(caller,container,initializer,null));//todo: specify return type if possible
			}
			return new InitializerListExpression(exprTree.getLocation(), listType,expressions);
		}

		throw new UnsupportedOperationException(exprTree.getClass().getName());
	}

	default boolean isAssignable() {
		return false;
	}


	/*final*/ default void addUsageIfVariable(Expression expression) {
		if (expression instanceof VariableRefExpression)
			((VariableRefExpression) expression).getVariable().addUsage(this);
	}

	default boolean checkImplicitCastTo_mutable(TypeInfo type) {
		if (this.getReturnType() != null)
			return this.getReturnType().canBeImplicitCastTo(type);
		return false;
	}
}
