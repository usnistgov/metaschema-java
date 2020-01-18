package gov.nist.secauto.metaschema.binding.writer.json;

import java.io.IOException;

import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.property.ModelItemPropertyBinding;

public interface ItemBinding<PROPERTY_BINDING extends ModelItemPropertyBinding> {
	PROPERTY_BINDING getPropertyBinding();
	void writeValue(Object value, FlagPropertyBindingFilter filter, JsonWritingContext writingContext) throws BindingException, IOException;

}
