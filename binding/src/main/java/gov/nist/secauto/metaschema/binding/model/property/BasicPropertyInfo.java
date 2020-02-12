package gov.nist.secauto.metaschema.binding.model.property;

import java.lang.reflect.Type;

public class BasicPropertyInfo extends AbstractPropertyInfo<Type> {

	public BasicPropertyInfo(Type type, PropertyAccessor propertyAccessor) {
		super(type, propertyAccessor);
	}

	@Override
	public SingletonPropertyCollector<BasicPropertyInfo> newPropertyCollector() {
		return new SingletonPropertyCollector<BasicPropertyInfo>(this);
	}
}
