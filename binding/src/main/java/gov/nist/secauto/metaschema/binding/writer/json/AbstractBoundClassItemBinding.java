package gov.nist.secauto.metaschema.binding.writer.json;

import gov.nist.secauto.metaschema.binding.ClassBinding;
import gov.nist.secauto.metaschema.binding.property.ModelItemPropertyBinding;

public abstract class AbstractBoundClassItemBinding<CLASS_BINDING extends ClassBinding<?>, PROPERTY_BINDING extends ModelItemPropertyBinding>
		extends AbstractItemBinding<PROPERTY_BINDING> implements BoundClassItemBinding<CLASS_BINDING, PROPERTY_BINDING> {
	private final CLASS_BINDING classBinding;

	public AbstractBoundClassItemBinding(CLASS_BINDING classBinding, PROPERTY_BINDING propertyBinding) {
		super(propertyBinding);
		this.classBinding = classBinding;
	}

	@Override
	public CLASS_BINDING getClassBinding() {
		return classBinding;
	}
}
