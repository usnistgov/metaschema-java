package gov.nist.secauto.metaschema.binding.io.json.parser;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public interface JsonReader<CLASS> {
	CLASS readJson(JsonParsingContext parsingContext, PropertyBindingFilter filter, boolean parseRoot) throws BindingException;
}
