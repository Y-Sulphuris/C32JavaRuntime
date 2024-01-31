package c32.compiler.parser.ast.expr;

import c32.compiler.Location;
import c32.compiler.lexer.tokenizer.Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public class TernaryExprTree implements ExprTree {
    private final ExprTree lhs;
    private final Token question;
    private final ExprTree ifTrue;
    private final Token el;
    private final ExprTree ifFalse;

    @Override
    public Location getLocation() {
        return Location.between(lhs.getLocation(),ifFalse.getLocation());
    }

    @Override
    public JsonNode toJson(ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        node.set("hls",lhs.toJson(mapper));
        node.put("question",question.text);
        node.set("ifTrue",ifTrue.toJson(mapper));
        node.put("el",el.text);
        node.set("ifFalse",ifFalse.toJson(mapper));
        return node;
    }
}
