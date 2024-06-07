package c32.compiler.parser.ast.type;

import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.Tree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;


@AllArgsConstructor
@Getter
@Setter
public abstract class TypeElementTree implements Tree {
	@Nullable private Token _mut;
	@Nullable private Token _const;
	@Nullable private Token _restrict;


	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode root = mapper.createObjectNode();
		if (_mut != null) root.put("mut", _mut.text);
		if (_const != null) root.put("const", _const.text);
		if (_restrict != null) root.put("restrict", _restrict.text);
		return applyJson(mapper,root);
	}

	protected abstract ObjectNode applyJson(ObjectMapper mapper, ObjectNode root);
}
