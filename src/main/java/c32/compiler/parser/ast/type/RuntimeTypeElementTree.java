package c32.compiler.parser.ast.type;

import c32.compiler.lexer.tokenizer.Token;
import c32.compiler.parser.ast.expr.ReferenceExprTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public abstract class RuntimeTypeElementTree extends TypeElementTree {
	@Nullable private Token _const;
	@Nullable private Token _restrict;

	@Override
	public abstract Token getKeyword();
	public abstract ReferenceExprTree getTypeReference();


	@Override
	public JsonNode toJson(ObjectMapper mapper) {
		ObjectNode root = mapper.createObjectNode();
		if (_const != null) root.put("const",_const.text);
		return applyJson(mapper,root);
	}

	protected abstract ObjectNode applyJson(ObjectMapper mapper, ObjectNode root);
}
