package c32.compiler.ast.expr;


import c32.compiler.ast.Tree;
import c32.compiler.ast.type.TypeTree;

public abstract class ExprTree implements Tree {
	public abstract String brewJava();
	private final TypeTree retType;
	public TypeTree getRetType() {
		return retType;
	}

	public ExprTree(TypeTree retType) {
		this.retType = retType;
	}

	public boolean isLeftValue() {
		return false;
	}

	public boolean canBeImplicitCastTo(TypeTree type) {
		if (retType == null) return false;
		return retType.canBeImplicitCastTo(type);
	}

	public String brewJavaAsArgument() {
		return brewJava();
	}
}
