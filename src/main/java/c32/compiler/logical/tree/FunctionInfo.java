package c32.compiler.logical.tree;

import java.util.List;

public interface FunctionInfo extends SpaceInfo {
	String getName();
	TypeRefInfo getReturnType();
	List<LocalVariableInfo> getArgs();
	List<TypeRefInfo> getThrowTypes();
}
