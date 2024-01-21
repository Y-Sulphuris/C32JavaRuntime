package c32.compiler.codegen.bytecode;

import c32.compiler.codegen.LinkerException;
import c32.compiler.logical.tree.FunctionInfo;
import c32.compiler.logical.tree.expression.CallExpression;
import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class ExternFunctionsTable {
	@RequiredArgsConstructor
	@Getter
	private static final class ExternFunctionInfo {
		private final String owner;
		private final String name;
		private final String descriptor;
	}
	private static final HashMap<String,ExternFunctionInfo> table = new HashMap();

	static {
		table.put("c32/std/$package::println£byte",new ExternFunctionInfo("c32/extern/Runtime","println",   "(B)V"));
		table.put("c32/std/$package::println£ushort",new ExternFunctionInfo("c32/extern/Runtime","uprintln","(S)V"));
		table.put("c32/std/$package::println£int",new ExternFunctionInfo("c32/extern/Runtime","println","(I)V"));
		table.put("c32/std/$package::println£long",new ExternFunctionInfo("c32/extern/Runtime","println","(J)V"));

		table.put("c32/std/$package::malloc£ulong",new ExternFunctionInfo("c32/extern/Memory","malloc","(J)J"));
	}
	public static void pushExternCall(MethodVisitor mv, FunctionInfo func, CallExpression expr) {
		int i = 0;
		for (Expression arg : expr.getArgs()) {
			JVMGenerator.pushExpression(mv, arg, expr.getFunction().getArgs().get(i++).getTypeRef().getType());
		}
		String sign = ASMUtils.asClassName(func.getParent()) + "::" + ASMUtils.asFunctionName(func);
		ExternFunctionInfo target = table.get(sign);
		if (target == null) {
			throw new LinkerException(expr.getLocation(),"extern function implementation not found: " + sign);
		}
		mv.visitMethodInsn(INVOKESTATIC, target.getOwner(), target.getName(), target.getDescriptor(),
				false);
	}
}
