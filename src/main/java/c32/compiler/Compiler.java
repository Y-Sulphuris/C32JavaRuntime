package c32.compiler;

import c32.compiler.ast.ASTBuilder;
import c32.compiler.ast.CompilationUnitTree;
import c32.compiler.tokenizer.ConfigurableTokenizer;
import c32.compiler.tokenizer.Token;
import c32.compiler.tokenizer.Tokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
		Collections.addAll(modifiers,CONST);
		Collections.addAll(modifiers,EXTERN);
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
		CompilationUnitTree AST = new ASTBuilder().parse(tokens,filename);
		AST.brewJava().forEach((file) -> {
			try {
				file.writeTo(new File("c32target/generated/"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		File f = new File("c32target/generated/test/test_c32.class");
		if (f.exists()) f.delete();
		System.out.println("Compiling...");
		proc("javac c32target/generated/test/test_c32.java");

		if (f.exists()) {
			System.out.println("Starting process...\n");
			proc("java -cp c32target/generated/ test.test_c32");
		} else {
			System.out.println("Compilation error");
		}
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
}
