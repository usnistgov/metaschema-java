package gov.nist.secauto.metaschema.binding.property;

import java.lang.reflect.Type;
import java.util.Objects;

import gov.nist.secauto.metaschema.binding.parser.BindingException;

public abstract class AbstractPropertyInfo<TYPE extends Type> implements PropertyInfo {
	private final TYPE type;
	private final PropertyAccessor propertyAccessor;

	public AbstractPropertyInfo(TYPE type, PropertyAccessor propertyAccessor) {
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

	protected PropertyAccessor getPropertyAccessor() {
		return propertyAccessor;
	}

	@Override
	public String getSimpleName() {
		return getPropertyAccessor().getSimpleName();
	}

	@Override
	public Class<?> getContainingClass() {
		return getPropertyAccessor().getContainingClass();
	}

	@Override
	public String getPropertyName() {
		return getPropertyAccessor().getPropertyName();
	}

	@Override
	public void setValue(Object obj, Object value) throws BindingException {
		getPropertyAccessor().setValue(obj, value);
	}

	@Override
	public Object getValue(Object obj) throws BindingException {
		return getPropertyAccessor().getValue(obj);
	}

}
