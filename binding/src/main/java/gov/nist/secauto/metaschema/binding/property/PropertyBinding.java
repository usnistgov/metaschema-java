package gov.nist.secauto.metaschema.binding.property;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlPropertyParser;

public interface PropertyBinding {

	PropertyInfo getPropertyInfo();
	XmlPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException;
}
