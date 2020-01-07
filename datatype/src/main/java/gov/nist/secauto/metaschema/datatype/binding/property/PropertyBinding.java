package gov.nist.secauto.metaschema.datatype.binding.property;

import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlPropertyParser;

public interface PropertyBinding {

	PropertyInfo getPropertyInfo();
	XmlPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException;
}
