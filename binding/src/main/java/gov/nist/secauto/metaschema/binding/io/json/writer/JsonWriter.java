package gov.nist.secauto.metaschema.binding.io.json.writer;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public interface JsonWriter<CLASS> {
	void writeJson(CLASS obj, PropertyBindingFilter filter, JsonWritingContext writingContext) throws BindingException;
}
