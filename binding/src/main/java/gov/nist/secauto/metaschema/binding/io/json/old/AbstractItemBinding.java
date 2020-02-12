package gov.nist.secauto.metaschema.binding.io.json.old;

import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;

public abstract class AbstractItemBinding<PROPERTY_BINDING extends ModelItemPropertyBinding> implements ItemBinding<PROPERTY_BINDING> {
	private final PROPERTY_BINDING propertyBinding;

	public AbstractItemBinding(PROPERTY_BINDING propertyBinding) {
		this.propertyBinding = propertyBinding;
	}

	@Override
	public PROPERTY_BINDING getPropertyBinding() {
		return propertyBinding;
	}
}
