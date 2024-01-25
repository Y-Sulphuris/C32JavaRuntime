package c32.compiler.codegen.bytecode;

import c32.compiler.logical.tree.VariableInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ISTORE;

@Getter
@RequiredArgsConstructor
public abstract class VariableHandle {
	private final VariableInfo targetVariable;

	public abstract void loadMe(MethodVisitor mv);

	public abstract void storeToMe(MethodVisitor mv);
}
@Getter
class IndexedVariableHandle extends VariableHandle {

	private final int index;
	private final int storeOpcode;

	public IndexedVariableHandle(VariableInfo targetVariable, int index) {
		super(targetVariable);
		this.index = index;
		Class<?> type = ASMUtils.asJavaPrimitive(targetVariable.getTypeRef().getType());
		int opcode;
		if (type == float.class) {
			opcode = FSTORE;
		} else if (type == long.class) {
			opcode = LSTORE;
		} else if (type == double.class) {
			opcode = DSTORE;
		} else {
			if (type == int.class || type == byte.class || type == short.class || type == char.class || type == boolean.class) {
				opcode = ISTORE;
			} else {
				throw new IllegalArgumentException(type.getName());
			}
		}
		this.storeOpcode = opcode;
	}

	@Override
	public void loadMe(MethodVisitor mv) {
		mv.visitVarInsn(ASMUtils.getLoadInstruction(this.getTargetVariable().getTypeRef().getType()), index);
	}

	@Override
	public void storeToMe(MethodVisitor mv) {
		mv.visitVarInsn(storeOpcode,index);
	}
}