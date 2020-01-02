package gov.nist.secauto.metaschema.datatype.binding;

public abstract class AbstractPropertyBinding<INFO extends PropertyInfo> implements PropertyBinding {
	private final INFO propertyInfo;

	public AbstractPropertyBinding(INFO propertyInfo) {
		this.propertyInfo = propertyInfo;
	}
	@Override
	public INFO getPropertyInfo() {
		return propertyInfo;
	}
}
