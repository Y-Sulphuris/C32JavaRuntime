package c32.compiler.logical;

import c32.compiler.except.CompilerException;
import c32.compiler.parser.ast.expr.ReferenceExprTree;

public class VariableNotFoundException extends CompilerException  {
    public VariableNotFoundException(ReferenceExprTree reference) {
        super(reference.getLocation(), "cannot find variable: " + reference.getIdentifier().text);
    }
}
