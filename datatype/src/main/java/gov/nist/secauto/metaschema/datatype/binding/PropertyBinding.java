package gov.nist.secauto.metaschema.datatype.binding;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParser;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlPropertyParser;

public interface PropertyBinding {

	PropertyInfo getPropertyInfo();
	XmlPropertyParser newXmlPropertyParser(XmlParser parser) throws BindingException;
}
