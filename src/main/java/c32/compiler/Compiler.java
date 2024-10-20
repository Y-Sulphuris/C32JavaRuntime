package c32.compiler;

import c32.compiler.codegen.Generator;
import c32.compiler.codegen.bytecode.JVMGenerator;
import c32.compiler.except.CompilerException;
import c32.compiler.lexer.tokenizer.TokenType;
import c32.compiler.logical.TreeBuilder;
import c32.compiler.logical.tree.NamespaceInfo;
import c32.compiler.parser.Parser;
import c32.compiler.parser.ast.CompilationUnitTree;
import c32.compiler.lexer.tokenizer.ConfigurableTokenizer;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.lexer.tokenizer.Tokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class Compiler {

	public static final String
			CONST = "const",
			EXTERN = "extern",
			RETURN = "return",
			NATIVE = "native",
			STATIC = "static",
			RESTRICT = "restrict",
			PURE = "pure",
			MUTABLE = "mutable",
			ABSTRACT = "abstract",
			VIRTUAL = "virtual",
			CONSTEXPR = "constexpr",
			OVER = "over",
			NOEXCEPT = "noexcept",
			MUT = "mut",
			UNCHECKED = "unchecked",

	PUBLIC = "public",
			PROTECTED = "protected",
			PACKAGE = "package",
			PRIVATE = "private";

	public static final Set<String> modifiers = new HashSet<>();
	public static final Set<String> postModifiers = new HashSet<>();

	static {
		Collections.addAll(modifiers,
				PUBLIC, PROTECTED, PRIVATE,
				STATIC,
				EXTERN, NATIVE, ABSTRACT, VIRTUAL, CONSTEXPR, OVER,
				MUTABLE, "register"
		);

		postModifiers.addAll(modifiers);
		Collections.addAll(postModifiers, PURE, NOEXCEPT, MUT, UNCHECKED);
	}

	public static final Set<String> keywords = new HashSet<>();

	static {
		Collections.addAll(keywords, (
				"abstract\n" +
						"assert\n" +
						"auto\n" +
						"bool\n" +
						"break\n" +
						"byte\n" +
						"case\n" +
						"catch\n" +
						"char\n" +
						"char32\n" +
						"char8\n" +
						"class\n" +
						"concept\n" +
						"const\n" +
						"consteval\n" +
						"constexpr\n" +
						"continue\n" +
						"decltype\n" +
						"default\n" +
						"delete\n" +
						"do\n" +
						"double\n" +
						"else\n" +
						"enum\n" +
						"extends\n" +
						"extern\n" +
						"false\n" +
						"final\n" +
						"finally\n" +
						"float\n" +
						"for\n" +
						"goto\n" +
						"half\n" +
						"if\n" +
						"implements\n" +
						"import\n" +
						"instanceof\n" +
						"int\n" +
						"interface\n" +
						"internal\n" +
						"long\n" +
						"mut\n" +
						"mutable\n" +
						"namespace\n" +
						"native\n" +
						"new\n" +
						"noexcept\n" +
						"nop\n" +
						"null\n" +
						"octuple\n" +
						"operator\n" +
						"over\n" +
						"package\n" +
						"private\n" +
						"protected\n" +
						"public\n" +
						"pure\n" +
						"quadruple\n" +
						"register\n" +
						"requires\n" +
						"restrict\n" +
						"return\n" +
						"short\n" +
						"sizeof\n" +
						"static\n" +
						"struct\n" +
						"super\n" +
						"switch\n" +
						"template\n" +
						"this\n" +
						"throw\n" +
						"throws\n" +
						"true\n" +
						"try\n" +
						"typedef\n" +
						"typename\n" +
						"ubyte\n" +
						"uint\n" +
						"ulong\n" +
						"union\n" +
						"ushort\n" +
						"virtual\n" +
						"void\n" +
						"while\n" +
						"unchecked").split("\n"));
	}

	public static final String[] validOperators = ("=;" +
			"+;-;/;*;%;" +
			"+=;-=;/=;*=;%=;" +
			">>;>>>;>>>>;<<<<;<<<;<<;" +
			">>=;>>>=;>>>>=;<<<<=;<<<=;<<=;" +
			"|;&;^;~;" +
			"|=;&=;^=;~=;" +
			"||;&&;!;" +
			"==;!=;>;<;>=;<=;" +
			"++;--;" +
			".;,;->;?;:;::").split(";");
	static Tokenizer tokenizer = new ConfigurableTokenizer().addKeywords(keywords).addOperators(validOperators);


	private CompilationUnitTree getAST(File file) throws IOException {
		StringBuilder sourceb = new StringBuilder();
		for (String str : Files.readAllLines(file.toPath())) {
			sourceb.append(str).append('\n');
		}
		String source = sourceb.toString();

		CompilationUnitTree AST = getAST(source, file);

		if (AST != null && config.writeAST() && !file.getParentFile().getName().equals("std")) {
			ObjectMapper mapper = new ObjectMapper();
			File astFile = new File(file.getAbsoluteFile() + "_AST.json");
			PrintStream printStream = new PrintStream(astFile);
			printStream.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(AST.toJson(mapper)));
			printStream.close();
		}

		return AST;
	}

	private CompilationUnitTree getAST(String source, File file) {
		Stack<Token> tokens = ((ConfigurableTokenizer) tokenizer).tokenize(source, file);
		tokens = Preprocessor.preprocess(tokens);

		//System.out.println(file.getName() + ":");
		//for (Token token : tokens) {
		//	System.out.print(token.text + ' ');
		//	if (token.type == TokenType.CLOSE || token.type == TokenType.ENDLINE || token.type == TokenType.OPEN) System.out.println();
		//}

		CompilationUnitTree AST;
		LABEL:
		try {
			AST = new Parser().parse(tokens, file.getName());
		} catch (CompilerException e) {
			throw handleCompilerException(e, source, file);
		}
		return AST;
	}

	private static NamespaceInfo build(Collection<CompilationUnitTree> units) {
		return new TreeBuilder().buildNamespace(units);
	}

	private void compile(Collection<File> files, Collection<Generator> generators, File outputDirectory) {
		Collection<CompilationUnitTree> units;
		{
			long start = System.currentTimeMillis();

			units = files.stream().map((file) -> {
				try {
					return getAST(file);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}).collect(Collectors.toList());

			long end = System.currentTimeMillis();

			System.out.println("Parsing (stage 1): " + (end - start) + "ms");
		}

		if (outputDirectory.exists()) {
			if (outputDirectory.isDirectory())
				deleteDirectory(outputDirectory);
			else
				throw new RuntimeException("Invalid output directory: " + outputDirectory);
		}
		try {
			long start = System.currentTimeMillis();

			NamespaceInfo space = build(units);

			long end = System.currentTimeMillis();
			System.out.println("Parsing (stage 2): " + (end - start) + "ms");
			for (Generator generator : generators) {
				long gstart = System.currentTimeMillis();
				generator.generate(space, outputDirectory);
				long gend = System.currentTimeMillis();
				System.out.println("Generating for target '" + generator.getClass().getSimpleName().replace("Generator", "") + "': "
						+ (gend - gstart) + "ms");
			}
		} catch (CompilerException e) {
			if (e.getLocation() == null || e.getLocation().getSourceFile() == null)
				throw e;

			StringBuilder sourceb = new StringBuilder();
			try {
				for (String str : Files.readAllLines(e.getLocation().getSourceFile().toPath())) {
					sourceb.append(str).append('\n');
				}
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			String source = sourceb.toString();
			throw handleCompilerException(e, source, e.getLocation().getSourceFile());
		}
	}

	private final CompilerConfig config;

	public String getMainFunctionName() {
		return config.getMainFunctionName();
	}

	private Compiler(CompilerConfig config) {
		this.config = config;
	}


	public static void main(String... args) throws IOException {
		System.out.println("Compiling...");
		long start = System.currentTimeMillis();
		/*try {
			String filename = "Main";
			if (args.length == 0) throw new RuntimeException();
			filename = args[args.length-1];
			File file = new File(filename);
			if (!file.exists()) throw new RuntimeException();

			compile(Collections.singleton(new File(filename)),Collections.singleton(new JavaGenerator()));

			return;
		} catch (RuntimeException e)*/
		{
			CompilerConfig config;
			try {
				File configFile = new File("Figures.json");
				config = CompilerConfig.parse(configFile);
				long end = System.currentTimeMillis();
				System.out.println("Config parsed: " + (end - start) + "ms\n");
			} catch (Exception ee) {
				System.err.println("Invalid configuration format");
				throw ee;
			}
			Compiler compiler = new Compiler(config);
			try {
				compiler.compile(allC32Files(config.getSrc()), config.getTargets(), new File("out"));
			} catch (CompilerException e) {
				if (config.isDebug()) e.printStackTrace(System.err);
				System.exit(1);
			}
			long end = System.currentTimeMillis();
			System.out.println("Finished (total: " + (end - start) + "ms)\n");
		}

		/*AST.brewJava().forEach((file) -> {
			try {
				file.writeTo(new File("c32target/generated/"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});*/

		File f = new File("out/jvm/");
		//f.mkdirs();
		//proc("javac -d out/java/out -cp ../target/classes/;out/java/ out/java/$package.java");

		if (f.exists()) {
			System.out.println("Starting process...\n");
			proc("java -cp out/jvm/;../target/classes/;../lib/NativesInit.jar c32.$package");
		} else {
			System.out.println("Compilation error");
		}
	}

	private static Collection<File> allC32Files(File dir) {
		Collection<File> files = new HashSet<>();
		File[] contents = dir.listFiles();
		assert contents != null;
		for (File file : contents) {
			if (!file.isDirectory()) {
				if (file.getName().endsWith(".c32")) files.add(file);
			} else {
				files.addAll(allC32Files(file));
			}
		}
		return files;
	}
	
	private static void deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		directoryToBeDeleted.delete();
	}
	
	private CompilerException handleCompilerException(CompilerException e, String source, File file) {
		String filename = file.getAbsolutePath().replace(config.getSrc().getAbsolutePath(), "");
		filename = filename.substring(1).replaceAll("\\|/", ".");
		System.err.println(getErrorDescription(e, filename, source));
		if (e.getCause() != e && e.getCause() instanceof CompilerException) {
			System.err.println("for:");
			System.err.println(getErrorDescription((CompilerException) e.getCause(), filename, source));
		}
		return e;
	}
	
	private static String getErrorDescription(CompilerException e, String filename, String source) {
		StringBuilder msg = new StringBuilder(e.getClass().getSimpleName()).append(": ").append(e.getRawMessage()).append(" (")
				.append(filename);
		if (e.getLocation() != null) {
			msg.append(':').append(e.getLocation().getStartLine());
		}
		msg.append(')').append("\n\n");
		
		if (e.getLocation() != null) {
			String line;
			try {
				line = source.split("\n")[e.getLocation().getStartLine() - 1];
			} catch (ArrayIndexOutOfBoundsException ee) {
				line = " ";
			}
			
			int linePos = source.substring(0, e.getLocation().getStartPos()).lastIndexOf('\n');
			int errStart = e.getLocation().getStartPos() - linePos;
			int errEnd = e.getLocation().getEndPos() - linePos;
			
			msg.append(line).append('\n');
			
			char[] underline = new char[line.length()];
			for (int i = 0; i < underline.length; i++) {
				if (i >= errStart - 1 && i < errEnd - 1) underline[i] = '~';
				else if (line.charAt(i) == '\t') underline[i] = '\t';
				else underline[i] = ' ';
			}
			return msg.append(underline).append('\n').toString();
		}
		
		return msg.append('\n').toString();
	}
	
	private static void proc(String cmd) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(cmd);
		
		
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		
		BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		
		// Read the output from the command
		String s = null;
		while ((s = stdInput.readLine()) != null) {
			System.out.println(s);
		}
		
		// Read any errors from the attempted command
		while ((s = stdError.readLine()) != null) {
			System.out.println(s);
		}
	}
	
	
	private static final Map<String, Integer> binaryOperatorPriorities = new HashMap<>();
	
	static {
		setBinaryPriority(5, "=", "*=", "/=", "%=", "+=", "-=", ">>=", "<<=", ">>>=", "<<<=", ">>>>=", "<<<<=", "&=", "|=", "^=");
		
		setBinaryPriority(15, "||");
		setBinaryPriority(16, "&&");
		setBinaryPriority(17, "|");
		setBinaryPriority(18, "^");
		setBinaryPriority(19, "&");
		
		setBinaryPriority(20, "==", "!=");
		
		setBinaryPriority(25, "<", "<=", ">", ">=");
		
		setBinaryPriority(30, ">>>>", "<<<<", ">>>", "<<<", ">>", "<<");
		
		setBinaryPriority(35, "+", "-");
		
		setBinaryPriority(40, "*", "%", "/");
		
		setBinaryPriority(100, ".", "->");
		setBinaryPriority(120, "::");
	}

	private static void setBinaryPriority(int priority, String operator) {
		binaryOperatorPriorities.put(operator, priority);
	}

	private static void setBinaryPriority(int priority, String... operators) {
		for (String operator : operators) {
			binaryOperatorPriorities.put(operator, priority);
		}
	}

	public static int getBinaryOperatorPriority(String operator) {
		if (binaryOperatorPriorities.containsKey(operator))
			return binaryOperatorPriorities.get(operator);
		return -1;
	}


	private static final HashMap<String, Integer> prefixOperatorPriories = new HashMap<>();

	static {
		setPrefixPriority(50, "++", "--", "~", "!", "-", "+", "&", "*");
	}

	private static void setPrefixPriority(int priority, String... operators) {
		for (String operator : operators) {
			prefixOperatorPriories.put(operator, priority);
		}
	}

	public static int getPrefixOperatorPriority(String operator) {
		if (prefixOperatorPriories.containsKey(operator))
			return prefixOperatorPriories.get(operator);
		return -1;
	}


	private static final HashMap<String, Integer> postfixOperatorPriories = new HashMap<>();

	static {
		setPostfixPriority(70, "++", "--");
	}

	private static void setPostfixPriority(int priority, String... operators) {
		for (String operator : operators) {
			postfixOperatorPriories.put(operator, priority);
		}
	}

	public static int getPostfixOperatorPriority(String operator) {
		if (postfixOperatorPriories.containsKey(operator))
			return postfixOperatorPriories.get(operator);
		return -1;
	}
}
