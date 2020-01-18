package gov.nist.secauto.metaschema.binding.writer.json;

import gov.nist.secauto.metaschema.binding.parser.BindingException;

public interface JsonWriter {
	void writeJson(Object obj, FlagPropertyBindingFilter filter, JsonWritingContext writingContext) throws BindingException;
}
