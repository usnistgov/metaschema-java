package gov.nist.secauto.metaschema.binding.property;

import gov.nist.secauto.metaschema.binding.parser.BindingException;

class SingletonPropertyCollector<INFO extends PropertyInfo> extends AbstractPropertyCollector<INFO> {

	private Object object;

	protected SingletonPropertyCollector(INFO propertyInfo) {
		super(propertyInfo);
	}

	@Override
	public void add(Object item) throws BindingException {
		if (object != null) {
			throw new IllegalStateException("A value has already been set for this singleton");
		}
		object = item;
	}

	@Override
	protected Object getCollection() {
		return object;
	}

}