package gov.nist.secauto.metaschema.binding.io.xml.parser;

import gov.nist.secauto.metaschema.binding.BindingException;

public interface FieldValueXmlPropertyParser extends XmlPropertyParser {
	void parse(Object obj, XmlParsingContext parser) throws BindingException;

}
