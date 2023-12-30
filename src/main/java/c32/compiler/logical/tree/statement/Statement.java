package c32.compiler.logical.tree.statement;

import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.BinaryExpression;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.parser.ast.ModifierTree;
import c32.compiler.parser.ast.declaration.ValuedDeclarationTree;
import c32.compiler.parser.ast.declarator.DeclaratorTree;
import c32.compiler.parser.ast.declarator.VariableDeclaratorTree;
import c32.compiler.parser.ast.expr.BinaryExprTree;
import c32.compiler.parser.ast.statement.*;
import lombok.var;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface Statement {

	FunctionImplementationInfo getFunction();

	BlockStatement getContainer();

	static Statement build(FunctionImplementationInfo function, BlockStatement container, StatementTree statement) {
		if (statement instanceof BlockStatementTree) {
			return BlockStatement.build(function,container,(BlockStatementTree)statement);
		} else if (statement instanceof DeclarationStatementTree) {
			//local variable
			var decl = (ValuedDeclarationTree)((DeclarationStatementTree) statement).getDeclaration();
			List<VariableInfo> variables = new ArrayList<>();
			for (DeclaratorTree declaratorTree : decl) {
				var varDec = ((VariableDeclaratorTree) declaratorTree);
				TypeInfo type = container.resolveType(container,decl.getTypeElement());

				ModifierTree mod_static = decl.eatModifier("static");
				if (mod_static != null && mod_static.getAttributes() != null)
					throw new CompilerException(mod_static.getLocation(),
							"unknown attributes: " + mod_static.getAttributes().stream().map(a -> a.text).collect(Collectors.toList()));

				ModifierTree mod_register = decl.eatModifier("register");
				if (mod_register != null) {
					if (mod_register.getAttributes() != null)
						throw new CompilerException(mod_register.getLocation(),
							"unknown attributes: " + mod_register.getAttributes().stream().map(a -> a.text).collect(Collectors.toList()));
					if (type instanceof TypeArrayInfo && !((TypeArrayInfo) type).isStaticArray()) {
						throw new CompilerException(mod_register.getLocation(),
								"'register' is not available for dynamic arrays");
					}
				}

				if (!decl.getModifiers().isEmpty())
					throw new CompilerException(decl.getLocation(), "unknown modifiers: " + decl.getModifiers().stream().map(m -> m.getKeyword().text).collect(Collectors.toList()));
				if (varDec.getName() != null) variables.add(new VariableInfo(varDec.getName().text,
						new TypeRefInfo(
								decl.getTypeElement().get_const() != null,
								decl.getTypeElement().get_restrict() != null,
								type
						),
						varDec.getInitializer() != null ? Expression.build(container,container, varDec.getInitializer(),type) : null,
						mod_static != null,mod_register != null
				));
			}
			return new VariableDeclarationStatement(function, container, variables);
		} else if (statement instanceof IfStatementTree) {
			Expression condition = Expression.build(container,container,((IfStatementTree) statement).getCondition(), TypeInfo.PrimitiveTypeInfo.BOOL);
			Statement block = Statement.build(function,container,((IfStatementTree) statement).getStatement());
			Statement elseBlock = null;
			if (((IfStatementTree) statement).getElseStatement() != null) {
				elseBlock = Statement.build(function,container,((IfStatementTree) statement).getElseStatement());
			}
			return new IfStatement(function, container, condition,block,elseBlock);
		} else if (statement instanceof WhileStatementTree) {
			Expression condition = Expression.build(container,container, ((WhileStatementTree) statement).getCondition(), TypeInfo.PrimitiveTypeInfo.BOOL);
			Statement block = Statement.build(function, container, ((WhileStatementTree) statement).getStatement());
			return new WhileStatement(function, container, condition, block);
		} else if (statement instanceof BreakStatementTree) {
			return new BreakStatement(function, container);
		} else if (statement instanceof ReturnStatementTree) {
			Expression expr = null;
			TypeInfo retType = function.getReturnType();
			if (((ReturnStatementTree) statement).getExpression() != null) {
				expr = Expression.build(container,container,((ReturnStatementTree) statement).getExpression(),retType);
			} else {
				if (!retType.equals(TypeInfo.PrimitiveTypeInfo.VOID)) {
					throw new CompilerException(statement.getLocation(), "expression expected");
				}
			}
			return new ReturnStatement(function,container,expr);
		} else if (statement instanceof ExpressionStatementTree) {
			Expression expr = Expression.build(container,container,((ExpressionStatementTree) statement).getExpression(),null);
			return new ExpressionStatement(function,container,expr);
		}

		throw new UnsupportedOperationException(statement.getClass().toString());
	}
}
