package gov.nist.secauto.metaschema.binding.writer.json;

import gov.nist.secauto.metaschema.binding.ClassBinding;
import gov.nist.secauto.metaschema.binding.property.ModelItemPropertyBinding;

public interface BoundClassItemBinding<CLASS_BINDING extends ClassBinding<?>, PROPERTY_BINDING extends ModelItemPropertyBinding> extends ItemBinding<PROPERTY_BINDING> {
	CLASS_BINDING getClassBinding();
}
