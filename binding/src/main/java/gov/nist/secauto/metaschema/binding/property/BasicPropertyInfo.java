package gov.nist.secauto.metaschema.binding.property;

import java.lang.reflect.Type;

public class BasicPropertyInfo extends AbstractPropertyInfo<Type, PropertyAccessor> {

	public BasicPropertyInfo(Type type, PropertyAccessor propertyAccessor) {
		super(type, propertyAccessor);
	}

	@Override
	public PropertyCollector newPropertyCollector() {
		return new SingletonPropertyCollector<PropertyInfo>(this);
	}
}
