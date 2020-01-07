package gov.nist.secauto.metaschema.datatype.binding.property;

import java.lang.reflect.Type;
import java.util.Objects;

public class BasicPropertyInfo<TYPE extends Type> implements PropertyInfo {
	private final TYPE type;
	private final PropertyAccessor propertyAccessor;

	public BasicPropertyInfo(TYPE type, PropertyAccessor propertyAccessor) {
		Objects.requireNonNull(type, "type");
		Objects.requireNonNull(propertyAccessor, "propertyAccessor");
		this.type = type;
		this.propertyAccessor = propertyAccessor;
	}

	@Override
	public PropertyAccessor getPropertyAccessor() {
		return propertyAccessor;
	}

	@Override
	public TYPE getType() {
		return type;
	}

	@Override
	public Type getRawType() {
		return getType();
	}

	@Override
	public Type getItemType() {
		return getType();
	}

	@Override
	public String getSimpleName() {
		return getPropertyAccessor().getSimpleName();
	}


}
