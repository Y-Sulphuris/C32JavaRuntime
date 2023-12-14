package c32.compiler.ast.statement;

import c32.compiler.ast.*;
import c32.compiler.ast.expr.ExprTree;
import c32.compiler.ast.expr.InitializerListExprTree;
import c32.compiler.ast.type.StructTypeTree;
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

public class VariableDeclarationStatementTree implements StatementTree {
	@Getter
	private final TypeTree type;
	@Getter
	private final String varName;
	@Getter
	private final ExprTree init;

	@Getter
	private final Modifiers modifiers;


	public VariableDeclarationStatementTree(TypeTree type, String varName, Modifiers modifiers) {
		this(type,varName,null,modifiers);
	}
	public VariableDeclarationStatementTree(TypeTree type, String varName, ExprTree init, Modifiers modifiers) {
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
			for (VariableDeclarationStatementTree field : struct.getFields()) {
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
		if (init != null) {
			field.initializer(init.brewJava());
		}
		return Collections.singleton(field.build());
	}

	private Collection<CodeBlock> brewJava(String varName, ExprTree init) {
		if (type instanceof StructTypeTree) {
			StructTypeTree struct = (StructTypeTree) type;
			if (init instanceof InitializerListExprTree) {
				InitializerListExprTree structInit = (InitializerListExprTree) init;
				List<VariableDeclarationStatementTree> fields = struct.getFields();
				List<CodeBlock> code = new ArrayList<>();
				for (int i = 0; i < fields.size(); i++) {
					VariableDeclarationStatementTree field = fields.get(i);
					ExprTree fieldInit = structInit.getInits().get(i);
					code.addAll(field.brewJava("_" + varName + "_" + field.varName, fieldInit));
				}
				return code;
			} else if ((init instanceof VariableExprTree || init instanceof SyntheticVariableExprTree) && init.getRetType().equals(this.getType())) {
				List<VariableDeclarationStatementTree> fields = struct.getFields();
				List<CodeBlock> code = new ArrayList<>();
				for (int i = 0; i < fields.size(); i++) {
					VariableDeclarationStatementTree field = fields.get(i);
					ExprTree fieldInit = new SyntheticVariableExprTree(field.type, "_"+init.brewJava()+"_" + field.varName);
					code.addAll(field.brewJava("_" + varName + "_" + field.varName, fieldInit));
				}
				return code;
			}
		}
		CodeBlock.Builder code = CodeBlock.builder();
		String strInit = "";
		if (init != null) {
			strInit = " = " + init.brewJava();
		}
		code.add((modifiers.is_const() ? "final " : "") + type.getJavaTypeName() + " " + varName + strInit);
		return Collections.singleton(code.build());
	}
	@Override
	public Collection<CodeBlock> brewJava() {
		return brewJava(varName,init);
	}

	private static final class SyntheticVariableExprTree extends ExprTree {
		private final String name;
		public SyntheticVariableExprTree(TypeTree retType, String name) {
			super(retType);
			this.name = name;
		}

		@Override
		public String brewJava() {
			return name;
		}
	}
}
