package c32.compiler.logical.tree.statement;

import c32.compiler.logical.tree.SpaceInfo;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import c32.compiler.logical.tree.VariableInfo;
import c32.compiler.logical.tree.expression.Expression;
import c32.compiler.parser.ast.declaration.ValuedDeclarationTree;
import c32.compiler.parser.ast.declarator.DeclaratorTree;
import c32.compiler.parser.ast.declarator.VariableDeclaratorTree;
import c32.compiler.parser.ast.statement.BlockStatementTree;
import c32.compiler.parser.ast.statement.DeclarationStatementTree;
import c32.compiler.parser.ast.statement.IfStatementTree;
import c32.compiler.parser.ast.statement.StatementTree;
import lombok.var;

import java.util.ArrayList;
import java.util.List;

public interface Statement {
	static Statement build(SpaceInfo container, StatementTree statement) {
		if (statement instanceof DeclarationStatementTree) {
			//local variable
			var decl = (ValuedDeclarationTree)((DeclarationStatementTree) statement).getDeclaration();
			List<VariableInfo> variables = new ArrayList<>();
			for (DeclaratorTree declaratorTree : decl) {
				var varDec = ((VariableDeclaratorTree) declaratorTree);
				TypeRefInfo type = container.resolveType(container,decl.getTypeElement());
				if (varDec.getName() != null) variables.add(new VariableInfo(varDec.getName().text,type,
						varDec.getInitializer() != null ? Expression.build(container, varDec.getInitializer(),type) : null
				));
			}
			return new VariableDeclarationStatement(variables);
		} else if (statement instanceof IfStatementTree) {
			Expression condition = Expression.build(container,((IfStatementTree) statement).getCondition(), new TypeRefInfo(false,false, TypeInfo.PrimitiveTypeInfo.BOOL));
		}

		throw new UnsupportedOperationException();
	}
}
