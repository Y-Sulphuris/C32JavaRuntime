package c32.compiler.logical.tree.expression;

import c32.compiler.Location;
import c32.compiler.logical.tree.TypeInfo;
import c32.compiler.logical.tree.TypePointerInfo;
import c32.compiler.logical.tree.TypeRefInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NullLiteralExpression implements LiteralExpression {
	private final Location location;

	@Override
	public TypeInfo getReturnType() {
		return TypePointerInfo.pointerOf(new TypeRefInfo(false,false,false, TypeInfo.PrimitiveTypeInfo.VOID));
	}
}
