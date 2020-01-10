package gov.nist.secauto.metaschema.binding.property;

import gov.nist.secauto.metaschema.binding.parser.BindingException;

public interface NamedPropertyBinding extends PropertyBinding {

	/**
	 * Get the bound property name as it will appear in the parsed format.
	 */
	String getLocalName();
	String getNamespace() throws BindingException;

}
