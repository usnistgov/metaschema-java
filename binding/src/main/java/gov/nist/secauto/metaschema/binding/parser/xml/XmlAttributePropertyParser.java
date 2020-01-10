package gov.nist.secauto.metaschema.binding.parser.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;

import gov.nist.secauto.metaschema.binding.parser.BindingException;

public interface XmlAttributePropertyParser extends XmlPropertyParser {
	QName getHandledQName();
	<CLASS> void parse(CLASS obj, XmlParsingContext parser, Attribute attribue) throws BindingException;
}
