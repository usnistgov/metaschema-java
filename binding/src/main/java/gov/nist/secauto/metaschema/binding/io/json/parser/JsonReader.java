package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.util.List;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public interface JsonReader<CLASS> {
	List<CLASS> readJson(JsonParsingContext parsingContext, PropertyBindingFilter filter, boolean parseRoot) throws BindingException;
}
