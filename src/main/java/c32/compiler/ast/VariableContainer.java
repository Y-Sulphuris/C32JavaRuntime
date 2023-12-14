package c32.compiler.ast;

import java.util.*;

public interface VariableContainer {
	Set<String> availableVariableModifiers();

	VariableDeclarationTree getVariable(String name);

}
