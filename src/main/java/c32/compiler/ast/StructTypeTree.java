package c32.compiler.ast;

import c32.compiler.ast.type.TypeTree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StructTypeTree extends TypeTree implements VariableContainer {
	private final List<VariableDeclarationTree> fields;

	public List<VariableDeclarationTree> getFields() {
		return fields;
	}

	public StructTypeTree(String name, List<VariableDeclarationTree> fields) {
		super(name);
		this.fields = fields;
	}


	@Override
	public Set<String> availableVariableModifiers() {
		return availableVariableMod;
	}
	public static final HashSet<String> availableVariableMod = new HashSet<>();
	static {
		availableVariableMod.add("static");
		availableVariableMod.add("const");
	}

	@Override
	public VariableDeclarationTree getVariable(String name) {
		for (VariableDeclarationTree field : fields) {
			if (field.getVarName().equals(name)) return field;
		}
		throw new VariableDeclNotFoundException(this,name);
	}
}
