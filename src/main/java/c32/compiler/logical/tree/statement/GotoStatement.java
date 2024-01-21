package c32.compiler.logical.tree.statement;

import c32.compiler.Location;
import c32.compiler.except.CompilerException;
import c32.compiler.logical.tree.FunctionImplementationInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GotoStatement implements Statement {
	private final FunctionImplementationInfo function;
	private final BlockStatement container;
	private final String labelName;
	private LabelStatement label;
	private final Location location;

	@Override
	public void resolveAll() {
		resolveLabel(container);
	}
	public void resolveLabel(BlockStatement container) {
		for (Statement statement : container.getStatements()) {
			if (statement instanceof LabelStatement) {
				if (((LabelStatement) statement).getName().equals(labelName)) {
					this.label = (LabelStatement) statement;
					return;
				}
			}
		}
		if (container.getParent() instanceof BlockStatement) {
			resolveLabel((BlockStatement) container.getParent());
		}
		if (this.label == null) {
			throw new CompilerException(location, "label '" + this.labelName + "' not found");
		}
	}
}
