package c32.compiler.logical.tree.statement;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.TypeNotFoundException;
import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.parser.ast.ModifierTree;
import c32.compiler.parser.ast.declaration.ValuedDeclarationTree;
import c32.compiler.parser.ast.declarator.DeclaratorTree;
import c32.compiler.parser.ast.declarator.VariableDeclaratorTree;
import c32.compiler.parser.ast.statement.*;
import lombok.var;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface Statement {

	FunctionImplementationInfo getFunction();

	BlockStatement getContainer();

	Location getLocation();

	default void resolveAll() {
		//nop;
	}

	static Statement build(FunctionImplementationInfo function, BlockStatement container, StatementTree statement) {
		if (statement instanceof BlockStatementTree) {
			return BlockStatement.build(function,container,(BlockStatementTree)statement);
		} else if (statement instanceof DeclarationStatementTree) {
			if (((DeclarationStatementTree) statement).getDeclaration() instanceof ValuedDeclarationTree); else return null;
			//local variable
			var decl = (ValuedDeclarationTree)((DeclarationStatementTree) statement).getDeclaration();
			List<VariableInfo> variables = new ArrayList<>();
			for (DeclaratorTree declaratorTree : decl) {
				if (declaratorTree instanceof VariableDeclaratorTree); else break;
				var varDec = ((VariableDeclaratorTree) declaratorTree);
				TypeInfo type = container.resolveType(container,decl.getTypeElement());

				ModifierTree mod_static = decl.eatModifier("static");
				if (mod_static != null) {
					if (mod_static.getAttributes() != null) {
						throw new CompilerException(mod_static.getLocation(),
								"unknown attributes: " + mod_static.getAttributes().stream().map(a -> a.text).collect(Collectors.toList()));
					}
					return null;//static variable has already performed
				}

				ModifierTree mod_register = decl.eatModifier("register");
				boolean registerAllowed = true;
				if (mod_register != null) {
					if (mod_register.getAttributes() != null) {
						if (mod_register.getAttributes().size() == 1) {
							String attribute = mod_register.getAttributes().get(0).text;
							if (attribute.equals("true")) {
								//just an explicit forcing register
							} else if (attribute.equals("false")){
								registerAllowed = false;
							} else {
								throw new CompilerException(mod_register.getLocation(),
										"unknown attributes: " + mod_register.getAttributes().stream().map(a -> a.text).collect(Collectors.toList()));
							}
						} else {
							throw new CompilerException(mod_register.getLocation(),
									"unknown attributes: " + mod_register.getAttributes().stream().map(a -> a.text).collect(Collectors.toList()));
						}
					}
					if (type instanceof TypeArrayInfo && !((TypeArrayInfo) type).isStaticArray()) {
						throw new CompilerException(mod_register.getLocation(),
								"'register' is not available for dynamic arrays");
					}
				}

				if (!decl.getModifiers().isEmpty())
					throw new CompilerException(decl.getLocation(), "unknown modifiers: " + decl.getModifiers().stream().map(m -> m.getKeyword().text).collect(Collectors.toList()));

				Expression init = null;
				if (varDec.getInitializer() != null) {
					init = Expression.build(container,container, varDec.getInitializer(),type);
					if (type == null) {
						type = init.getReturnType();
					}
				}
				if (type == null) {
					throw new CompilerException(decl.getTypeElement().getLocation(), "'auto' is not allowed here");
				}
				if (!type.canHaveVariable()) {
					throw new CompilerException(decl.getLocation(),"cannot declare variable with type " + type.getCanonicalName());
				}

				if (varDec.getName() != null) {
					String name = varDec.getName().text;
					if (container.getVisibleLocalVariable(name) != null) {
						throw new CompilerException(varDec.getLocation(),"variable '" + name + "' has already defined");
					}
					variables.add(new VariableInfo(decl.getLocation(), varDec.getName().text,
							new TypeRefInfo(decl.getTypeElement().get_mut() != null,
									decl.getTypeElement().get_const() != null,
									decl.getTypeElement().get_restrict() != null,
									type
							),
							init,
							mod_static != null, registerAllowed ? mod_register != null : null
					));
				}
			}
			return new VariableDeclarationStatement(function, container, variables, statement.getLocation());
		} else if (statement instanceof IfStatementTree) {
			Expression condition = Expression.build(container,container,((IfStatementTree) statement).getCondition(), TypeInfo.PrimitiveTypeInfo.BOOL);
			if (!condition.getReturnType().canBeImplicitlyCastTo(TypeInfo.PrimitiveTypeInfo.BOOL))
				throw new CompilerException(condition.getLocation(),"condition must be a bool value");
			Statement block = Statement.build(function,container,((IfStatementTree) statement).getStatement());
			Statement elseBlock = null;
			if (((IfStatementTree) statement).getElseStatement() != null) {
				elseBlock = Statement.build(function,container,((IfStatementTree) statement).getElseStatement());
			}
			return new IfStatement(function, container, condition,block,elseBlock,statement.getLocation());
		} else if (statement instanceof WhileStatementTree) {
			Expression condition = Expression.build(container,container, ((WhileStatementTree) statement).getCondition(), TypeInfo.PrimitiveTypeInfo.BOOL);
			Statement block = Statement.build(function, container, ((WhileStatementTree) statement).getStatement());
			return new WhileStatement(function, container, condition, block,statement.getLocation());
		} else if (statement instanceof BreakStatementTree) {
			return new BreakStatement(function, container, statement.getLocation());
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
			return new ReturnStatement(function,container,expr,statement.getLocation());
		} else if (statement instanceof ExpressionStatementTree) {
			Expression expr = Expression.build(container,container,((ExpressionStatementTree) statement).getExpression(),null);
			return new ExpressionStatement(function,container,expr);
		} else if (statement instanceof LabelStatementTree) {
			String label = ((LabelStatementTree) statement).getLabelName().text;
			return new LabelStatement(function,container,label,statement.getLocation());
		} else if (statement instanceof GotoStatementTree) {
			String label = ((GotoStatementTree) statement).getLabelName().text;
			return new GotoStatement(function,container,label,statement.getLocation());
		} else if (statement instanceof NopStatementTree) {
			return new NopStatement(function,container,statement.getLocation(),((NopStatementTree) statement).getNop() != null);
		}

		throw new UnsupportedOperationException(statement.getClass().toString());
	}
}
