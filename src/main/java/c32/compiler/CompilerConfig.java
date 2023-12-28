package c32.compiler;

import c32.compiler.codegen.cs.CSGenerator;
import c32.compiler.codegen.java.Generator;
import c32.compiler.codegen.java.JavaGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Data
public final class CompilerConfig {
	private final String mainFunctionName;
	private final File src;
	private final Set<Generator> targets;
	public static CompilerConfig parse(File configFile) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonParser parser = mapper.createParser(configFile);
		TreeNode node = parser.readValueAsTree();
		File src = new File(node.get("source-path").toString().replace("\"",""));
		String mainFunctionName = node.get("startup").toString().replace("\"","");
		int targetsCount = (node.get("target")).size();
		Set<Generator> targets = new HashSet<>(targetsCount);
		for (int i = 0; i < targetsCount; i++) {
			String str = node.get("target").get(i).toString();
			switch (str.replace("\"","").toLowerCase()) {
				case "java":
					targets.add(new JavaGenerator());
					break;
				case "cs":
				case "c#":
					targets.add(new CSGenerator());
					break;
				default:
					System.err.println("Unknown target type: " + str);
			}
		}
		parser.close();
		return new CompilerConfig(mainFunctionName,src,targets);
	}

	public boolean writeAST() {
		return false;
	}
}
