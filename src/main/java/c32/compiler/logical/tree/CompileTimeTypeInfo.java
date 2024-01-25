package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class CompileTimeTypeInfo implements TypeInfo {
    @Override
    public Set<Weak<Expression>> getUsages() {
        throw new UnsupportedOperationException();
    }

    private final String name;

    public static final CompileTimeTypeInfo Namespace = new CompileTimeTypeInfo("namespace");

    @Override
    public boolean isAccessibleFrom(SpaceInfo space) {
        return true;
    }

    @Override
    public SpaceInfo getParent() {
        return null;
    }

    @Override
    public Collection<FunctionInfo> getFunctions() {
        return Collections.emptySet();
    }

    @Override
    public FunctionInfo addFunction(FunctionInfo function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<NamespaceInfo> getNamespaces() {
        return Collections.emptySet();
    }

    @Override
    public NamespaceInfo addNamespace(NamespaceInfo namespace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<FieldInfo> getFields() {
        return Collections.emptySet();
    }

    @Override
    public FieldInfo addField(FieldInfo field) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long sizeof() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression getDefaultValue() {
        throw new UnsupportedOperationException();
    }
}
