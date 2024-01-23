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
	private static final class EFSign { //Extern Function Signature
		private final String owner;
		private final String name;
		private final String descriptor;
	}
	private static final HashMap<String, EFSign> table = new HashMap<>();

	static {
		table.put("std.println[bool]",new EFSign("c32/extern/Runtime","println",    "(Z)V"));
		table.put("std.println[byte]",new EFSign("c32/extern/Runtime","println",    "(B)V"));
		table.put("std.println[ubyte]",new EFSign("c32/extern/Runtime","uprintln",  "(B)V"));
		table.put("std.println[short]",new EFSign("c32/extern/Runtime","println",   "(S)V"));
		table.put("std.println[ushort]",new EFSign("c32/extern/Runtime","uprintln", "(S)V"));
		table.put("std.println[int]",new EFSign("c32/extern/Runtime","println",     "(I)V"));
		table.put("std.println[uint]",new EFSign("c32/extern/Runtime","uprintln",   "(I)V"));
		table.put("std.println[long]",new EFSign("c32/extern/Runtime","println",    "(J)V"));
		table.put("std.println[ulong]",new EFSign("c32/extern/Runtime","uprintln",  "(J)V"));

		table.put("std.malloc[ulong]",new EFSign("c32/extern/Memory","malloc",      "(J)J"));
	}
	public static void loadExternCall(FunctionWriter wr, FunctionInfo func, CallExpression expr) {
		int i = 0;
		for (Expression arg : expr.getArgs()) {
			wr.loadExpression(arg, expr.getFunction().getArgs().get(i++).getTypeRef().getType());
		}
		String sign = func.getFullNameEx();
		EFSign target = table.get(sign);
		if (target == null) {
			throw new LinkerException(expr.getLocation(),"extern function implementation not found: " + sign);
		}
		wr.getMv().visitMethodInsn(INVOKESTATIC, target.getOwner(), target.getName(), target.getDescriptor(),
				false);
	}
}
