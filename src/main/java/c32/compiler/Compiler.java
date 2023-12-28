package c32.compiler;

import c32.compiler.codegen.java.Generator;
import c32.compiler.codegen.java.JavaGenerator;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.TreeBuilder;
import c32.compiler.logical.tree.SpaceInfo;
import c32.compiler.parser.Parser;
import c32.compiler.parser.ast.CompilationUnitTree;
import c32.compiler.lexer.tokenizer.ConfigurableTokenizer;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.lexer.tokenizer.Tokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;

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

			PUBLIC = "public",
			PROTECTED = "protected",
			PACKAGE = "package",
			PRIVATE = "private";

	public static final Set<String> modifiers = new HashSet<>();
	public static final Set<String> postModifiers = new HashSet<>();
	static {
		Collections.addAll(modifiers,
				PUBLIC,PROTECTED,PRIVATE,
				STATIC,
				EXTERN,NATIVE,ABSTRACT,VIRTUAL,CONSTEXPR,OVER,
				MUTABLE
				);

		postModifiers.addAll(modifiers);
		Collections.addAll(postModifiers,PURE,CONST,NOEXCEPT);
	}
	public static final Set<String> keywords = new HashSet<>();
	static {
		Collections.addAll(keywords,(
				"noexcept\n" +
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
				"constexpr\n" +
				"continue\n" +
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
				"mutable\n" +
				"namespace\n" +
				"native\n" +
				"new\n" +
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
				"while").split("\n"));
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
		".;,;->;?;:").split(";");
	static Tokenizer tokenizer = new ConfigurableTokenizer().addKeywords(keywords).addOperators(validOperators);



	private static CompilationUnitTree getAST(File file) throws IOException {
		StringBuilder sourceb = new StringBuilder();
		for(String str : Files.readAllLines(file.toPath())) {
			sourceb.append(str).append('\n');
		}
		String source = sourceb.toString();

		CompilationUnitTree AST = getAST(source, file);

		if (config.writeAST()) {
			ObjectMapper mapper = new ObjectMapper();
			File astFile = new File(file.getAbsoluteFile() + "_AST.json");
			PrintStream printStream = new PrintStream(astFile);
			printStream.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(AST.toJson(mapper)));
			printStream.close();
		}

		return AST;
	}

	private static CompilationUnitTree getAST(String source, File file) {
		Stack<Token> tokens = ((ConfigurableTokenizer)tokenizer).tokenize(source, file);
		Preprocessor.preprocess(tokens);

		CompilationUnitTree AST;
		LABEL:
		try {
			AST = new Parser().parse(tokens,file.getName());
		} catch (CompilerException e) {
			throw handleCompilerException(e,source,file);
		}
		return AST;
	}

	private static SpaceInfo build(Collection<CompilationUnitTree> units) {
		return new TreeBuilder().buildNamespace(units);
	}

	private static void compile(Collection<File> files, Collection<Generator> generators) {
		Collection<CompilationUnitTree> units = files.stream().map((file) -> {
			try {
				return getAST(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());

		deleteDirectory(new File("out"));
		try {
			SpaceInfo space = build(units);
			for (Generator generator : generators) {
				generator.generate(space);
			}
		} catch (CompilerException e) {
			if (e.getLocation() == null || e.getLocation().getSourceFile() == null)
				throw e;

			StringBuilder sourceb = new StringBuilder();
			try {
				for(String str : Files.readAllLines(e.getLocation().getSourceFile().toPath())) {
					sourceb.append(str).append('\n');
				}
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			String source = sourceb.toString();
			throw handleCompilerException(e,source,e.getLocation().getSourceFile());
		}
	}


	public static CompilerConfig config = null;
	public static void main(String... args) throws IOException{
		try {
			String filename = "Main";
			if (args.length == 0) throw new RuntimeException();
			filename = args[args.length-1];
			File file = new File(filename);
			if (!file.exists()) throw new RuntimeException();

			compile(Collections.singleton(new File(filename)),Collections.singleton(new JavaGenerator()));

			return;
		} catch (RuntimeException e) {
			try {
				File configFile = new File("Figures.json");
				Compiler.config = CompilerConfig.parse(configFile);
			} catch (Exception ee) {
				System.err.println("Invalid configuration format");
				throw ee;
			}
			compile(allC32Files(config.getSrc()),config.getTargets());
		}

		/*AST.brewJava().forEach((file) -> {
			try {
				file.writeTo(new File("c32target/generated/"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});*/

		File f = new File("out/java/out/$package.class");
		if (f.exists()) f.delete();
		System.out.println("Compiling...");
		proc("javac -d out/java/out -cp target/classes/ out/java/$package.java");

		if (f.exists()) {
			System.out.println("Starting process...\n");
			proc("java -cp out/out/;target/classes/ out/out/$package");
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

	private static CompilerException handleCompilerException(CompilerException e, String source, File file) {
		File working = new File("");
		System.err.println(getErrorDescription(e,file.getAbsolutePath().replace(working.getAbsolutePath(),""),source));
		if (e.getCause() != e && e.getCause() instanceof CompilerException) {
			System.err.println("for:");
			System.err.println(getErrorDescription((CompilerException) e.getCause(),file.getAbsolutePath().replace(working.getAbsolutePath(),""),source));
		}
		return e;
	}

	private static String getErrorDescription(CompilerException e, String filename, String source) {
		StringBuilder msg = new StringBuilder(e.getClass().getSimpleName()).append(": ").append(e.getRawMessage()).append(" (at ")
				.append(filename);
		if (e.getLocation() != null) {
			msg.append(':').append(e.getLocation().getStartLine());
		}
		msg.append(')').append("\n\n");

		if (e.getLocation() != null) {
			String line;
			try {
				line = source.split("\n")[e.getLocation().getStartLine()-1];
			} catch (ArrayIndexOutOfBoundsException ee) {
				line = " ";
			}

			int linePos = source.substring(0,e.getLocation().getStartPos()).lastIndexOf('\n');
			int errStart = e.getLocation().getStartPos() - linePos;
			int errEnd = e.getLocation().getEndPos() - linePos;

			msg.append(line).append('\n');

			char[] underline = new char[line.length()];
			for (int i = 0; i < underline.length; i++) {
				if (i >= errStart-1 && i < errEnd-1) underline[i] = '~';
				else if (line.charAt(i) == '\t') underline[i] = '\t';
				else underline[i] = ' ';
			}
			return msg.append(underline).append('\n').toString();
		}

		return msg.append('\n').toString();
	}

	private static void proc(String cmd) throws IOException{
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
		setBinaryPriority(5,"=","*=","/=","%=","+=","-=",">>=","<<=",">>>=","<<<=",">>>>=","<<<<=","&=","|=","^=");

		setBinaryPriority(15,"||");
		setBinaryPriority(16,"&&");
		setBinaryPriority(17,"|");
		setBinaryPriority(18,"^");
		setBinaryPriority(19,"&");

		setBinaryPriority(20,"==","!=");

		setBinaryPriority(25,"<","<=",">",">=");

		setBinaryPriority(30,">>>>","<<<<",">>>","<<<",">>","<<");

		setBinaryPriority(35,"+","-");

		setBinaryPriority(40,"*","%","/");

		setBinaryPriority(100,".","->");
		setBinaryPriority(120,"::");
	}
	private static void setBinaryPriority(int priority, String operator) {
		binaryOperatorPriorities.put(operator,priority);
	}
	private static void setBinaryPriority(int priority, String... operators) {
		for (String operator : operators) {
			binaryOperatorPriorities.put(operator,priority);
		}
	}
	public static int getBinaryOperatorPriority(String operator) {
		if (binaryOperatorPriorities.containsKey(operator))
			return binaryOperatorPriorities.get(operator);
		return -1;
	}



	private static final HashMap<String, Integer> prefixOperatorPriories = new HashMap<>();
	static {
		setPrefixPriority(50,"++","--","~","!","-","+","&","*");
	}
	private static void setPrefixPriority(int priority, String... operators) {
		for (String operator : operators) {
			prefixOperatorPriories.put(operator,priority);
		}
	}
	public static int getPrefixOperatorPriority(String operator) {
		if (prefixOperatorPriories.containsKey(operator))
			return prefixOperatorPriories.get(operator);
		return -1;
	}




	private static final HashMap<String, Integer> postfixOperatorPriories = new HashMap<>();
	static {
		setPostfixPriority(70,"++","--");
	}
	private static void setPostfixPriority(int priority, String... operators) {
		for (String operator : operators) {
			postfixOperatorPriories.put(operator,priority);
		}
	}
	public static int getPostfixOperatorPriority(String operator) {
		if (postfixOperatorPriories.containsKey(operator))
			return postfixOperatorPriories.get(operator);
		return -1;
	}
}
