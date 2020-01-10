package gov.nist.secauto.metaschema.binding.parser.xml;

import gov.nist.secauto.metaschema.binding.parser.BindingException;

public interface FieldValueXmlPropertyParser extends XmlPropertyParser {
	void parse(Object obj, XmlParsingContext parser) throws BindingException;

}
