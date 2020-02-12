package gov.nist.secauto.metaschema.binding.io.json;

import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

public abstract class AbstractProperrtyItemHandler<PROPERTY_BINDING extends PropertyBinding> implements PropertyItemHandler {
	private final PropertyBinding propertyBinding;

	public AbstractProperrtyItemHandler(PropertyBinding propertyBinding) {
		this.propertyBinding = propertyBinding;
	}

	@Override
	public PropertyBinding getPropertyBinding() {
		return propertyBinding;
	}
}
