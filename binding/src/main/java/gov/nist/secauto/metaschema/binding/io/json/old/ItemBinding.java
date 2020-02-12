package gov.nist.secauto.metaschema.binding.io.json.old;

import java.io.IOException;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public interface ItemBinding<PROPERTY_BINDING extends ModelItemPropertyBinding> {
	PROPERTY_BINDING getPropertyBinding();
	void writeValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext) throws BindingException, IOException;
}
