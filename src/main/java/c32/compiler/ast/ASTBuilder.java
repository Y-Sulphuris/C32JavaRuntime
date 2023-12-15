package c32.compiler.ast;

import c32.compiler.Compiler;
import c32.compiler.CompilerException;
import c32.compiler.ast.expr.*;
import c32.compiler.ast.operator.BinaryExprTree;
import c32.compiler.ast.operator.BinaryOperator;
import c32.compiler.ast.statement.IfStatement;
import c32.compiler.ast.statement.VariableDeclarationStatementTree;
import c32.compiler.ast.type.StructTypeTree;
import c32.compiler.ast.type.TypeDynamicArrayTree;
import c32.compiler.ast.type.TypeTree;
import c32.compiler.tokenizer.Token;
import c32.compiler.tokenizer.TokenType;
import c32.compiler.tokenizer.UnexpectedTokenException;

import java.util.*;

public class ASTBuilder {
	private Token[] tokens;
	private int curTok = 0;
	private Token nextToken() {
		try {
			return tokens[curTok++];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new CompilerException("Где код????????");
		}
	}
	private Token seeNextToken() {
		return tokens[curTok];
	}
	private Token token;

	public CompilationUnitTree parse(Collection<Token> tokens, String filename) {
		CompilationUnitTree unit = new CompilationUnitTree(filename);
		this.tokens = tokens.toArray(new Token[0]);
		token = nextToken();
		if ("package".equals(token.text)) {
			token = nextToken();
			unit.packageName = token.text;
			token = nextToken();
			while (token.text.equals(".")) {
				token = nextToken();
				if (token.type != TokenType.IDENTIFIER) throw new UnexpectedTokenException(token,"identifier expected");
				unit.packageName += "." + token.text;
			}
			if (token.type != TokenType.ENDLINE) throw new UnexpectedTokenException(token,"';' expected");
		}
		token = nextToken();
		while (parseUnitMember(unit)) token = nextToken();
		return unit;
	}
	private boolean parseUnitMember(CompilationUnitTree unit) {
		List<String> modifiers = readModifiers(unit);
		if (token.type == TokenType.EOF) return false;
		if (token.type == TokenType.IDENTIFIER || isKeywordType(token.text)) {
			TypeTree type = parseType(unit);
			if (type == null) {
				throw new UnexpectedTokenException(token,"type expected");
			}
			if (seeNextToken().type == TokenType.OPENROUND) {
				//function
				unit.addFunction(
						parseFunctionImplementation(
								unit, parseFunctionDeclaration(unit,type,modifiers)
						)
				);
				return true;
			}
			//field
			VariableDeclarationStatementTree field = parseVariableDeclaration(unit,null,type,Modifiers.parseModifiers(modifiers,FunctionImplTree.availableVariableMod));
			unit.addField(field);
			return true;
		}
		if (token.type == TokenType.KEYWORD) {
			switch (token.text) {
				case "typename": {
					return parseTypename(unit);
				}
				case "struct": {
					return parseStructDeclaration(unit);
				}
			}
		}
		throw new UnexpectedTokenException(token);
	}

	private TypeTree parseType(CompilationUnitTree unit) {
		if (token.type != TokenType.IDENTIFIER && !isKeywordType(token.text)) {
			throw new UnexpectedTokenException(token,"type expected");
		}
		TypeTree type = unit.getType(token.text);
		token = nextToken();
		while (token.type == TokenType.OPENSQUARE) {
			type = TypeDynamicArrayTree.get(type);
			token = nextToken();
			if (token.type != TokenType.CLOSESQUARE)
				throw new UnexpectedTokenException(token,']');
			token = nextToken();
		}
		return type;
	}


	private boolean parseStructDeclaration(CompilationUnitTree unit /* token = keyword("struct") */) {
		token = nextToken();
		if (token.type != TokenType.IDENTIFIER) {
			throw new UnexpectedTokenException(token,"struct name expected");
		}
		String structName = token.text;
		token = nextToken();
		if (token.type != TokenType.OPEN) {
			throw new UnexpectedTokenException(token,'{');
		}
		token = nextToken();
		List<VariableDeclarationStatementTree> fields = new ArrayList<>();
		while (token.type != TokenType.CLOSE) {
			List<String> modifiers = readModifiers(unit);
			if (token.type != TokenType.IDENTIFIER && !isKeywordType(token.text)) {
				throw new UnexpectedTokenException(token,"field type expected");
			}
			TypeTree type = parseType(unit);
			fields.add(parseVariableDeclaration(unit,null,type,Modifiers.parseModifiers(modifiers, StructTypeTree.availableVariableMod)));
			if (token.type != TokenType.ENDLINE) {
				throw new UnexpectedTokenException(token,';');
			}
			token = nextToken();
		}
		token = nextToken();
		if (token.type != TokenType.ENDLINE) {
			throw new UnexpectedTokenException(token,';');
		}
		unit.addStruct(new StructTypeTree(structName,fields));
		return true;
	}


	private boolean parseTypename(CompilationUnitTree unit /* token = keyword("typename") */) {
		token = nextToken();
		if (token.type != TokenType.IDENTIFIER) {
			throw new UnexpectedTokenException(token,"typename expected");
		}
		String typename = token.text;
		token = nextToken();
		if (token.type != TokenType.OPERATOR || !token.text.equals("=")) {
			throw new UnexpectedTokenException(token,'=');
		}
		token = nextToken();
		if (token.type != TokenType.IDENTIFIER && !isKeywordType(token.text)) {
			throw new UnexpectedTokenException(token,"type expected");
		}
		TypeTree type = parseType(unit);
		if (token.type != TokenType.ENDLINE)
			throw new UnexpectedTokenException(token,';');
		unit.addTypename(typename,type);
		return true;
	}

	private FunctionDeclarationTree parseFunctionDeclaration(CompilationUnitTree unit, TypeTree retType, List<String> modifiers) {
		if (token.type != TokenType.IDENTIFIER) {
			throw new UnexpectedTokenException(token,"function name expected");
		}
		String fname = token.text;
		token = nextToken();
		if (token.type != TokenType.OPENROUND) {
			throw new UnexpectedTokenException(token,'(');
		}
		token = nextToken();
		ArrayList<VariableDeclarationStatementTree> args = new ArrayList<>(8);
		while (token.type != TokenType.CLOSEROUND) {
			List<String> argMod = readModifiers(unit);
			args.add(parseArgument(unit,argMod));
			token = nextToken();
			if (token.type == TokenType.OPERATOR && token.text.equals(",")) {
				token = nextToken();
				continue;
			}
		}
		return new FunctionDeclarationTree(fname,args.toArray(new VariableDeclarationStatementTree[0]),retType,Modifiers.parseModifiers(modifiers,unit.availableFunctionModifiers()));
	}

	private FunctionDeclarationTree parseFunctionImplementation(CompilationUnitTree unit, FunctionDeclarationTree declaration) {
		token = nextToken();
		if (token.type == TokenType.ENDLINE) {
			return declaration;
		}
		if (token.type != TokenType.OPEN) throw new UnexpectedTokenException(token,"'{' expected");

		FunctionImplTree functionImplTree = new FunctionImplTree();
		declaration.setImpl(functionImplTree);
		token = nextToken();
		while (token.type != TokenType.CLOSE) {
			functionImplTree.statements.add(parseStatement(unit, declaration));
			token = nextToken();
		}
		return declaration;
	}

	private VariableDeclarationStatementTree parseArgument(CompilationUnitTree unit, List<String> modifiers) {
		TypeTree type = parseType(unit);
		if (token.type != TokenType.IDENTIFIER) {
			throw new UnexpectedTokenException(token,"identifier expected");
		}
		String name = token.text;


		return new VariableDeclarationStatementTree(type,name, Modifiers.parseModifiers(modifiers,FunctionDeclarationTree.availableVariableMod));
	}

	private VariableDeclarationStatementTree parseVariableDeclaration(CompilationUnitTree unit, FunctionDeclarationTree context, TypeTree type, Modifiers modifiers) {
		String varname = token.text;
		token = nextToken();
		ExprTree init = null;
		if (token.type == TokenType.OPERATOR && token.text.equals("=")) {
			token = nextToken();
			init = parseExpr(unit,context,type);
		}
		if (token.type != TokenType.ENDLINE)
			throw new UnexpectedTokenException(token, ';');

		return new VariableDeclarationStatementTree(type, varname, init, modifiers);
	}


	private List<String> readModifiers(CompilationUnitTree unit) {
		List<String> modifiers = new ArrayList<>();
		while (isModifier(token)) {
			if (modifiers.contains(token.text))
				throw new UnexpectedTokenException(token, "duplicated modifier '" + token.text + "'");
			modifiers.add(token.text);
			token = nextToken();
		}
		return modifiers;
	}

	private StatementTree parseStatement(CompilationUnitTree unit, FunctionDeclarationTree context) {
		List<String> modifiers = readModifiers(unit);
		if (token.type == TokenType.IDENTIFIER) {
			TypeTree type = context.getType(unit,token.text);
			if (type == null) {
				ExprTree expr = parseExpr(unit,context,null);
				/*if (expr instanceof VariableExprTree) {
					if (token.type == TokenType.OPERATOR && token.text.equals("=")) {
						token = nextToken();
						ExprTree asign = parseExpr(unit,context,expr.getRetType());
						if (token.type != TokenType.ENDLINE) {
							throw new UnexpectedTokenException(token,';');
						}
						return new VariableAssignmentStatementTree((VariableExprTree) expr,asign);
					}
				}*/
				if (token.type != TokenType.ENDLINE) {
					throw new UnexpectedTokenException(token,';');
				}
				return new ExprStatementTree(expr);
			} else {
				token = nextToken();
				return context.getImpl().declareVariable(parseVariableDeclaration(unit,context,type,Modifiers.parseModifiers(modifiers,FunctionImplTree.availableVariableMod)));
			}
		} else if (token.type == TokenType.KEYWORD) {
			if (isKeywordType(token.text)) {
				TypeTree type = parseType(unit);
				return context.getImpl().declareVariable(parseVariableDeclaration(unit,context,type,Modifiers.parseModifiers(modifiers,FunctionImplTree.availableVariableMod)));
			}
			switch (token.text) {
				case "return": {
					Token keyword = token;
					if (context.getReturnType() == TypeTree.VOID) {
						token = nextToken();
						if (token.type != TokenType.ENDLINE)
							throw new UnexpectedTokenException(token,"';' expected");
						return new ReturnStatementTree(keyword,null,token);
					}
					token = nextToken();
					ExprTree ret = parseExpr(unit,context,context.getReturnType());
					if (token.type != TokenType.ENDLINE)
						throw new UnexpectedTokenException(token,"';' expected");
					return new ReturnStatementTree(keyword,ret, token);
				}
				case "if": {
					Token ifToken = token;
					token = nextToken();
					if (token.type != TokenType.OPENROUND) {
						throw new UnexpectedTokenException(token,"'(' expected");
					}
					Token openr = token;
					ExprTree assert_ = parseExpr(unit,context,TypeTree.BOOL);
					Token closer = token;
					StatementTree statementTree = parseStatement(unit,context);
					return new IfStatement(ifToken,assert_,statementTree);
				}
				default:
					throw new UnexpectedTokenException(token, "illegal keyword");
			}
		}
		throw new UnexpectedTokenException(token, "statement expected");
	}


	private ExprTree parseExpr(final CompilationUnitTree unit, FunctionDeclarationTree context, final TypeTree retType) {
		ExprTree lhs = parsePrimary(unit,context,retType);
		token = nextToken();
		return parseBinOpRhs(unit,context,retType,0,lhs);
	}

	private ExprTree parsePrimary(final CompilationUnitTree unit, FunctionDeclarationTree context, final TypeTree retType) {
		switch (token.type) {
			case NUMBER:
				return parseNumberExpr(retType);
			case CHARS:
				return parseCharExpr(retType);
			case STRING:
				return parseStringExpr(retType);
			case IDENTIFIER:
				return parseIdentifierExpr(unit,context,retType);
			case KEYWORD:
				if (token.text.equals("true")) {
					return new BoolLiterallExprTree(true,token);
				} else if (token.text.equals("false")) {
					return new BoolLiterallExprTree(false,token);
				}
			case OPENROUND:
				return parseParentExpr(unit,context,retType);
			case OPEN:
				return parseInitializer(unit,context,retType);
		}
		throw new UnexpectedTokenException(token,"primary expression expected");
	}

	private ExprTree parseStringExpr(TypeTree retType) {
		if (retType == null)
			retType = TypeDynamicArrayTree.get(TypeTree.CHAR);
		if (token.type != TokenType.STRING) {
			throw new UnexpectedTokenException(token,"String literal expected");
		}
		ExprTree str = new StringLiteralExprTree(token,retType);
		return str;
	}

	private ExprTree parseCharExpr(TypeTree retType) {
		String text = token.text;
		return new CharExprTree(retType,text);
	}

	private ExprTree parseInitializer(CompilationUnitTree unit, FunctionDeclarationTree context, TypeTree retType) {
		if (retType instanceof StructTypeTree || retType == null) {
			List<ExprTree> inits = new ArrayList<>();
			int i = 0;
			while (token.type != TokenType.CLOSE) {
				token = nextToken();
				inits.add(parseExpr(unit,context,retType == null ? null : ((StructTypeTree) retType).getFields().get(i).getType()));
				++i;
			}
			return new InitializerListExprTree(retType,inits);
		} else
			throw new UnexpectedTokenException(token,"incomparable types: " + retType + " and struct{...}");
	}

	private ExprTree parseBinOpRhs(final CompilationUnitTree unit, FunctionDeclarationTree context, final TypeTree retType, int priority, ExprTree lhs) {
		while(true) {
			/*if (token.type != TokenType.OPERATOR) {
				throw new UnexpectedTokenException(token,"operator expected");
			}*/
			Token operator = token;
			int tokenPriority = BinaryOperator.getPriority(operator.text);
			if (tokenPriority < priority) {
				return lhs;
			}
			token = nextToken();

			ExprTree rhs = parsePrimary(unit,context,retType);
			token = nextToken();
			/*if (token.type != TokenType.OPERATOR) {
				throw new UnexpectedTokenException(token,"operator expected");
			}*/
			int nextPriority = BinaryOperator.getPriority(token.text);
			if (tokenPriority < nextPriority) {
				rhs = parseBinOpRhs(unit,context,retType,tokenPriority+1,rhs);
			}

			lhs = new BinaryExprTree(lhs,rhs, BinaryOperator.getOperator(operator.text),operator);
			continue;
		}
	}
	private ExprTree parseParentExpr(CompilationUnitTree unit, FunctionDeclarationTree context, TypeTree retType) {
		if (token.type != TokenType.OPENROUND)
			throw new UnexpectedTokenException(token,'(');
		token = nextToken();
		ExprTree expr = parseExpr(unit,context,retType);
		if (token.type != TokenType.CLOSEROUND)
			throw new UnexpectedTokenException(token,')');
		return expr;
	}

	private ExprTree parseIdentifierExpr(CompilationUnitTree unit, FunctionDeclarationTree context, TypeTree retType) {
		if (token.type != TokenType.IDENTIFIER)
			throw new UnexpectedTokenException(token,"identifier expected");
		String name = token.text;
		if (seeNextToken().type != TokenType.OPENROUND) {
			VariableDeclarationStatementTree variable = context.getVariable(name);
			if (variable.getType() instanceof StructTypeTree && seeNextToken().type == TokenType.OPERATOR && seeNextToken().text.equals(".")) {
				Token operator = nextToken();//eat '.'
				StructTypeTree struct = (StructTypeTree) variable.getType();
				token = nextToken();
				if (token.type != TokenType.IDENTIFIER)
					throw new UnexpectedTokenException(token,"field name expected");
				VariableDeclarationStatementTree field = struct.getVariable(token.text);
				return new BinaryExprTree(new VariableExprTree(variable),new VariableExprTree(field),BinaryOperator.SELECT,retType,operator);
			} else if (retType != null && !variable.getType().canBeImplicitCastTo(retType)) {
				throw new UnexpectedTokenException(token,"cannot implicit cast " + variable.getType() + " to " + retType);
			}
			return new VariableExprTree(variable,retType);
		}
		token = nextToken(); //OPENROUND
		if (seeNextToken().type == TokenType.CLOSEROUND) {
			token = nextToken();
		}
		ArrayList<ExprTree> args = new ArrayList<>(8);
		while (token.type != TokenType.CLOSEROUND) {
			token = nextToken();
			args.add(parseExpr(unit,context,null));
		}
		return new InvokeExprTree(context.getFunction(unit,name,args), args.toArray(new ExprTree[0]));
	}

	private NumberExprTree parseNumberExpr(TypeTree retType) {
		if (token.type != TokenType.NUMBER)
			throw new UnexpectedTokenException(token,"number expected");
		String text = token.text;
		boolean unsigned = false;
		if (text.endsWith("u")) {
			unsigned = true;
			text = text.substring(0,text.length() - 1);
		}
		if (retType == null) {
				 if(text.endsWith("l")) retType = TypeTree.overriddenTypes.get("long");
			else if(text.endsWith("f")) retType = TypeTree.overriddenTypes.get("float");
			else if(text.endsWith("d") || text.contains(".")) retType = TypeTree.overriddenTypes.get("double");
			else if(text.endsWith("s")) {
					 retType = TypeTree.overriddenTypes.get("short");
					 text = text.substring(0,text.length() - 1);
			} else if(text.endsWith("b")) {
					 retType = TypeTree.overriddenTypes.get("byte");
					 text = text.substring(0,text.length() - 1);
			}
			else retType = TypeTree.INT;
		}
		if (unsigned) {
			retType = retType.getUnsignedVersion();
		}
		return new NumberExprTree(retType,text);
	}

	private static boolean isKeywordType(String key) {
		return TypeTree.overriddenTypes.containsKey(key);
	}

	private boolean isModifier(Token token) {
		return token.type == TokenType.KEYWORD && Compiler.modifiers.contains(token.text);
	}
}
