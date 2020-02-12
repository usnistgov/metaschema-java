package gov.nist.secauto.metaschema.binding.io.json.parser;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

public interface JsonReader<CLASS> {
	CLASS readJson(JsonParsingContext parsingContext, NamedPropertyBindingFilter filter, boolean parseRoot) throws BindingException;
}
