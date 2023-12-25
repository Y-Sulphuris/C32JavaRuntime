package c32.compiler.logical.tree;

import java.util.List;

public interface FunctionInfo extends SpaceInfo {
	String getName();
	TypeRefInfo getReturnType();
	List<VariableInfo> getArgs();
	List<TypeRefInfo> getThrowTypes();

	boolean is_pure();
	boolean is_noexcept();
}
