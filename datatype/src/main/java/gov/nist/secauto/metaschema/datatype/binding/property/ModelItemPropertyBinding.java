package gov.nist.secauto.metaschema.datatype.binding.property;

import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlObjectPropertyParser;

public interface ModelItemPropertyBinding extends NamedPropertyBinding {
	@Override
	CollectionPropertyInfo getPropertyInfo();
	PropertyCollector newPropertyCollector();
	@Override
	XmlObjectPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException;
}
