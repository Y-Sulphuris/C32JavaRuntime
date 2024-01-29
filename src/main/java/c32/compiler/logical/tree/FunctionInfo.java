package c32.compiler.logical.tree;

import c32.compiler.Location;
import c32.compiler.logical.FunctionNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

public interface FunctionInfo extends SymbolInfo {
	String getName();
	TypeInfo getReturnType();
	List<VariableInfo> getArgs();
	List<TypeInfo> getThrowTypes();

	SpaceInfo getParent();

	boolean is_pure();
	boolean is_noexcept();
	boolean is_extern();
	boolean is_native();

	default String getFullNameEx() {
		StringBuilder builder = new StringBuilder(getFullName()).append('[');
		List<VariableInfo> args = getArgs();
		for (int i = 0; i < args.size(); i++) {
			VariableInfo arg = args.get(i);
			builder.append(arg.getTypeRef().getType().getCanonicalName());
			if (i != args.size() - 1) builder.append(", ");
		}
		builder.append(']');
		return builder.toString();
	}

	default boolean equalsDeclarationSignature(FunctionInfo other) {
		if (this.getArgs().size() != other.getArgs().size()) return false;
		if (!this.getName().equals(other.getName())) return false;
		for (int i = 0; i < other.getArgs().size(); i++) {
			if (!this.getArgs().get(i).getTypeRef().getType().equals(other.getArgs().get(i).getTypeRef().getType()))
				return false;
		}
		return true;
	}

	Location getLocation();
}
