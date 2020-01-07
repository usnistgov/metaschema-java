package gov.nist.secauto.metaschema.datatype.parser.xml;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public interface FieldValueXmlPropertyParser extends XmlPropertyParser {
	<CLASS> void parse(CLASS obj, XmlParsingContext parser) throws BindingException;

}
