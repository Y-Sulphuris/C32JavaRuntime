package c32.compiler.ast;

import c32.compiler.ast.expr.ExprTree;
import c32.compiler.ast.statement.VariableDeclarationStatementTree;
import c32.compiler.ast.type.StructTypeTree;
import c32.compiler.ast.type.TypeTree;
import c32.compiler.tokenizer.UnexpectedTokenException;

import java.util.List;

public class VariableExprTree extends ExprTree {
	private final VariableDeclarationStatementTree var;
	VariableExprTree(VariableDeclarationStatementTree var, TypeTree retType) {
		super(retType == null ? var.getType() : retType);
		this.var = var;
	}
	VariableExprTree(VariableDeclarationStatementTree var) {
		super(var.getType());
		this.var = var;
	}
	public VariableDeclarationStatementTree getVariable() {
		return var;
	}

	@Override
	public String brewJava() {
		return var.getVarName();
	}

	@Override
	public String brewJavaAsArgument() {
		if (var.getType() instanceof StructTypeTree) {
			StructTypeTree struct = (StructTypeTree) var.getType();
			StringBuilder builder = new StringBuilder();
			List<VariableDeclarationStatementTree> fields = struct.getFields();
			for (int i = 0; i < fields.size(); i++) {
				VariableDeclarationStatementTree field = fields.get(i);
				builder.append("_").append(var.getVarName()).append("_").append(field.getVarName());
				if (i != fields.size() - 1) builder.append(",");
			}
			return builder.toString();
		}
		return super.brewJavaAsArgument();
	}

	@Override
	public boolean isLeftValue() {
		return !var.getModifiers().is_const();
	}

}
