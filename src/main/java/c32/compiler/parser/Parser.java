package c32.compiler.parser;

import c32.compiler.Compiler;
import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.lexer.tokenizer.TokenType;
import c32.compiler.lexer.tokenizer.UnexpectedTokenException;
import c32.compiler.parser.ast.*;
import c32.compiler.parser.ast.declaration.*;
import c32.compiler.parser.ast.declarator.*;
import c32.compiler.parser.ast.expr.*;
import c32.compiler.parser.ast.statement.*;
import c32.compiler.parser.ast.type.*;

import java.util.*;

/*
Для каждого метода parseX:
При входе в метод, token является первым токеном читаемой конструкции
После завершения token равен следующему токену после читаемой конструкции
 */
public class Parser {
	private Token[] tokens;
	int curTok = 0;
	Token nextToken() {
		try {
			return tokens[curTok++];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new CompilerException(null,"Где код????????");
		}
	}
	private Token currentToken() {
		return tokens[curTok-1];
	}

	Token seeNextToken() {
		return tokens[curTok];
	}
	Token token;

	public CompilationUnitTree parse(Collection<Token> tokens, String filename) {
		this.tokens = tokens.toArray(new Token[0]);
		token = nextToken();
		List<DeclarationTree<?>> declarations = new ArrayList<>();
		while (token.type != TokenType.EOF) {
			declarations.add(parseDeclaration());
		}
		return new CompilationUnitTree(filename,declarations);
	}
	private DeclarationTree<?> parseDeclaration() {
		Location startLocation = token.location;
		List<Token> modifiers = readModifiers();
		switch (token.text) {
			case "struct":
				return parseStructDeclaration(modifiers,startLocation);
			case "typename":
				return parseTypenameDeclaration(modifiers,startLocation);
		}
		RuntimeTypeElementTree type = parseTypeElement();
		List<DeclaratorTree> declarators = new ArrayList<>();
		while (token.type != TokenType.ENDLINE) {
			declarators.add(parseDeclarator());
			if (token.text.equals(","))
				token = nextToken();
			else
				break;
		}
		Token endLine = null;
		if (declarators.isEmpty() || declarators.get(declarators.size()-1).isLineDeclarator()) {
			if (token.type != TokenType.ENDLINE) {
				throw new UnexpectedTokenException(token,';');
			}
			endLine = token;
			token = nextToken();
		}
		return new ValuedDeclarationTree(modifiers,type,declarators,endLine,
				Location.between(startLocation,endLine == null ? declarators.get(declarators.size() - 1).getLocation() : endLine.location));
	}

	private TypenameDeclarationTree parseTypenameDeclaration(List<Token> modifiers, Location startLocation) {
		if (!token.text.equals("typename")) {
			throw new UnexpectedTokenException(token,"'typename' expected");
		}
		Token keyword = token;
		List<TypenameDeclaratorTree> declarators = new ArrayList<>();
		do {
			token = nextToken();
			declarators.add(parseTypenameDeclarator());
		} while (token.text.equals(","));
		if (token.type != TokenType.ENDLINE) {
			throw new UnexpectedTokenException(token,';');
		}
		Token endLine = token;
		token = nextToken();
		return new TypenameDeclarationTree(modifiers,keyword,declarators,endLine,Location.between(startLocation,endLine.location));
	}

	private TypenameDeclaratorTree parseTypenameDeclarator() {
		if (token.type != TokenType.IDENTIFIER) {
			throw new UnexpectedTokenException(token,"name of declaring type expected");
		}
		Token name = token;
		token = nextToken();
		if (!token.text.equals("=")) {
			throw new UnexpectedTokenException(token,'=');
		}
		Token assign = token;
		token = nextToken();
		TypeElementTree type = parseTypeElement();
		return new TypenameDeclaratorTree(name,assign,type,Location.between(name.location,type.getLocation()));
	}

	private DeclarationTree<?> parseStructDeclaration(List<Token> modifiers, Location startLocation) {
		if (!token.text.equals("struct")) {
			throw new UnexpectedTokenException(token,"'struct' expected");
		}
		Token keyword = token;
		List<StructDeclaratorTree> declarators = new ArrayList<>();
		token = nextToken();
		do {
			declarators.add(parseStruct());
		} while (token.text.equals(","));
		return new StructDeclarationTree(modifiers,declarators,Location.between(startLocation,token.location),keyword);
	}

	private StructDeclaratorTree parseStruct() {
		Token name = null;
		if (token.type == TokenType.IDENTIFIER) {
			name = token;
			token = nextToken();
		}
		Token open = assertToken(TokenType.OPEN);
		token = nextToken();
		List<DeclarationTree<?>> declarations = new ArrayList<>();
		while (token.type != TokenType.CLOSE) {
			declarations.add(parseDeclaration());
		}
		Token close = token;
		token = nextToken();
		return new StructDeclaratorTree(name,open,declarations,close);
	}

	private DeclaratorTree parseDeclarator() {
		if (seeNextToken().type == TokenType.OPENROUND) {
			return parseFunctionDeclarator();
		}
		return parseVariableDeclarator();
	}

	private DeclaratorTree parseFunctionDeclarator() {
		Token name = assertToken(TokenType.IDENTIFIER,"function name");
		token = nextToken();
		ParameterListTree parameterList = parseParameterList();

		FunctionDeclaratorTree declarator = new FunctionDeclaratorTree(name, parameterList);
		if (token.type == TokenType.OPEN) {
			BlockStatementTree block = parseBlockStatement();
			return new FunctionDefinitionTree(declarator,block,Location.between(name.location,block.getLocation()));
		}
		return declarator;
	}

	private StatementTree parseStatement() {
		switch (token.type) {
			case OPEN:
				return parseBlockStatement();
			case KEYWORD:
				switch (token.text) {
					case "return":
						return parseReturnStatement();
					case "if":
						return parseIfStatement();
					case "while":
						return parseWhileStatement();
					case "do":
						return parseDoWhileStatement();
					case "for":
						return parseForStatement();
					/*case "goto":
						return parseGotoStatement();
					case "break":
						return parseBreakStatement();
					case "continue":
						return parseContinueStatement();
					case "switch":
						return parseSwitchStatement();
					case "try":
						return parseTryCatchFinallyStatement();
					case "throw":
						return parseThrowStatement();
					case "assert":
						return parseAssertStatement();*/
				}
				break;
			case ENDLINE:
				return parseNopStatement();
		}
		int oldTok = curTok;
		try {
			return parseDeclarationStatement();
			/*ExprTree expr = parseExpr();

			if (
					expr instanceof BinaryExprTree && (
							((BinaryExprTree) expr).getOperator().text.equals("*") ||
									((BinaryExprTree) expr).getOperator().text.equals("&") &&
											!(
													((BinaryExprTree) expr).getOperator().text.equals("=")
															&& ((BinaryExprTree) expr).getLhs() instanceof BinaryExprTree
															&& (
															((BinaryExprTree) ((BinaryExprTree) expr).getLhs()).getOperator().text.equals("*")
																	|| ((BinaryExprTree) ((BinaryExprTree) expr).getLhs()).getOperator().text.equals("&")
													)
											))) {
				if (token.type != TokenType.ENDLINE) {
					throw new UnexpectedTokenException(token,';');
				}
				token = nextToken();
				return new ExpressionStatementTree(expr);
			}*/
		} catch (UnexpectedTokenException ignored) {
			//not an expression
		}
		curTok = oldTok;
		token = currentToken();

		return parseExpressionStatement();
	}

	private StatementTree parseNopStatement() {
		Token endLine = assertToken(TokenType.ENDLINE);
		token = nextToken();
		return new NopStatementTree(endLine);
	}

	private StatementTree parseForStatement() {
		Token keyword = assertToken("for");
		token = nextToken();

		Token openRound = assertToken(TokenType.OPENROUND);
		token = nextToken();

		StatementTree declarationStatement = parseStatement();
		StatementTree conditionStatement = parseStatement();
		ExprTree action = null;
		if (token.type != TokenType.CLOSEROUND)
			action = parseExpr();

		Token closeRound = assertToken(TokenType.CLOSEROUND);
		token = nextToken();

		StatementTree statement = parseStatement();
		return new ForStatementTree(keyword,openRound,declarationStatement,conditionStatement,action,closeRound,statement);
	}

	private ExpressionStatementTree parseExpressionStatement() {
		ExprTree expr = parseExpr();
		Token endLine = assertToken(TokenType.ENDLINE);
		token = nextToken();
		return new ExpressionStatementTree(expr,endLine);
	}

	private DeclarationStatementTree parseDeclarationStatement() {
		DeclarationTree<?> decl = parseDeclaration();
		return new DeclarationStatementTree(decl);
	}

	private DoWhileStatement parseDoWhileStatement() {
		Token doKeyword = assertToken("do");
		token = nextToken();

		StatementTree statement = parseStatement();

		Token whileKeyword = assertToken("while");
		token = nextToken();

		Token openRound = assertToken(TokenType.OPENROUND);
		token = nextToken();

		ExprTree condition = parseExpr();

		Token closeRound = assertToken(TokenType.CLOSEROUND);
		token = nextToken();

		Token endLine = assertToken(TokenType.ENDLINE);
		token = nextToken();
		return new DoWhileStatement(doKeyword,statement,whileKeyword,openRound,condition,closeRound,endLine);
	}

	private WhileStatementTree parseWhileStatement() {
		Token keyword = assertToken("while");
		token = nextToken();
		Token openRound = assertToken(TokenType.OPENROUND);
		token = nextToken();
		ExprTree condition = parseExpr();
		Token closeRound = assertToken(TokenType.CLOSEROUND);
		token = nextToken();
		StatementTree statement = parseStatement();
		return new WhileStatementTree(keyword,openRound,condition,closeRound,statement);
	}

	private IfStatementTree parseIfStatement() {
		Token keyword = assertToken("if");
		token = nextToken();

		Token open = assertToken(TokenType.OPENROUND);
		token = nextToken();

		ExprTree condition = parseExpr();


		Token close = assertToken(TokenType.CLOSEROUND);
		token = nextToken();

		StatementTree statement = parseStatement();
		Token elseKeyword = null;
		StatementTree elseStatement = null;
		if (token.text.equals("else")) {
			elseKeyword = token;
			token = nextToken();
			elseStatement = parseStatement();
		}
		return new IfStatementTree(keyword,open,condition,close,statement,elseKeyword,elseStatement);
	}

	//region assert

	private Token assertToken(TokenType type, String expected) {
		return assertToken(token,type,expected);
	}
	private static Token assertToken(Token token, TokenType type, String expected) {
		if (token.type != type) {
			throw new UnexpectedTokenException(token, expected + " expected");
		}
		return token;
	}
	private Token assertToken(TokenType type) {
		return assertToken(token,type);
	}
	private static Token assertToken(Token token, TokenType type) {
		return assertToken(token,type,expectedCheckName(type));
	}

	private static String expectedCheckName(TokenType type) {
		switch (type) {
			case IDENTIFIER: return "identifier";
			case EOF: return "EOF";
			case OPEN: return "'{'";
			case CHARS: return "char literal";
			case CLOSE: return "'}'";
			case NUMBER: return "number literal";
			case STRING: return "string literal";
			case ERROR: return "ERROR";
			case COMMENT: return "COMMENT";
			case ENDLINE: return "';'";
			case KEYWORD: return "keyword";
			case OPERATOR: return "operator";
			case DIRECTIVE: return "DIRECTIVE";
			case OPENROUND: return "'('";
			case CLOSEROUND: return "')'";
			case OPENSQUARE: return "'['";
			case CLOSESQUARE: return "']'";
		}
		throw new AssertionError("Unknown TokenType");
	}

	private Token assertToken(String expected) {
		return assertToken(token,expected);
	}
	private static Token assertToken(Token token, String expected) {
		if (!token.text.equals(expected)) {
			throw new UnexpectedTokenException(token,"'" + expected + "' expected");
		}
		return token;
	}

	//endregion

	private ReturnStatementTree parseReturnStatement() {
		if (!token.text.equals("return")) {
			throw new UnexpectedTokenException(token,"'return' expected");
		}
		Token keyword = token;
		token = nextToken();
		if (token.type == TokenType.ENDLINE) {
			Token endLine = token;
			token = nextToken();
			return new ReturnStatementTree(keyword,null,endLine);
		}
		ExprTree expr = parseExpr();
		if (token.type != TokenType.ENDLINE) {
			throw new UnexpectedTokenException(token,';');
		}
		Token endLine = token;
		token = nextToken();
		return new ReturnStatementTree(keyword,expr,endLine);
	}


	private BlockStatementTree parseBlockStatement() {
		if (token.type != TokenType.OPEN)
			throw new UnexpectedTokenException(token,'{');
		Token open = token;
		token = nextToken();
		List<StatementTree> statements = new ArrayList<>();
		while(token.type != TokenType.CLOSE) {
			statements.add(parseStatement());
		}
		Token close = token;
		token = nextToken();
		return new BlockStatementTree(open,statements,close);
	}


	private ParameterListTree parseParameterList() {
		if (token.type != TokenType.OPENROUND) {
			throw new UnexpectedTokenException(token,'(');
		}
		Token open = token;
		token = nextToken();
		List<ParameterDeclaration> parameterList = new ArrayList<>();
		while (token.type != TokenType.CLOSEROUND) {
			RuntimeTypeElementTree type = parseTypeElement();
			DeclaratorTree declarator = parseDeclarator();
			parameterList.add(new ParameterDeclaration(type,declarator));
			if (token.text.equals(",")) {
				token = nextToken();
				continue;
			} else break;
		}
		if (token.type != TokenType.CLOSEROUND) {
			throw new UnexpectedTokenException(token,')');
		}
		Token close = token;
		token = nextToken();
		return new ParameterListTree(open,parameterList,close);
	}

	private VariableDeclaratorTree parseVariableDeclarator() {
		if (token.type != TokenType.IDENTIFIER) {
			throw new UnexpectedTokenException(token,"variable name expected");
		}
		Token name = token;
		token = nextToken();
		if (token.type == TokenType.OPERATOR && token.text.equals("=")) {
			Token assignOperator = token;
			token = nextToken();
			ExprTree init = parseExpr();
			return new VariableDeclaratorTree(name,assignOperator,init,Location.between(name.location, init.getLocation()));
		} else {
			return new VariableDeclaratorTree(name,null,null,name.location);
		}
	}


	private RuntimeTypeElementTree parseTypeElement() {
		Token _const = null;
		Token _restrict = null;
		while(true) {
			switch (token.text) {
				case "const":
					if (_const != null) throw new UnexpectedTokenException(token,"duplicated 'const' modifier");
					_const = token;
					token = nextToken();
					continue;
				case "restrict":
					if (_restrict != null) throw new UnexpectedTokenException(token,"duplicated 'restrict' modifier");
					_restrict = token;
					token = nextToken();
					continue;
			}
			break;
		}
		RuntimeTypeElementTree type;
		if (token.type == TokenType.OPENROUND) {
			token = nextToken();
			type = parseTypeElement();
			if (_const != null) type.set_const(_const);
			if (_restrict != null) type.set_restrict(_restrict);
			assertToken(TokenType.CLOSEROUND);
			token = nextToken();
		} else if (isKeywordType(token)) {
			type = new TypeKeywordElementTree(_const, _restrict, token);
			token = nextToken();
		} else if (token.type == TokenType.IDENTIFIER) {
			type = new TypeReferenceElementTree(_const, _restrict, new ReferenceExprTree(token));
			token = nextToken();
		} /*else if (token.text.equals("struct")) {
			type = new TypeStructElementTree(_const, parseStruct());
		} */else {
			throw new UnexpectedTokenException(token,"type expected");
		}
		while (token.type == TokenType.OPENSQUARE || token.text.equals("*") || token.text.equals("&")) {
			Token spec = token;
			if (token.type == TokenType.OPENSQUARE) {
				token = nextToken();
				if (token.type != TokenType.CLOSESQUARE) {
					throw new UnexpectedTokenException(token,']');
				}
				Token closeSquare = token;
				type = new DynamicArrayTypeElementTree(null,null,type,spec,closeSquare);

				token = nextToken();
			} else if(token.text.equals("*")) {
				token = nextToken();
				type = new PointerTypeElementTree(null,null,type,spec);
			} else {
				token = nextToken();
				type = new ReferenceTypeElementTree(null,null,type,spec);
			}
		}
		return type;
	}

	
	
	
	//region #parseExpr



	public ExprTree parseExpr() {

		ExprTree lhs = parsePrimary();


		token = nextToken();


		if (token.type == TokenType.OPENSQUARE) {
			Token open = token;
			token = nextToken();
			ExprTree index = parseExpr();
			if (token.type != TokenType.CLOSESQUARE) {
				throw new UnexpectedTokenException(token,']');
			}
			Token close = token;
			token = nextToken();
			return new IndexExpr(lhs,open,index,close);
		}

		return parseBinOpRhs(0, lhs);
	}

	private ExprTree parseBinOpRhs(int priority, ExprTree lhs) {
		while(true) {
			Token operator = token;
			int tokenPriority = Compiler.getBinaryOperatorPriority(operator.text);
			if (tokenPriority <= priority) {//чтобы x.y.z читалось как (x.y).z, а не x.(y.z)
				return lhs;
			}
			token = nextToken();

			ExprTree rhs = parsePrimary();
			token = nextToken();

			int nextPriority = Compiler.getBinaryOperatorPriority(token.text);
			if (tokenPriority < nextPriority) {
				rhs = parseBinOpRhs(tokenPriority+1,rhs);
			}

			lhs = new BinaryExprTree(lhs,operator,rhs);
			continue;
		}
	}


	private ExprTree parsePrimary() {
		Token prefixOperator = null;
		int prefixPriority = 0;
		if (token.type == TokenType.OPERATOR) {
			prefixOperator = token;
			prefixPriority = Compiler.getPrefixOperatorPriority(prefixOperator.text);
			token = nextToken();
		}

		ExprTree expr = parsePrimary0();

		if (seeNextToken().type == TokenType.OPERATOR) {
			if (Compiler.getPostfixOperatorPriority(seeNextToken().text) > prefixPriority) {
				token = nextToken();
				expr = new UnaryPostfixExprTree(expr,token);
			}
		}

		if (prefixOperator != null) {
			expr = new UnaryPrefixExprTree(prefixOperator,expr);
		}

		return expr;
	}

	private ExprTree parsePrimary0() {
		switch (token.type) {
			case NUMBER:
				return new LiteralExprTree(token, LiteralType.INTEGER_LITERAL);
			case CHARS:
				return new LiteralExprTree(token, LiteralType.CHAR_LITERAL);
			case STRING:
				return new LiteralExprTree(token, LiteralType.STRING_LITERAL);
			case OPENROUND:
				return parseParentExpr();
			case OPEN:
				return parseInitializerList();
			case IDENTIFIER:
				return parseIdentifierExpr();
			case KEYWORD: {
				switch (token.text) {
					case "false":
					case "true":
						return new LiteralExprTree(token, LiteralType.BOOLEAN_LITERAL);
				}
			}
		}
		throw new UnexpectedTokenException(token,"primary expression expected");
	}

	private ExprTree parseIdentifierExpr() {
		ReferenceExprTree ref = new ReferenceExprTree(token);
		if (seeNextToken().type == TokenType.OPENROUND) {
			token = nextToken();
			ArgumentListTree args = parseArgumentList();
			return new CallExprTree(ref,args);
		}
		return ref;
	}

	private ArgumentListTree parseArgumentList() {
		Token openRound = assertToken(TokenType.OPENROUND);
		token = nextToken();
		List<ExprTree> args = new ArrayList<>();
		while (token.type != TokenType.CLOSEROUND) {
			args.add(parseExpr());
			if (token.type == TokenType.CLOSEROUND) break;
			if (!token.text.equals(",")) {
				throw new UnexpectedTokenException(token,',');
			}
			token = nextToken();
		}
		Token closeRound = token;
		return new ArgumentListTree(openRound,args,closeRound);
	}

	private ExprTree parseInitializerList() {
		if (token.type != TokenType.OPEN) {
			throw new UnexpectedTokenException(token,'{');
		}
		Token open = token;
		token = nextToken();
		List<ExprTree> initializers = new ArrayList<>();
		while (token.type != TokenType.CLOSE) {
			initializers.add(parseExpr());
			if (token.text.equals(",")) {
				token = nextToken();
				continue;
			}
			if (token.type == TokenType.CLOSE) break;
			throw new UnexpectedTokenException(token,"',' or '}' expected");
		}
		Token close = token;
		return new InitializerListExprTree(open,initializers,close);
	}

	private ExprTree parseParentExpr() {
		if (token.type != TokenType.OPENROUND)
			throw new UnexpectedTokenException(token,'(');
		token = nextToken();
		ExprTree expr = parseExpr();
		if (token.type != TokenType.CLOSEROUND)
			throw new UnexpectedTokenException(token,')');
		return expr;
	}
	//endregion



	private List<Token> readModifiers() {
		List<Token> modifiers = new ArrayList<>();
		while (isModifier(token)) {
			if (modifiers.contains(token))
				throw new UnexpectedTokenException(token, "duplicated modifier '" + token.text + "'");
			modifiers.add(token);
			token = nextToken();
		}
		return modifiers;
	}

	private boolean isModifier(Token token) {
		return token.type == TokenType.KEYWORD && Compiler.modifiers.contains(token.text);
	}



	private static boolean isKeywordType(Token token) {
		if (token.type != TokenType.KEYWORD) return false;
		return keywordsTypes.contains(token.text);
	}

	private static final Set<String> keywordsTypes = new HashSet<>();
	static {
		keywordsTypes.add("void");

		keywordsTypes.add("bool");

		keywordsTypes.add("byte");
		keywordsTypes.add("ubyte");
		keywordsTypes.add("short");
		keywordsTypes.add("ushort");
		keywordsTypes.add("int");
		keywordsTypes.add("uint");
		keywordsTypes.add("long");
		keywordsTypes.add("ulong");

		keywordsTypes.add("half");
		keywordsTypes.add("float");
		keywordsTypes.add("double");

		keywordsTypes.add("char");
		keywordsTypes.add("char8");
		keywordsTypes.add("char32");
	}
}
