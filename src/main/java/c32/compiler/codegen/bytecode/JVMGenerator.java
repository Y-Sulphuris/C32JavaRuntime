package c32.compiler.codegen.bytecode;

import c32.compiler.CompilerConfig;
import c32.compiler.logical.tree.*;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static c32.compiler.codegen.bytecode.ASMUtils.*;
import static org.objectweb.asm.Opcodes.*;

public class JVMGenerator implements c32.compiler.codegen.Generator {
	private final CompilerConfig config;

	public JVMGenerator(CompilerConfig config) {
		this.config = config;
	}

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
	public void generate(NamespaceInfo space, File outputDirectory) {
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

	private HashMap<SpaceInfo, ClassWriter> writeAll(SpaceInfo space) {
		HashMap<SpaceInfo, ClassWriter> writers = new HashMap<>();

		ClassWriter cw = null;
		if (space instanceof FunctionInfo) {
			boolean write =
					!space.getNamespaces().isEmpty() ||
							!space.getFields().isEmpty() ||
							!space.getImports().isEmpty() ||
							!space.getTypenames().isEmpty();
			if (write) cw = writeSpaceItself(space);
		} else {
			cw = writeSpaceItself(space);
		}
		if (cw != null) writers.put(space, cw);

		for (NamespaceInfo namespace : space.getNamespaces()) {
			writers.putAll(writeAll(namespace));
		}
		for (FunctionInfo function : space.getFunctions()) {
			if (function instanceof FunctionImplementationInfo)
				writers.putAll(writeAll((FunctionImplementationInfo)function));
		}

		return writers;
	}

	private final Collection<FieldInfo> fieldsToInit = new LinkedList<>();

	private void writeClinit(ClassWriter cw) {
		if (fieldsToInit.isEmpty()) return;
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "<clinit>", "()V", null, null);

		new FunctionWriter(mv).writeClinit(fieldsToInit);
		fieldsToInit.clear();
		mv.visitEnd();
	}

	private void writeField(ClassWriter cv, FieldInfo field) {
		int mod = ACC_PUBLIC | ACC_STATIC;
		if (!field.getTypeRef().is_mut()) mod |= ACC_FINAL;
		FieldVisitor fv = cv.visitField(mod,field.getName(),asDescriptor(field.getTypeRef().getType()),null,asFieldInitializerValue(field));
		fv.visitEnd();
		fieldsToInit.add(field);
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
		MethodVisitor mv = cv.visitMethod(mod, asFunctionName(function), asJavaFunctionDescriptor(function),null,null);

		if (function instanceof FunctionImplementationInfo) {
			FunctionImplementationInfo func = (FunctionImplementationInfo) function;
			if (func.getImplementation().getStatements().isEmpty()) {
				System.out.println("empty: " + func);
			}
			new FunctionWriter(func, mv).write();
		}

		mv.visitEnd();
	}




	private ClassWriter writeSpaceItself(SpaceInfo space) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | (version >= 7 ? ClassWriter.COMPUTE_FRAMES : 0));
		String super_name = "c32/extern/SpaceSymbol";
		if (space instanceof NamespaceInfo) {
			super_name = "c32/extern/NamespaceSymbol";
		} else if (space instanceof FunctionInfo) {
			super_name = "c32/extern/FunctionSymbol";
		}
		cw.visit(classVersion(),
			ACC_PUBLIC | ACC_SUPER | ACC_FINAL,
			asClassName(space),
			null,
			super_name,
			null);
		cw.visitSource(space.getName(),space.getName());

		for (FieldInfo field : space.getFields()) {
			writeField(cw,field);
		}
		for (FunctionInfo function : space.getFunctions()) {
			writeFunction(cw, function);
		}
		writeClinit(cw);
		if (space.getParent() == null) {
			ASMUtils.generateMainFunction(cw);
		}

		cw.visitEnd();
		return cw;
	}


}
