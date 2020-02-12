package gov.nist.secauto.metaschema.binding.model.property;

public abstract class AbstractPropertyBinding implements PropertyBinding {
	private final PropertyInfo propertyInfo;

	public AbstractPropertyBinding(PropertyInfo propertyInfo) {
		this.propertyInfo = propertyInfo;
	}

	@Override
	public PropertyInfo getPropertyInfo() {
		return propertyInfo;
	}
}
