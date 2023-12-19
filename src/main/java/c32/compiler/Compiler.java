package c32.compiler;

import c32.compiler.except.CompilerException;
import c32.compiler.parser.Parser;
import c32.compiler.parser.ast.CompilationUnitTree;
import c32.compiler.lexer.tokenizer.ConfigurableTokenizer;
import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.lexer.tokenizer.Tokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Compiler {

	public static final String
			CONST = "const",
			EXTERN = "extern",
			RETURN = "return",
			NATIVE = "native",
			STATIC = "static",
			RESTRICT = "restrict",

			PUBLIC = "public",
			PROTECTED = "protected",
			PACKAGE = "package",
			PRIVATE = "private";

	public static final Set<String> modifiers = new HashSet<>();
	static {
		Collections.addAll(modifiers,EXTERN,STATIC,NATIVE,PUBLIC,PROTECTED,PACKAGE,PRIVATE);
	}
	public static final Set<String> keywords = new HashSet<>();
	static {
		Collections.addAll(keywords,(
				"template\n" +
						"typename\n" +
						"class\n" +
						"public\n" +
						"protected\n" +
						"internal\n" +
						"private\n" +
						RESTRICT + "\n" +
						"final\n" +
						CONST+"\n" +
						STATIC+"\n" +
						"virtual\n" +
						"void\n" +
						"byte\n" +
						"short\n" +
						"int\n" +
						"long\n" +
						"ubyte\n" +
						"ushort\n" +
						"uint\n" +
						"ulong\n" +
						"bool\n" +
						"char8\n" +
						"char\n" +
						"char32\n" +
						"half\n" +
						"float\n" +
						"double\n" +
						"import\n" +
						"package\n" +
						"implicit\n" +
						"override\n" +
						"this\n" +
						RETURN+"\n" +
						"struct\n" +
						"union\n" +
						"assert\n" +
						"sizeof\n" +
						"abstract\n" +
						"interface\n" +
						"new\n" +
						"delete\n" +
						"typedef\n" +
						"operator\n" +
						"constexpr\n" +
						"throw\n" +
						"throws\n" +
						"native\n" +
						EXTERN+"\n" +
						"true\n" +
						"false\n" +
						"if\n" +
						"for\n" +
						"while\n" +
						"do\n" +
						"try\n" +
						"catch\n" +
						"finally\n" +
						"break\n" +
						"continue\n" +
						"switch\n" +
						"case\n" +
						"default\n" +
						"instanceof\n" +
						"super\n" +
						"quadruple\n" +
						"octuple").split("\n"));
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


	public static void main(String... args) throws IOException{
		String source = null;
		String filename = "Main";
		try {
			if (args.length == 0) throw new RuntimeException();
			StringBuilder sourceb = new StringBuilder();
			filename = args[args.length-1];
			for(String str : Files.readAllLines(new File(filename).toPath())) {
				sourceb.append(str).append('\n');
			}
			source = sourceb.toString();
		} catch (IOException e) {
			filename = args[args.length-1];
			System.out.println("No input files: " + new File(filename).getAbsolutePath());
			System.exit(0);
		} catch (RuntimeException e) {
			System.out.println("Ender code here: ('-' to abort)");
			StringBuilder sourceb = new StringBuilder();
			Scanner scanner = new Scanner(System.in);
			while (true) {
				String line = scanner.nextLine();
				if (line.equals("-")) break;
				sourceb.append(line).append('\n');
			}
			source = sourceb.toString();
		}


		Collection<Token> tokens = tokenizer.tokenize(source);
		Preprocessor.preprocess(tokens);
		/*
		for (Token token : tokens) {
			System.out.println(token);
		}
		*/
		CompilationUnitTree AST;
		try {
			AST = new Parser().parse(tokens,filename);
		} catch (CompilerException e) {
			StringBuilder msg = new StringBuilder(e.getClass().getSimpleName()).append(": ").append(e.getRawMessage()).append(" (at ")
					.append(filename).append(':').append(e.getLocation().getStartLine()).append(')').append("\n\n");

			String line = source.split("\n")[e.getLocation().getStartLine()-1];

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

			msg.append(underline).append('\n');
			System.err.println(msg);
			throw e;
		}
		ObjectMapper mapper = new ObjectMapper();
		File file = new File("AST.json");
		PrintStream printStream = new PrintStream(file);
		printStream.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(AST.toJson(mapper)));
		printStream.close();
		/*AST.brewJava().forEach((file) -> {
			try {
				file.writeTo(new File("c32target/generated/"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		File f = new File("c32target/generated/test/test_c32.class");
		if (f.exists()) f.delete();
		System.out.println("Compiling...");
		proc("javac -cp target/classes/ c32target/generated/test/test_c32.java");

		if (f.exists()) {
			System.out.println("Starting process...\n");
			proc("java -cp c32target/generated/;target/classes/ test.test_c32");
		} else {
			System.out.println("Compilation error");
		}*/
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
