package c32.compiler.ast;

import c32.compiler.Compiler;
import c32.compiler.FunctionDeclNotFoundException;
import c32.compiler.ast.expr.ExprTree;
import c32.compiler.ast.type.TypeTree;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.*;

public class CompilationUnitTree implements Tree, VariableContainer, FunctionContainer {
	final String fileName;
	String packageName = "";


	private final List<FunctionDeclarationTree> functions = new ArrayList<>();
	public FunctionDeclarationTree addFunction(FunctionDeclarationTree function) {
		functions.add(function);
		return function;
	}


	private final HashMap<String, TypeTree> typenames = new HashMap<>();
	public void addTypename(String name, TypeTree forType) {
		typenames.put(name,forType);
	}


	private final List<VariableDeclarationTree> fields = new ArrayList<>();


	private final List<StructTypeTree> structs = new ArrayList<>();
	public void addStruct(StructTypeTree structTree) {
		structs.add(structTree);
	}


	public CompilationUnitTree(String fileName) {
		this.fileName = fileName.replace(".","_");
	}

	public Collection<JavaFile> brewJava() {
		TypeSpec.Builder unitFile = TypeSpec.classBuilder(fileName);
		unitFile.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		unitFile.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());
		for (FunctionDeclarationTree function : functions) {
			MethodSpec brewJava = function.brewJava();
			if (brewJava != null) unitFile.addMethod(brewJava);
		}
		for (VariableDeclarationTree field : fields) {
			for (FieldSpec fieldSpec : field.brewJavaAsField(this)) unitFile.addField(fieldSpec);
		}
		return Collections.singleton(JavaFile.builder(packageName,unitFile.build()).build());
	}

	public TypeTree getType(String typename) {
		if (typenames.containsKey(typename)) {
			return typenames.get(typename);
		}
		for (StructTypeTree struct : structs) {
			if(struct.getName().equals(typename)) return struct;
		}
		return TypeTree.overriddenTypes.get(typename);
	}

	@Override
	public FunctionDeclarationTree getFunction(String name, ArrayList<ExprTree> args) {
		FunctionDeclarationTree f = null;
		Ff:
		for (FunctionDeclarationTree function : functions) {
			if (args.size() != function.getArgs().length) continue;
			if (!function.getFunctionName().equals(name)) continue;
			for (int i = 0; i < args.size(); i++) {
				if (args.get(i).getRetType() != function.getArgs()[i].getType()) continue Ff;
			}
			if (f == null)
				f = function;
			else
				throw new AmbiguousFunctionCallException(name, args);
		}
		if (f != null) return f;
		//искать уже не точные совпадения по типам аргументов, а чтобы хотя бы можно было неявно привести к ним
		FfIm:
		for (FunctionDeclarationTree function : functions) {
			if (args.size() != function.getArgs().length) continue;
			if (!function.getFunctionName().equals(name)) continue;
			for (int i = 0; i < args.size(); i++) {
				if (!args.get(i).canBeImplicitCastTo(function.getArgs()[i].getType())) continue FfIm;
			}
			if (f == null)
				f = function;
			else
				throw new AmbiguousFunctionCallException(name, args);
		}
		if (f != null) return f;
		throw new FunctionDeclNotFoundException(name, args);
	}

	@Override
	public Set<String> availableFunctionModifiers() {
		return availableFunctionMod;
	}


	private static final Set<String> availableFunctionMod = new HashSet<>();
	static {
		availableFunctionMod.add(Compiler.EXTERN);
		availableFunctionMod.add(Compiler.NATIVE);
	}

	@Override
	public Set<String> availableVariableModifiers() {
		return availableVariableMod;
	}
	private static final Set<String> availableVariableMod = new HashSet<>();
	static {
		availableVariableMod.add(Compiler.CONST);
	}

	@Override
	public VariableDeclarationTree getVariable(String name) {
		for (VariableDeclarationTree field : fields) {
			if (field.getVarName().equals(name)) return field;
		}
		throw new VariableDeclNotFoundException(this,name);
	}

	public void addField(VariableDeclarationTree var) {
		fields.add(var);
	}

}
