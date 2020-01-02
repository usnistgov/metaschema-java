package gov.nist.secauto.metaschema.datatype.parser;

import gov.nist.secauto.metaschema.datatype.binding.CollectionPropertyInfo;

public abstract class AbstractPropertyCollector<INFO extends CollectionPropertyInfo> implements PropertyCollector {
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
		try {
			getPropertyInfo().getPropertyAccessor().setValue(obj, getCollection());
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new BindingException(ex);
		}
	}
}
