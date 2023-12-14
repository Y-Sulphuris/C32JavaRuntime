package c32.compiler.ast.type;

import c32.compiler.ast.VariableContainer;
import c32.compiler.ast.VariableDeclNotFoundException;
import c32.compiler.ast.statement.VariableDeclarationStatementTree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StructTypeTree extends TypeTree implements VariableContainer {
	private final List<VariableDeclarationStatementTree> fields;

	public List<VariableDeclarationStatementTree> getFields() {
		return fields;
	}

	public StructTypeTree(String name, List<VariableDeclarationStatementTree> fields) {
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
	public VariableDeclarationStatementTree getVariable(String name) {
		for (VariableDeclarationStatementTree field : fields) {
			if (field.getVarName().equals(name)) return field;
		}
		throw new VariableDeclNotFoundException(this,name);
	}
}
