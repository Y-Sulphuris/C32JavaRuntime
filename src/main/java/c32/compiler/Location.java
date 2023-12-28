package c32.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.io.File;

@Data
public final class Location {
	private final int startPos;
	private final int endPos;
	private final int startLine;
	private final int endLine;
	private final File sourceFile;

	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("startPos",startPos);
		node.put("endPos",endPos);
		node.put("startLine",startLine);
		node.put("endLine",endLine);
		return node;
	}

	public static Location between(Location start, Location end) {
		if (!end.getSourceFile().equals(start.getSourceFile()))
			return start;
		return new Location(start.startPos,end.endPos,start.startLine,end.endLine, end.getSourceFile());
	}
}
