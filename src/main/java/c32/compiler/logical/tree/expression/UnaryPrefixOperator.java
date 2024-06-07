package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.logical.IllegalOperatorException;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypePointerInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UnaryPrefixOperator {
	private final String op;
	private final TypeRefInfo targetType;
	private final TypeInfo returnType;

	private static final Map<TypeInfo, Set<UnaryPrefixOperator>> registeredOperators = new HashMap<>();
	static {
		registerUnaryPrefix("!",new TypeRefInfo(false, false,false, TypeInfo.PrimitiveTypeInfo.BOOL),TypeInfo.PrimitiveTypeInfo.BOOL);
		TypeInfo.PrimitiveTypeInfo.forEachNumeric(TYPE -> {
			registerUnaryPrefix("++",new TypeRefInfo(true,false,false, TYPE), TYPE);
			registerUnaryPrefix("--",new TypeRefInfo(true,false,false, TYPE), TYPE);
			if (TYPE instanceof TypeInfo.UnsignedIntegerPrimitiveTypeInfo); else
				registerUnaryPrefix("-", new TypeRefInfo(false,false, false, TYPE), TYPE);
		});
	}

	private static UnaryPrefixOperator registerUnaryPrefix(String op, TypeRefInfo targetType, TypeInfo returnType) {
		UnaryPrefixOperator operator = new UnaryPrefixOperator(op,targetType,returnType);
		Set<UnaryPrefixOperator> operators = registeredOperators.computeIfAbsent(targetType.getType(), k -> new HashSet<>());
		operators.add(operator);
		return operator;
	}

	public static UnaryPrefixOperator findOperator(Location location, TypeRefInfo targetType, String op) {
		Set<UnaryPrefixOperator> operators = registeredOperators.computeIfAbsent(targetType.getType(), k -> new HashSet<>());
		for (UnaryPrefixOperator registeredOperator : operators) {
			if (!registeredOperator.op.equals(op)) continue;
			if (!targetType.canBeImplicitlyCastTo(registeredOperator.targetType)) break;
			return registeredOperator;
		}
		switch (op) {
			case "*": if (targetType.getType() instanceof TypePointerInfo && ((TypePointerInfo) targetType.getType()).getTargetType().getType().sizeof() != 0) {
				return registerUnaryPrefix(op,targetType,((TypePointerInfo) targetType.getType()).getTargetType().getType());
			} break;
			case "&": {
				return registerUnaryPrefix(op,targetType,TypePointerInfo.pointerOf(targetType));
			} //break;
		}

		throw new IllegalOperatorException(location,op,targetType.getType());
	}
}
