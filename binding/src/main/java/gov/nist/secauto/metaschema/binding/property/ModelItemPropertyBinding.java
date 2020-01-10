package gov.nist.secauto.metaschema.binding.property;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlObjectPropertyParser;

public interface ModelItemPropertyBinding extends NamedPropertyBinding {
	@Override
	XmlObjectPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException;
}
