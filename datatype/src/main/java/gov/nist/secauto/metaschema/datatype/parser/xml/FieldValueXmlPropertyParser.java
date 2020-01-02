package gov.nist.secauto.metaschema.datatype.parser.xml;

import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public interface FieldValueXmlPropertyParser extends XmlPropertyParser {
	<CLASS> void parse(CLASS obj, XMLEventReader2 reader) throws BindingException;

}
