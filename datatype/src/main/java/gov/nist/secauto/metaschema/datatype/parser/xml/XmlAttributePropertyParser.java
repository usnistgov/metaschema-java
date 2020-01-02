package gov.nist.secauto.metaschema.datatype.parser.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public interface XmlAttributePropertyParser extends XmlPropertyParser {
	QName getHandledQName();
	<CLASS> void parse(CLASS obj, Attribute attribue) throws BindingException;
}
