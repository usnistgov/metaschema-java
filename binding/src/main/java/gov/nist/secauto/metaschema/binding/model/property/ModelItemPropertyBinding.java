package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlObjectPropertyParser;

public interface ModelItemPropertyBinding extends NamedPropertyBinding {
	/**
	 * Gets the required minimum occurrence of the property.
	 * 
	 * @return a non-negative integer
	 */
	int getMinimumOccurance();

	/**
	 * Gets the required maximum occurrence of the property.
	 * 
	 * @return a positive integer, or zero to indicate an unbounded maximum
	 */
	int getMaximumOccurance();

	@Override
	XmlObjectPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException;
}
