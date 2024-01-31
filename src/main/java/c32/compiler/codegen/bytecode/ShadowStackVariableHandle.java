package c32.compiler.codegen.bytecode;

import c32.compiler.logical.tree.VariableInfo;
import lombok.Getter;
import org.objectweb.asm.MethodVisitor;

@Getter
public class ShadowStackVariableHandle extends VariableHandle {
	private final long offset;
	public ShadowStackVariableHandle(VariableInfo var, long offset) {
		super(var);
		this.offset = offset;
	}

	@Override
	public void loadMe(MethodVisitor mv, FunctionWriter fw) {
		fw.loadFromLocalAddress(getTargetVariable().getTypeRef().getType(), offset);
	}

	@Override
	public void storeToMe(MethodVisitor mv, FunctionWriter fw) {
		fw.storeToLocalAddress(getTargetVariable().getTypeRef().getType(), offset);
	}
}
