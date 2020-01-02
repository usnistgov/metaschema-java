package gov.nist.secauto.metaschema.datatype.binding;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public interface NamedPropertyBinding extends PropertyBinding {

	/**
	 * Get the bound property name as it will appear in the parsed format.
	 */
	String getLocalName();
	String getNamespace() throws BindingException;

}
