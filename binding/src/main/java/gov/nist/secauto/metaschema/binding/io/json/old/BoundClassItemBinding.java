package gov.nist.secauto.metaschema.binding.io.json.old;

import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;

public interface BoundClassItemBinding<CLASS_BINDING extends ClassBinding<?>, PROPERTY_BINDING extends ModelItemPropertyBinding> extends ItemBinding<PROPERTY_BINDING> {
	CLASS_BINDING getClassBinding();
}
