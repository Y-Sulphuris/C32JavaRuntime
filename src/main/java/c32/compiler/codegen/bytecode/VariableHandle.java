package c32.compiler.codegen.bytecode;

import c32.compiler.logical.tree.VariableInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.MethodVisitor;

@Getter
@RequiredArgsConstructor
public abstract class VariableHandle {
	private final VariableInfo targetVariable;

	public abstract void loadMe(MethodVisitor mv);
}
@Getter
class IndexedVariableHandle extends VariableHandle {

	private final int index;

	public IndexedVariableHandle(VariableInfo targetVariable, int index) {
		super(targetVariable);
		this.index = index;
	}

	@Override
	public void loadMe(MethodVisitor mv) {
		mv.visitVarInsn(ASMUtils.getLoadInstruction(this.getTargetVariable().getTypeRef().getType()), index);
	}
}