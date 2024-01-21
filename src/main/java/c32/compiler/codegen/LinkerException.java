package c32.compiler.codegen;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;

public class LinkerException extends CompilerException {
    public LinkerException(Location location, String msg) {
        super(location,msg);
    }
}
