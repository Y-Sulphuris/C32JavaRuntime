package c32.compiler.logical.tree;

import java.util.List;

public interface FunctionInfo extends SymbolInfo {
	String getName();
	TypeInfo getReturnType();
	List<VariableInfo> getArgs();
	List<TypeInfo> getThrowTypes();

	boolean is_pure();
	boolean is_noexcept();
	boolean is_extern();
	boolean is_native();
}
