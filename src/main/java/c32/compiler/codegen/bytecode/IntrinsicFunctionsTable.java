package c32.compiler.codegen.bytecode;

import c32.compiler.codegen.LinkerException;
import c32.compiler.logical.tree.FunctionInfo;
import c32.compiler.logical.tree.expression.CallExpression;
import c32.compiler.logical.tree.expression.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class IntrinsicFunctionsTable {
	@RequiredArgsConstructor
	@Getter
	private static final class EFSign { //Extern Function Signature
		private final String owner;
		private final String name;
		private final String descriptor;
	}
	private static final HashMap<String, EFSign> table = new HashMap<>();

	static {
		table.put("std.println[]",new EFSign("c32/extern/Runtime","println",    "()V"));

		table.put("std.println[bool]",new EFSign("c32/extern/Runtime","println",    "(Z)V"));
		table.put("std.print[bool]",new EFSign("c32/extern/Runtime","print",    "(Z)V"));
		table.put("std.println[byte]",new EFSign("c32/extern/Runtime","println",    "(B)V"));
		table.put("std.print[byte]",new EFSign("c32/extern/Runtime","print",    "(B)V"));
		table.put("std.println[ubyte]",new EFSign("c32/extern/Runtime","uprintln",  "(B)V"));
		table.put("std.print[ubyte]",new EFSign("c32/extern/Runtime","uprint",  "(B)V"));
		table.put("std.println[short]",new EFSign("c32/extern/Runtime","println",   "(S)V"));
		table.put("std.print[short]",new EFSign("c32/extern/Runtime","print",   "(S)V"));
		table.put("std.println[ushort]",new EFSign("c32/extern/Runtime","uprintln", "(S)V"));
		table.put("std.print[ushort]",new EFSign("c32/extern/Runtime","uprint", "(S)V"));
		table.put("std.println[int]",new EFSign("c32/extern/Runtime","println",     "(I)V"));
		table.put("std.print[int]",new EFSign("c32/extern/Runtime","print",     "(I)V"));
		table.put("std.println[uint]",new EFSign("c32/extern/Runtime","uprintln",   "(I)V"));
		table.put("std.print[uint]",new EFSign("c32/extern/Runtime","uprint",   "(I)V"));
		table.put("std.println[long]",new EFSign("c32/extern/Runtime","println",    "(J)V"));
		table.put("std.print[long]",new EFSign("c32/extern/Runtime","print",    "(J)V"));
		table.put("std.println[ulong]",new EFSign("c32/extern/Runtime","uprintln",  "(J)V"));
		table.put("std.print[ulong]",new EFSign("c32/extern/Runtime","uprint",  "(J)V"));
		table.put("std.println[void*]",new EFSign("c32/extern/Runtime","pprintln",  "(J)V"));
		table.put("std.print[void*]",new EFSign("c32/extern/Runtime","pprint",      "(J)V"));

		table.put("std.println[char]",new EFSign("c32/extern/Runtime","println",  "(C)V"));
		table.put("std.print[char]",new EFSign("c32/extern/Runtime","print",      "(C)V"));
		table.put("std.println[char8]",new EFSign("c32/extern/Runtime","println8",  "(B)V"));
		table.put("std.print[char8]",new EFSign("c32/extern/Runtime","print8",      "(B)V"));
		table.put("std.println[char32]",new EFSign("c32/extern/Runtime","println32",  "(I)V"));
		table.put("std.print[char32]",new EFSign("c32/extern/Runtime","print32",      "(I)V"));


		table.put("std.malloc[ulong]",new EFSign("c32/extern/Memory","malloc",      "(J)J"));
		table.put("std.calloc[ulong, ulong]",new EFSign("c32/extern/Memory","calloc",      "(JJ)J"));
		table.put("std.realloc[void*, ulong]",new EFSign("c32/extern/Memory","realloc",      "(JJ)J"));
		table.put("std.free[void*]",new EFSign("c32/extern/Memory","free",      "(J)V"));
		table.put("std.memset[void*, ubyte, ulong]",new EFSign("c32/extern/Memory","memset","(JBJ)J"));

		table.put("std.Core.sleep[long]",new EFSign("java/lang/Thread","sleep",      "(J)V"));
		table.put("std.Core.nanoTime[]",new EFSign("java/lang/System","nanoTime",      "()J"));
		table.put("std.Core.currentTimeMillis[]",new EFSign("java/lang/System","currentTimeMillis",      "()J"));

		//temp
		table.put("test.randInt[int]",new EFSign("c32/extern/Runtime","randInt",      "(I)I"));
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
