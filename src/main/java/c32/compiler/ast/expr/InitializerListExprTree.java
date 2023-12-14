package c32.compiler.ast.expr;

import c32.compiler.ast.type.StructTypeTree;
import c32.compiler.ast.statement.VariableDeclarationStatementTree;
import c32.compiler.ast.type.TypeTree;

import java.util.List;

public class InitializerListExprTree extends ExprTree {
	private final List<ExprTree> inits;

	public List<ExprTree> getInits() {
		return inits;
	}

	public InitializerListExprTree(TypeTree struct, List<ExprTree> inits) {
		super(struct);
		this.inits = inits;
	}

	@Override
	public String brewJava() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < inits.size(); i++) {
			builder.append(inits.get(i).brewJava());
			if (i != inits.size() - 1) builder.append(',');
		}
		return builder.toString();
	}

	@Override
	public boolean canBeImplicitCastTo(TypeTree type) {
		if (type instanceof StructTypeTree) {
			StructTypeTree struct = (StructTypeTree) type;
			if (struct.getFields().size() != inits.size()) return false;
			int i = 0;
			for (VariableDeclarationStatementTree field : struct.getFields()) {
				if (!inits.get(i).canBeImplicitCastTo(field.getType())) return false;
				++i;
			}
			return true;
		}
		return super.canBeImplicitCastTo(type);
	}
}
