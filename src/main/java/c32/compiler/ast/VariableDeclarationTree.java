package c32.compiler.ast;

import c32.compiler.ast.expr.ExprTree;
import c32.compiler.ast.expr.InitializerListExprTree;
import c32.compiler.ast.type.TypeTree;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import lombok.Getter;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class VariableDeclarationTree implements StatementTree {
	@Getter
	private final TypeTree type;
	@Getter
	private final String varName;
	@Getter
	private final ExprTree init;

	@Getter
	private final Modifiers modifiers;


	public VariableDeclarationTree(TypeTree type, String varName, Modifiers modifiers) {
		this(type,varName,null,modifiers);
	}
	public VariableDeclarationTree(TypeTree type, String varName, ExprTree init, Modifiers modifiers) {
		if (type == null) throw new NullPointerException();
		if (varName == null) throw new NullPointerException();
		this.type = type;
		this.varName = varName;
		this.init = init;
		this.modifiers = modifiers;
	}

	public Collection<ParameterSpec> brewJavaAsArgument() {
		return brewJavaAsArgument0(varName);
	}
	private Collection<ParameterSpec> brewJavaAsArgument0(String varName) {
		if (type instanceof StructTypeTree) {
			StructTypeTree struct = (StructTypeTree) type;
			Collection<ParameterSpec> args = new ArrayList<>();
			for (VariableDeclarationTree field : struct.getFields()) {
				args.addAll(field.brewJavaAsArgument0("_" + varName + "_" + field.varName));
			}
			return args;
		}
		ParameterSpec.Builder argument = ParameterSpec.builder(type.getJavaType(),varName);
		if (modifiers.is_const()) argument.addModifiers(Modifier.FINAL);
		return Collections.singleton(argument.build());
	}

	public Collection<FieldSpec> brewJavaAsField(VariableContainer container) {
		FieldSpec.Builder field = FieldSpec.builder(type.getJavaType(), varName, Modifier.PUBLIC);
		if (container instanceof CompilationUnitTree) field.addModifiers(Modifier.STATIC);
		if (modifiers.is_const()) field.addModifiers(Modifier.FINAL);
		return Collections.singleton(field.build());
	}

	@Override
	public Collection<CodeBlock> brewJava() {
		if (type instanceof StructTypeTree && init instanceof InitializerListExprTree) {
			StructTypeTree struct = (StructTypeTree) type;
			InitializerListExprTree structInit = (InitializerListExprTree) init;
			List<VariableDeclarationTree> fields = struct.getFields();
			List<CodeBlock> code = new ArrayList<>();
			for (int i = 0; i < fields.size(); i++) {
				VariableDeclarationTree field = fields.get(i);
				ExprTree fieldInit = structInit.getInits().get(i);
				CodeBlock.Builder codeField = CodeBlock.builder();
				String strInit = "";
				if (fieldInit != null) {
					strInit = " = " + fieldInit.brewJava();
				} else if (field.init != null) {
					strInit = " = " + field.init.brewJava();
				}
				codeField.add((field.modifiers.is_const() ? "final " : "") + "$L _$L_$L" + strInit,field.type,varName,field.varName);
				code.add(codeField.build());
			}
			return code;
		}
		CodeBlock.Builder code = CodeBlock.builder();
		String strInit = "";
		if (init != null) {
			strInit = " = " + init.brewJava();
		}
		code.add((modifiers.is_const() ? "final " : "") + type.getJavaTypeName() + " " + varName + strInit);
		return Collections.singleton(code.build());
	}
}
