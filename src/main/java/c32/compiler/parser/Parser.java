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
import org.jetbrains.annotations.Nullable;

import java.util.*;

/*
Для каждого метода parseX:
При входе в метод, token является первым токеном читаемой конструкции
После завершения token равен следующему токену после читаемой конструкции

Для parse_X:
Всё то же самое, только две искорки снисходительности (после завершения token равен последнему токену читаемой конструкции)
 */
public class Parser {
	private Stack<Token> tokens;
	int curTok = 0;
	Token nextToken() {
		try {
			return tokens.get(curTok++);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new CompilerException(null,"Где код????????");
		}
	}
	private Token currentToken() {
		return tokens.get(curTok-1);
	}

	Token seeNextToken() {
		return tokens.get(curTok);
	}
	Token token;


	private PackageTree packageTree = null;
	private List<DeclarationTree<?>> declarations;
	public CompilationUnitTree parse(Stack<Token> tokens, String filename) {
		this.tokens = tokens;
		token = nextToken();
		this.declarations = new ArrayList<>();
		while (token.type != TokenType.EOF) {
			DeclarationTree<?> decl = parseDeclaration();
			if (decl != null) declarations.add(decl);
		}
		return new CompilationUnitTree(filename,packageTree,declarations);
	}
	private DeclarationTree<?> parseDeclaration() {
		Location startLocation = token.location;
		List<ModifierTree> modifiers = readModifiers();
		if (token.text.equals(Compiler.PACKAGE)) {
			if (packageTree == null && declarations.isEmpty()) {
				packageTree = parsePackage(modifiers,startLocation);
				if (token.type == TokenType.EOF)
					return null;
				startLocation = token.location;
				modifiers = readModifiers();
			}
		}
		if (token.type == TokenType.KEYWORD) switch (token.text) {
			case "typename":
				return parseTypenameDeclaration(modifiers,startLocation);
			case "import":
				return parseImportDeclaration(modifiers,startLocation);
			case "struct":
			case "namespace":
				return parseNamespaceDeclaration(modifiers,token.text,startLocation);
		}
		TypeElementTree type = parseTypeElement();
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

	private PackageTree parsePackage(List<ModifierTree> modifiers, Location startLocation) {
		Token keyword = assertAndNext(Compiler.PACKAGE);
		StaticElementReferenceTree name = parseStaticElementReference();
		Token endLine = assertAndNext(TokenType.ENDLINE);
		return new PackageTree(modifiers,keyword,name,endLine,Location.between(startLocation,endLine.location));
	}

	private NamespaceDeclaration parseNamespaceDeclaration(List<ModifierTree> modifiers, String namespaceTypeKeyword, Location startLocation) {
		Token keyword = assertAndNext(namespaceTypeKeyword);
		List<NamespaceDeclarator> declarators = new ArrayList<>();
		do {
			declarators.add(parseNamespace());
		} while (token.text.equals(","));
		return new NamespaceDeclaration(modifiers,declarators,Location.between(startLocation,token.location),keyword);
	}

	private NamespaceDeclarator parseNamespace() {
		Token name = null;
		if (token.type == TokenType.IDENTIFIER) {
			name = token;
			token = nextToken();
		}
		Token open = assertAndNext(TokenType.OPEN);
		List<DeclarationTree<?>> declarations = new ArrayList<>();
		while (token.type != TokenType.CLOSE) {
			declarations.add(parseDeclaration());
		}
		Token close = token;
		token = nextToken();
		return new NamespaceDeclarator(name,open,declarations,close);
	}

	private ImportDeclarationTree parseImportDeclaration(List<ModifierTree> modifiers, Location startLocation) {
		Token keyword = assertAndNext("import");
		List<ImportDeclaratorTree> declarators = new ArrayList<>();
		while (token.type != TokenType.ENDLINE) declarators.add(parseImportDeclarator());
		Token endLine = token;
		token = nextToken();
		return new ImportDeclarationTree(modifiers,keyword,declarators,endLine,Location.between(startLocation,endLine.location));
	}

	private ImportDeclaratorTree parseImportDeclarator() {
		StaticElementReferenceTree name = parseStaticElementReference();
		if (token.text.equals("=")) {
			Token assign = token;
			token = nextToken();
			Token alias = assertAndNext(TokenType.IDENTIFIER);
			return new ImportDeclaratorTree(alias,name,assign,Location.between(name.getLocation(),alias.location));
		}
		if (token.text.equals(",")) {
			token = nextToken();
		}
		return new ImportDeclaratorTree(null,name,null,name.getLocation());

	}

	private TypenameDeclarationTree parseTypenameDeclaration(List<ModifierTree> modifiers, Location startLocation) {
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
		List<ModifierTree> postModifiers = readPostModifiers();
		ThrowsTree throwsTree = null;
		if (token.text.equals("throws")) {
			throwsTree = parseThrows();
		}

		FunctionDeclaratorTree declarator = new FunctionDeclaratorTree(name, parameterList, postModifiers,throwsTree);
		if (token.type == TokenType.OPEN) {
			BlockStatementTree block = parseBlockStatement();
			return new FunctionDefinitionTree(declarator,block,Location.between(name.location,block.getLocation()));
		}
		return declarator;
	}

	private ThrowsTree parseThrows() {
		Token keyword = assertAndNext("throws");
		List<TypeElementTree> exceptionTypes = new ArrayList<>();
		while(token.type == TokenType.IDENTIFIER) {
			exceptionTypes.add(parseTypeElement());
			if (token.text.equals(",")) {
				token = nextToken();
				continue;
			}
			break;
		}
		return new ThrowsTree(keyword,exceptionTypes);
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
					case "goto":
						return parseGotoStatement();
					case "break":
						return parseBreakStatement();
					case "continue":
						return parseContinueStatement();
					case "try":
						return parseTryCatchFinallyStatement();
					case "throw":
						return parseThrowStatement();
					case "nop":
						return parseNopStatement();
					/*case "assert":
						return parseAssertStatement();
					case "switch":
						return parseSwitchStatement();*/
				}
				break;
			case ENDLINE:
				return parseNopStatement();
			case IDENTIFIER:
				if (seeNextToken().text.equals(":"))
					return parseLabelStatement();
				break;
		}
		int oldTok = curTok;
		CompilerException notAnExpression = null;
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
		} catch (UnexpectedTokenException e) {
			notAnExpression = e;
		}
		curTok = oldTok;
		token = currentToken();

		try {
			return parseExpressionStatement();
		} catch (CompilerException e) {
			e.initCause(notAnExpression);
			throw e;
		}
	}

	private ThrowStatement parseThrowStatement() {
		Token keyword = assertAndNext("throw");
		ExprTree throwExpr = parseExpr();
		Token endLine = assertAndNext(TokenType.ENDLINE);
		return new ThrowStatement(keyword,throwExpr,endLine);
	}

	private TryCatchFinallyStatementTree parseTryCatchFinallyStatement() {
		Token tryKeyword = assertAndNext("try");
		StatementTree tryBlock = parseStatement();
		List<TryCatchFinallyStatementTree.CatchBlock> catches = new ArrayList<>();
		while (token.text.equals("catch")) {
			Token catchKeyword = assertAndNext("catch");
			ParameterListTree exceptions = parseParameterList();
			StatementTree catchBlock = parseStatement();
			catches.add(new TryCatchFinallyStatementTree.CatchBlock(catchKeyword,exceptions,catchBlock));
		}
		Token finallyKeyword = null;
		StatementTree finallyBlock = null;
		if (token.text.equals("finally")) {
			finallyKeyword = token;
			token = nextToken();
			finallyBlock = parseStatement();
		}
		return new TryCatchFinallyStatementTree(tryKeyword,tryBlock,catches,finallyKeyword,finallyBlock);
	}

	private ContinueStatement parseContinueStatement() {
		Token keyword = assertAndNext("continue");
		Token endLine = assertAndNext(TokenType.ENDLINE);
		return new ContinueStatement(keyword,endLine);
	}

	private BreakStatementTree parseBreakStatement() {
		Token keyword = assertAndNext("break");
		Token endLine = assertAndNext(TokenType.ENDLINE);
		return new BreakStatementTree(keyword,endLine);
	}

	private GotoStatementTree parseGotoStatement() {
		Token keyword = assertAndNext("goto");
		Token labelName = assertAndNext(TokenType.IDENTIFIER,"label name");
		Token endLine = assertAndNext(TokenType.ENDLINE);
		return new GotoStatementTree(keyword,labelName,endLine);
	}

	private LabelStatementTree parseLabelStatement() {
		Token name = assertAndNext(TokenType.IDENTIFIER);
		Token colon = assertAndNext(":");
		return new LabelStatementTree(name,colon);
	}

	private NopStatementTree parseNopStatement() {
		Token nop = token;
		if (nop.text.equals("nop")) {
			token = nextToken();
			Token endLine = assertAndNext(TokenType.ENDLINE);
			return new NopStatementTree(nop,endLine);
		} else {
			assertAndNext(TokenType.ENDLINE);
			return new NopStatementTree(null,nop);
		}
	}

	private ForStatementTree parseForStatement() {
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

		assertAndNext(TokenType.OPENROUND);
		ExprTree condition = parseExpr();
		assertAndNext(TokenType.CLOSEROUND);

		StatementTree statement = parseStatement();
		Token elseKeyword = null;
		StatementTree elseStatement = null;
		if (token.text.equals("else")) {
			elseKeyword = token;
			token = nextToken();
			elseStatement = parseStatement();
		}
		return new IfStatementTree(keyword,condition,statement,elseKeyword,elseStatement);
	}

	//region assert

	private Token assertAndNext(TokenType type, String expected) {
		Token ret = assertToken(type, expected);
		token = nextToken();
		return ret;
	}
	private Token assertToken(TokenType type, String expected) {
		return assertToken(token,type,expected);
	}
	private static Token assertToken(Token token, TokenType type, String expected) {
		if (token.type != type) {
			throw new UnexpectedTokenException(token, expected + " expected");
		}
		return token;
	}
	private Token assertAndNext(TokenType expected) {
		Token ret = assertToken(expected);
		token = nextToken();
		return ret;
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

	private Token assertAndNext(String expected) {
		Token ret = assertToken(expected);
		token = nextToken();
		return ret;
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
			TypeElementTree type = parseTypeElement();
			DeclaratorTree declarator = null;
			if (!token.text.equals(",") && token.type != TokenType.CLOSEROUND) declarator = parseDeclarator();
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


	private TypeElementTree parseTypeElement() {
		Token _const = null;
		Token _restrict = null;
		Location startLocation = token.location;
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
		TypeElementTree type;
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
		} else if (token.text.equals("decltype")) {
			type = parseDeclType(_const, _restrict);
		} else if (token.type == TokenType.IDENTIFIER) {
			StaticElementReferenceTree reference = parseStaticElementReference();
			type = new TypeReferenceElementTree(_const, _restrict, reference, Location.between(startLocation,reference.getLocation()));
		} /*else if (token.text.equals("struct")) {
			type = new TypeStructElementTree(_const, parseStruct());
		} */else if (token.type == TokenType.EOF) {
			throw new UnexpectedTokenException(token,"'}' expected");
		} else {
			throw new UnexpectedTokenException(token,"type expected");
		}
		while (token.type == TokenType.OPENSQUARE || token.text.equals("*") || token.text.equals("&")) {
			Token spec = token;
			if (token.type == TokenType.OPENSQUARE) {
				token = nextToken();
				ExprTree size = null;
				if (token.type != TokenType.CLOSESQUARE) {
					size = parseExpr();
					if (token.type != TokenType.CLOSESQUARE)
						throw new UnexpectedTokenException(token,']');
				}
				Token closeSquare = token;
				type = size == null ?
						new ArrayTypeElementTree(null,null,type,spec,closeSquare) :
						new StaticArrayTypeElementTree(null,null,type,spec,size,closeSquare);

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

	private TypeElementTree parseDeclType(Token _const, Token _restrict) {
		Token keyword = assertAndNext("decltype");
		Token openRound = assertAndNext(TokenType.OPENROUND);
		ExprTree expression = parseExpr();
		Token closeRound = assertAndNext(TokenType.CLOSEROUND);
		return new DeclTypeElementTree(_const,_restrict,keyword,openRound,expression,closeRound);
	}

	private StaticElementReferenceTree parseStaticElementReference() {
		List<ReferenceExprTree> references = new ArrayList<>();
		while (true) {
			references.add(new ReferenceExprTree(token));
			token = nextToken();
			if (token.text.equals(".")) {
				token = nextToken();
				continue;
			}
			break;
		}
		return new StaticElementReferenceTree(references);
	}


	//region #parseExpr



	public ExprTree parseExpr() {

		ExprTree lhs = parse_Primary();


		token = nextToken();


		if (token.text.equals("?")) {
			return parseTernaryOp(lhs);
		}

		return parseBinOpRhs(0, lhs);
	}

	private ExprTree parseTernaryOp(ExprTree lhs) {
		Token question = assertAndNext("?");
		ExprTree ifTrue = parseExpr();
		Token el = assertAndNext(":");
		ExprTree ifFalse = parseExpr();
		return new TernaryExprTree(lhs, question, ifTrue, el, ifFalse);
	}

	private IndexListTree parseIndexList() {
		Token open = assertAndNext(TokenType.OPENSQUARE);
		List<ExprTree> args = new ArrayList<>();
		while (token.type != TokenType.CLOSESQUARE) {
			args.add(parseExpr());
			if (token.text.equals(",")) {
				token = nextToken();
				continue;
			}
			if (token.type == TokenType.CLOSESQUARE) break;
			throw new UnexpectedTokenException(token,"',' or ']' expected");
		}
		Token close = token;
		token = nextToken();
		return new IndexListTree(open,args,close);
	}

	private ExprTree parseBinOpRhs(int priority, ExprTree lhs) {
		while(true) {
			Token operator = token;
			int tokenPriority = Compiler.getBinaryOperatorPriority(operator.text);
			if (tokenPriority <= priority) {//чтобы x.y.z читалось как (x.y).z, а не x.(y.z)
				return lhs;
			}
			token = nextToken();

			ExprTree rhs = parse_Primary();
			token = nextToken();

			int nextPriority = Compiler.getBinaryOperatorPriority(token.text);
			if (tokenPriority < nextPriority) {
				rhs = parseBinOpRhs(tokenPriority+1,rhs);
			}

			lhs = new BinaryExprTree(lhs,operator,rhs);
			continue;
		}
	}


	private ExprTree parse_Primary() {
		int startPos = curTok;
		CompilerException typeException = null;
		TRY_TO_READ_INIT_LIST:
		try {
			if (token.type == TokenType.IDENTIFIER || token.type == TokenType.KEYWORD) {
				TypeElementTree type = parseTypeElement();
				if (token.type == TokenType.OPEN)
					return parse_InitializerList(type);
			}
		} catch (UnexpectedTokenException e){
			typeException = e;
		}
		curTok = startPos;
		token = currentToken();


		Token prefixOperator = null;
		int prefixPriority = 0;
		if (token.type == TokenType.OPERATOR) {
			prefixOperator = token;
			prefixPriority = Compiler.getPrefixOperatorPriority(prefixOperator.text);
			token = nextToken();
		}

		ExprTree expr = parse_Primary0();


		while (seeNextToken().type == TokenType.OPENSQUARE) {
			token = nextToken();
			expr = new IndexExprTree(expr,parseIndexList());
			curTok--;
		}

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

	private ExprTree parse_Primary0() {
		switch (token.type) {
			case NUMBER:
				return new LiteralExprTree(token, LiteralType.INTEGER_LITERAL);
			case CHARS:
				return new LiteralExprTree(token, LiteralType.CHAR_LITERAL);
			case STRING:
				return new LiteralExprTree(token, LiteralType.STRING_LITERAL);
			case OPENROUND:
				return parse_ParentExpr();
			case OPEN:
				return parse_InitializerList();
			case KEYWORD: {
				switch (token.text) {
					case "false":
					case "true":
						return new LiteralExprTree(token, LiteralType.BOOLEAN_LITERAL);
					case "new":
						return parse_NewExpr();
					case "delete":
						return parse_DeleteExpr();
					case "sizeof":
						return parse_SizeofExpr();
				}
			}
			case IDENTIFIER:
				return parse_IdentifierExpr();
		}
		throw new UnexpectedTokenException(token,"primary expression expected");
	}

	private ExprTree parse_SizeofExpr() {
		Token keyword = assertAndNext("sizeof");
		Token openRound = assertAndNext(TokenType.OPENROUND);
		TypeElementTree type = parseTypeElement();
		Token closeRound = assertToken(TokenType.CLOSEROUND);
		return new SizeOfExprTree(Location.between(keyword.location,closeRound.location),type);
	}

	private ExprTree parse_NewExpr() {
		Token keyword = assertAndNext("new");
		Tree args = null;
		if (token.type == TokenType.OPENROUND) {
			args = parse_ArgumentList();
			token = nextToken();
		} else if (token.type == TokenType.OPENSQUARE) {
			args = parseIndexList();
		}
		ExprTree expression = parseExpr();
		curTok--;
		token = currentToken();
		return new NewExprTree(keyword,args,expression);
	}
	private ExprTree parse_DeleteExpr() {
		Token keyword = assertAndNext("delete");
		Tree args = null;
		if (token.type == TokenType.OPENROUND) {
			args = parse_ArgumentList();
			token = nextToken();
		} else if (token.type == TokenType.OPENSQUARE) {
			args = parseIndexList();
		}
		ExprTree expression = parseExpr();
		curTok--;
		token = currentToken();
		return new DeleteExprTree(keyword,args,expression);
	}


	private ExprTree parse_IdentifierExpr() {
		ReferenceExprTree ref = new ReferenceExprTree(token);
		if (seeNextToken().type == TokenType.OPENROUND) {
			token = nextToken();
			ArgumentListTree args = parse_ArgumentList();
			return new CallExprTree(ref,args);
		}
		return ref;
	}

	private ArgumentListTree parse_ArgumentList() {
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

	private InitializerListExprTree parse_InitializerList() {
		return parse_InitializerList(null);
	}
	private InitializerListExprTree parse_InitializerList(@Nullable TypeElementTree type) {
		Token open = assertAndNext(TokenType.OPEN);
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
		return new InitializerListExprTree(type,open,initializers,close);
	}

	private ExprTree parse_ParentExpr() {
		assertToken(TokenType.OPENROUND);
		Token openRound = token;
		token = nextToken();
		int startPos = curTok;
		try {
			TypeElementTree type = parseTypeElement();
			Token closeRound = assertAndNext(TokenType.CLOSEROUND);
			ExprTree exprTree = parseExpr();
			curTok--;
			token = currentToken();
			return new CastExprTree(openRound,type,closeRound,exprTree);
		} catch (CompilerException e) {
			try {
				curTok = startPos;
				ExprTree expr = parseExpr();
				assertToken(TokenType.CLOSEROUND);
				return expr;
			} catch (CompilerException ee) {
				ee.initCause(e);
				throw ee;
			}
		}
	}
	//endregion



	private List<ModifierTree> readPostModifiers() {
		List<ModifierTree> modifiers = new ArrayList<>();
		while (isPostModifier(token)) {
			for (ModifierTree curMod : modifiers) {
				if (curMod.getKeyword().text.equals(token.text))
					throw new UnexpectedTokenException(token, "duplicated postfix modifier '" + token.text + "'");
			}
			ModifierTree mod;
			if (seeNextToken().type == TokenType.OPENSQUARE) {
				Token keyword = token;
				token = nextToken();
				Token openSquare = token;
				token = nextToken();
				List<Token> attributes = new ArrayList<>();
				while (true) {
					if (token.type != TokenType.IDENTIFIER && token.type != TokenType.KEYWORD && token.type != TokenType.STRING)
						throw new UnexpectedTokenException(token,"attribute expected");
					attributes.add(token);
					token = nextToken();
					if (token.type == TokenType.CLOSESQUARE) {
						Token closeSquare = token;
						mod = new ModifierTree(keyword,openSquare,attributes,closeSquare);
						break;
					} else if (token.text.equals(",")) {
						token = nextToken();
						continue;
					} else {
						throw new UnexpectedTokenException(token, ']');
					}
				}
			} else {
				mod = new ModifierTree(token);
			}
			modifiers.add(mod);
			token = nextToken();
		}
		return modifiers;
	}
	private List<ModifierTree> readModifiers() {
		List<ModifierTree> modifiers = new ArrayList<>();
		while (isModifier(token)) {
			for (ModifierTree curMod : modifiers) {
				if (curMod.getKeyword().text.equals(token.text))
					throw new UnexpectedTokenException(token, "duplicated modifier '" + token.text + "'");
			}
			ModifierTree mod;
			if (seeNextToken().type == TokenType.OPENSQUARE) {
				Token keyword = token;
				token = nextToken();
				Token openSquare = token;
				token = nextToken();
				List<Token> attributes = new ArrayList<>();
				while (true) {
					if (token.type != TokenType.IDENTIFIER && token.type != TokenType.KEYWORD && token.type != TokenType.STRING)
						throw new UnexpectedTokenException(token,"attribute expected");
					attributes.add(token);
					token = nextToken();
					if (token.type == TokenType.CLOSESQUARE) {
						Token closeSquare = token;
						mod = new ModifierTree(keyword,openSquare,attributes,closeSquare);
						break;
					} else if (token.text.equals(",")) {
						token = nextToken();
						continue;
					} else {
						throw new UnexpectedTokenException(token, ']');
					}
				}
			} else {
				mod = new ModifierTree(token);
			}
			modifiers.add(mod);
			token = nextToken();
		}
		return modifiers;
	}

	private boolean isModifier(Token token) {
		return token.type == TokenType.KEYWORD && Compiler.modifiers.contains(token.text);
	}
	private boolean isPostModifier(Token token) {
		return token.type == TokenType.KEYWORD && Compiler.postModifiers.contains(token.text);
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

		keywordsTypes.add("auto");
	}
}
