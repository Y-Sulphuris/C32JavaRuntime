package c32.compiler.parser.ast;


import c32.compiler.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface Tree {
	Location getLocation();
	JsonNode toJson(ObjectMapper mapper);
}
