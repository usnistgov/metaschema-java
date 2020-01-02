package gov.nist.secauto.metaschema.datatype.binding;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.PropertyCollector;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlObjectPropertyParser;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParser;

public interface ModelItemPropertyBinding extends NamedPropertyBinding {
	@Override
	CollectionPropertyInfo getPropertyInfo();
	PropertyCollector newPropertyCollector();
	@Override
	XmlObjectPropertyParser newXmlPropertyParser(XmlParser parser) throws BindingException;
}
