package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlAttributePropertyParser;

public interface FlagPropertyBinding extends NamedPropertyBinding {
	@Override
	XmlAttributePropertyParser newXmlPropertyParser(BindingContext bindingContext);

	boolean isJsonKey();

	boolean isJsonValueKey();
}
