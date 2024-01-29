package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.*;
import lombok.Getter;

@Getter
public class SpaceRefExpression extends SymbolRefExpression {
	private final TypeInfo returnType;
    public SpaceRefExpression(SpaceInfo space, Location location) {
        super(space, location);
	    if (get() instanceof NamespaceInfo) {
		    this.returnType = CompileTimeTypeInfo.Namespace;
	    } else if (get() instanceof TypeInfo) {
		    this.returnType =  CompileTimeTypeInfo.Typename;
	    } else {
			throw new CompilerException(location,"Invalid space reference type");
	    }
    }

	@Override
	public SpaceInfo get() {
		return (SpaceInfo) super.get();
	}

	public SpaceInfo getSpace() {
		return get();
	}

	@Override
    public TypeInfo getReturnType() {
		return returnType;
    }
}
