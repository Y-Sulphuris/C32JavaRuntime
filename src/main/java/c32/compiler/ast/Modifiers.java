package c32.compiler.ast;

import c32.compiler.Compiler;
import c32.compiler.CompilerException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Modifiers {
	private Modifiers() {
	}

	private boolean _const;
	private boolean _extern;
	private boolean _native;
	private boolean _static;


	private int accessMode = ACCESS_MODE_DEFAULT;
	public static final int ACCESS_MODE_DEFAULT = 0;
	public static final int ACCESS_MODE_PUBLIC = 1;
	public static final int ACCESS_MODE_PROTECTED = 2;
	public static final int ACCESS_MODE_PACKAGE = 3;
	public static final int ACCESS_MODE_PRIVATE = 4;

	private static final Map<String, Integer> accessModifiers = new HashMap<>();

	static {
		accessModifiers.put(Compiler.PUBLIC, ACCESS_MODE_PUBLIC);
		accessModifiers.put(Compiler.PROTECTED, ACCESS_MODE_PROTECTED);
		accessModifiers.put(Compiler.PACKAGE, ACCESS_MODE_PACKAGE);
		accessModifiers.put(Compiler.PRIVATE, ACCESS_MODE_PRIVATE);
	}

	public static Modifiers parseModifiers(Collection<String> modifiers, Set<String> availableModifiers) {
		return parseModifiers(modifiers,availableModifiers,ACCESS_MODE_DEFAULT);
	}
	public static Modifiers parseModifiers(Collection<String> modifiers, Set<String> availableModifiers, int defaultAccessModifiers) {
		for (String modifier : modifiers) {
			if (availableModifiers.contains(modifier)) continue;
			throw new CompilerException("Invalid modifier: " + modifier);
		}
		Modifiers mod = new Modifiers();
		mod._const = modifiers.remove(Compiler.CONST);
		mod._extern = modifiers.remove(Compiler.EXTERN);
		mod._native = modifiers.remove(Compiler.NATIVE);
		mod._static = modifiers.remove(Compiler.STATIC);

		for (Map.Entry<String, Integer> accessModifier : accessModifiers.entrySet()) {
			if (modifiers.remove(accessModifier.getKey())) {
				if (mod.accessMode != 0) {
					throw new CompilerException("Access modifier duplication");
				}
				mod.accessMode = accessModifier.getValue();
			}
		}
		if (mod.accessMode == 0) mod.accessMode = defaultAccessModifiers;

		if (!modifiers.isEmpty())
			throw new CompilerException("Unknown modifiers: " + modifiers);


		return mod;
	}


	public boolean is_extern() {
		return _extern;
	}

	public boolean is_const() {
		return _const;
	}

	public boolean is_native() {
		return _native;
	}

	public boolean is_static() {
		return _static;
	}

	public int getAccessMode() {
		return accessMode;
	}

	public boolean isPublic() {
		return accessMode == ACCESS_MODE_PUBLIC;
	}

	public boolean isProtected() {
		return accessMode == ACCESS_MODE_PROTECTED;
	}

	public boolean isPackage() {
		return accessMode == ACCESS_MODE_PACKAGE;
	}

	public boolean isPrivate() {
		return accessMode == ACCESS_MODE_PRIVATE;
	}
}
