package c32.compiler.logical;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.TypeInfo;

public class IllegalOperatorException extends CompilerException {
    public IllegalOperatorException(Location location, TypeInfo left, String operator, TypeInfo right, boolean assign) {
        super(location, "invalid operator: " + left.getCanonicalName() + " " + operator + (assign ? "= " : " ")  + right.getCanonicalName());
    }
}
