package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.lexer.tokenizer.TokenType;
import c32.compiler.logical.VariableNotFoundException;
import c32.compiler.logical.tree.*;
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
			ReferenceExprTree refExprTree = (ReferenceExprTree) exprTree;
			if (refExprTree.getIdentifier().type == TokenType.KEYWORD) {
				return findCompileTimeConstant(refExprTree.getIdentifier().text,refExprTree.getIdentifier().location);
			}
			try {
				VariableRefExpression var = container.resolveVariable(container, refExprTree);
				if (returnType != null) {
					if (!var.getReturnType().canBeImplicitlyCastTo(returnType)){
						throw new CompilerException(refExprTree.getLocation(), "cannot implicit cast '" + var.getReturnType().getCanonicalName() + "' to '" + returnType.getCanonicalName() + ";");
					}
				}
				return var;
			} catch (VariableNotFoundException e) {
				if (returnType != null) throw e;
				SpaceInfo space = container.resolveSpace(container,(ReferenceExprTree)exprTree);
				return new SpaceRefExpression(space, refExprTree.getLocation());
			}
		} else if (exprTree instanceof BinaryExprTree) {
			BinaryExprTree binExprTree = (BinaryExprTree) exprTree;
/*
			if (binExprTree.getOperator().text.equals(".") || binExprTree.getOperator().text.equals("::")) {
				if (binExprTree.getLhs() instanceof ReferenceExprTree) {
					//TO-DO: namespace types
					// different semantic for '::' and '.'
					// :: - get static member from expression with type 'space'
					// . - non-static members
					SpaceInfo space = container.resolveSpace(container,((ReferenceExprTree) binExprTree.getLhs()));
					Expression ret = Expression.build(caller,space,binExprTree.getRhs(),returnType);
					space.addUsage(ret);
					return ret;
				}
			}//это наверное надо будет педелелать, но это не точно (точно)
			//всё, переделал
*/

			Expression lhs = Expression.build(caller, container, binExprTree.getLhs(),null);
			Expression rhs;
			if (binExprTree.getOperator().text.equals("::")) {
				if (!(lhs instanceof SpaceRefExpression))
					throw new CompilerException(lhs.getLocation(),"namespace expected");
				SpaceInfo space = ((SpaceRefExpression) lhs).getSpace();
				rhs = Expression.build(caller,space,binExprTree.getRhs(),returnType);
				return rhs;
			} else {
				rhs = Expression.build(caller, container, binExprTree.getRhs(), null);
			}
			if (binExprTree.getOperator().text.endsWith("=")) {
				switch (binExprTree.getOperator().text) {
					case "==":
					case "!=":
					case "<=":
					case ">=":
					case "===":
					case "!==":
						break;
					default:
						String op = binExprTree.getOperator().text;
						op = op.substring(0,op.length()-1);
						return new AssignExpression(binExprTree.getLocation(), lhs, op, rhs);
				}
			}
			return new BinaryExpression(binExprTree.getLocation(), lhs, binExprTree.getOperator().text, rhs, returnType).calculate();
		} else if (exprTree instanceof CallExprTree) {
			List<Expression> args = new ArrayList<>();
			for (ExprTree argument : ((CallExprTree) exprTree).getArgumentList().getArguments()) {
				args.add(Expression.build(caller,caller,argument,null));
			}
			return new CallExpression(container.resolveFunction(caller, (CallExprTree)exprTree, args), args, exprTree.getLocation());
		}
		else if (exprTree instanceof UnaryPrefixExprTree)
		{
			Expression expression = Expression.build(caller,container,((UnaryPrefixExprTree) exprTree).getExpression(),null);
			boolean _const = false;
			if (expression instanceof VariableRefExpression) {
				_const = ((VariableRefExpression) expression).getVariable().getTypeRef().is_const();
			}
			return new UnaryPrefixExpression(exprTree.getLocation(), _const, expression,((UnaryPrefixExprTree) exprTree).getOperator().text);
		}
		else if (exprTree instanceof CastExprTree)
		{
			TypeInfo type = container.resolveType(container,((CastExprTree) exprTree).getTargetType());
			return new ExplicitCastExpression(exprTree.getLocation(),type,Expression.build(caller,container,((CastExprTree) exprTree).getExpression(),null));
		}
		else if (exprTree instanceof IndexExprTree)
		{
			Expression array = Expression.build(caller,container,((IndexExprTree) exprTree).getExpr(), null);
			List<Expression> args = ((IndexExprTree) exprTree).getArgs().getIndexes()
					.stream().map(a ->
							Expression.build(caller,container,a,null))
					.collect(Collectors.toList());
			return new IndexExpression(exprTree.getLocation(),array,args);
		}
		else if (exprTree instanceof InitializerListExprTree)
		{
			TypeElementTree listTypeTree = ((InitializerListExprTree) exprTree).getExplicitType();
			TypeInfo listType = null;

			if (listTypeTree != null) {
				listType = caller.resolveType(caller,listTypeTree);
				if (returnType != null && !listType.canBeImplicitlyCastTo(returnType)) {
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

	static Expression findCompileTimeConstant(String text, Location location) {
		if (text.equals("null")) {
			return new NullLiteralExpression(location);
		}
		throw new UnsupportedOperationException();
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
			return this.getReturnType().canBeImplicitlyCastTo(type);
		return false;
	}

	Location getLocation();
}
