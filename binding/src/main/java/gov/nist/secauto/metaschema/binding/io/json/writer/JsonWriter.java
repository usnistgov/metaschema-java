package gov.nist.secauto.metaschema.binding.io.json.writer;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

public interface JsonWriter<CLASS> {
	void writeJson(CLASS obj, NamedPropertyBindingFilter filter, JsonWritingContext writingContext) throws BindingException;
}
