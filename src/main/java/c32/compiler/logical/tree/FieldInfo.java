package c32.compiler.logical.tree;

import c32.compiler.logical.tree.expression.Expression;

public interface FieldInfo {
	TypeRefInfo getTypeRef();
	String getName();
	Expression getInitializer();
	SpaceInfo getContainer();
	VariableInfo getVariable();

}
