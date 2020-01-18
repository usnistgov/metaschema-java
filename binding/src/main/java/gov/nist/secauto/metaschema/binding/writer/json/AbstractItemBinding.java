package gov.nist.secauto.metaschema.binding.writer.json;

import gov.nist.secauto.metaschema.binding.property.ModelItemPropertyBinding;

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
