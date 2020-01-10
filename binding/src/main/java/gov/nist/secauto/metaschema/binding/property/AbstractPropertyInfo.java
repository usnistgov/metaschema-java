package gov.nist.secauto.metaschema.binding.property;

import java.lang.reflect.Type;
import java.util.Objects;

public abstract class AbstractPropertyInfo<TYPE extends Type, ACCESSOR extends PropertyAccessor> implements PropertyInfo {
	private final TYPE type;
	private final ACCESSOR propertyAccessor;

	public AbstractPropertyInfo(TYPE type, ACCESSOR propertyAccessor) {
		Objects.requireNonNull(type, "type");
		Objects.requireNonNull(propertyAccessor, "propertyAccessor");

		this.type = type;
		this.propertyAccessor = propertyAccessor;
	}

	@Override
	public TYPE getType() {
		return type;
	}

	@Override
	public Class<?> getRawType() {
		return (Class<?>)getType();
	}

	@Override
	public Class<?> getItemType() {
		return (Class<?>)getType();
	}

	@Override
	public ACCESSOR getPropertyAccessor() {
		return propertyAccessor;
	}

	@Override
	public String getSimpleName() {
		return getPropertyAccessor().getSimpleName();
	}

}
