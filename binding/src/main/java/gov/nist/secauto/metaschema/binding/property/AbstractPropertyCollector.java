package gov.nist.secauto.metaschema.binding.property;

import gov.nist.secauto.metaschema.binding.parser.BindingException;

public abstract class AbstractPropertyCollector<INFO extends PropertyInfo> implements PropertyCollector {
	private final INFO propertyInfo;

	protected AbstractPropertyCollector(INFO propertyInfo) {
		this.propertyInfo = propertyInfo;
	}

	public INFO getPropertyInfo() {
		return propertyInfo;
	}

	protected abstract Object getCollection();

	@Override
	public void applyCollection(Object obj) throws BindingException {
		getPropertyInfo().setValue(obj, getCollection());
	}
}
