package c32.compiler.codegen.bytecode;

import c32.compiler.logical.tree.*;
import c32.compiler.logical.tree.expression.*;
import c32.compiler.logical.tree.statement.*;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static c32.compiler.codegen.bytecode.ASMUtils.*;
import static org.objectweb.asm.Opcodes.*;

public class JVMGenerator implements c32.compiler.codegen.Generator {

	private static final int[] classVersion = new int[22+1]; static {
		classVersion[ 0] = 45;
		classVersion[ 1] = 45;
		classVersion[ 2] = 46;
		classVersion[ 3] = 47;
		classVersion[ 4] = 48;
		classVersion[ 5] = 49;
		classVersion[ 6] = 50;
		classVersion[ 7] = 51;
		classVersion[ 8] = 52;
		classVersion[ 9] = 53;
		classVersion[10] = 54;
		classVersion[11] = 55;
		classVersion[12] = 56;
		classVersion[13] = 57;
		classVersion[14] = 58;
		classVersion[15] = 59;
		classVersion[16] = 60;
		classVersion[17] = 61;
		classVersion[18] = 62;
		classVersion[19] = 63;
		classVersion[20] = 64;
		classVersion[21] = 65;
		classVersion[22] = 66;
	}

	public static int javaVersion(int i) {
		return classVersion[i];
	}

	private final int version = 8;
	private int classVersion() {
		return javaVersion(version);
	}


	@Override
	public void generate(NamespaceInfo space) {
		try {
			//JarOutputStream jar = new JarOutputStream(Files.newOutputStream(new File("test.jar").toPath()));

			/*ClassWriter cw = new ClassWriter(0);
			cw.visit(classVersion(),ACC_PUBLIC + ACC_SUPER,"test/package",null,"java/lang/Object",null);
			cw.visitSource("file",null);

			{
				MethodVisitor method = cw.visitMethod(ACC_PUBLIC | ACC_STATIC,"main","([Ljava/lang/String;)V",null,null);
				method.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");
				method.visitLdcInsn("hello");
				method.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V",false);
				method.visitInsn(RETURN);
				method.visitMaxs(2,1);
				method.visitEnd();
			}
			cw.visitEnd();*/

			Map<SpaceInfo, ClassWriter> writers = writeAll(space);
			for (Map.Entry<SpaceInfo, ClassWriter> entry : writers.entrySet()) {
				String clName = asClassName(entry.getKey());
				File dir = new File("out/jvm/" + (clName.contains("/") ? clName.substring(0,clName.lastIndexOf('/')) : ""));
				dir.mkdirs();

				File clFile = new File("out/jvm/"+clName + ".class");
				//System.out.println("writing " + clFile.getPath() + "...");
				clFile.createNewFile();

				OutputStream stream = Files.newOutputStream(clFile.toPath());
				stream.write(entry.getValue().toByteArray());
				stream.close();
			}


			//jar.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private HashMap<SpaceInfo, ClassWriter> writeAll(NamespaceInfo namespace) {
		HashMap<SpaceInfo, ClassWriter> writers = new HashMap<>();
		ClassWriter namespaceCv = writeNamespaceItself(namespace);
		writers.put(namespace, namespaceCv);
		for (NamespaceInfo namespaceNamespace : namespace.getNamespaces()) {
			writers.putAll(writeAll(namespaceNamespace));
		}

		return writers;
	}

	private void writeField(ClassWriter cv, FieldInfo field) {
		int mod = ACC_PUBLIC | ACC_STATIC;
		if (field.getVariable().getTypeRef().is_const()) mod |= ACC_FINAL;
		FieldVisitor fv = cv.visitField(mod,field.getName(),asDescriptor(field.getTypeRef().getType()),null,asFieldInitializerValue(field));
		fv.visitEnd();
	}

	private Object asFieldInitializerValue(FieldInfo fieldInfo) {
		/*Expression initializer = fieldInfo.getInitializer();
		if (initializer instanceof NumericLiteralExpression) {
			Number num = ((NumericLiteralExpression) initializer).getNumber();
			if (fieldInfo.getTypeRef().getType() == BYTE)
		}*/
		return null;//todo;
	}


	private void writeFunction(ClassWriter cv, FunctionInfo function) {
		if (function.is_extern()) return;
		int mod = ACC_PUBLIC | ACC_STATIC;
		if (function.is_native()) mod |= ACC_NATIVE;
		MethodVisitor mv = cv.visitMethod(mod, asFunctionName(function),asJavaFunctionDescriptor(function),null,null);

		if (function instanceof FunctionImplementationInfo) {
			FunctionImplementationInfo func = (FunctionImplementationInfo) function;
			new FunctionWriter(func, mv).write();
		}

		mv.visitEnd();
	}




	private ClassWriter writeNamespaceItself(NamespaceInfo namespace) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		cw.visit(classVersion(),
			ACC_PUBLIC | ACC_SUPER | ACC_FINAL,
			asClassName(namespace),
			null,
			"c32/extern/NamespaceSymbol",
			null);
		cw.visitSource(namespace.getName(),namespace.getName());

		for (FieldInfo field : namespace.getFields()) {
			writeField(cw,field);
		}
		for (FunctionInfo function : namespace.getFunctions()) {
			writeFunction(cw, function);
		}
		if (namespace.getParent() == null) {
			ASMUtils.generateMainFunction(cw);
		}

		cw.visitEnd();
		return cw;
	}


}
