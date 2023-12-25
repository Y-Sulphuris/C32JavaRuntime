package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FieldVariableInfo implements FieldInfo {
    private final VariableInfo variable;
    private final SpaceInfo container;

    @Override
    public TypeRefInfo getTypeRef() {
        return variable.getTypeRef();
    }

    @Override
    public String getName() {
        return variable.getName();
    }

    @Override
    public Expression getInitializer() {
        return variable.getInitializer();
    }
}
