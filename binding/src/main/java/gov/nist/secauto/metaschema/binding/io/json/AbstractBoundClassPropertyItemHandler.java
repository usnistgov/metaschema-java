package gov.nist.secauto.metaschema.binding.io.json;

import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;

public abstract class AbstractBoundClassPropertyItemHandler<CLASS_BINDING extends ClassBinding<?>, PROPERTY_BINDING extends ModelItemPropertyBinding>
		extends AbstractProperrtyItemHandler<PROPERTY_BINDING> {

	private final CLASS_BINDING classBinding;

	public AbstractBoundClassPropertyItemHandler(CLASS_BINDING classBinding, PROPERTY_BINDING propertyBinding) {
		super(propertyBinding);
		this.classBinding = classBinding;
	}

	protected CLASS_BINDING getClassBinding() {
		return classBinding;
	}

}
