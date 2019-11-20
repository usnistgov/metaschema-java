package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.datatype.MarkupString;

public interface InstanceGenerator {

	/**
	 * Retrieves the instance name, which will be the basis for the propery name.
	 * @return the name of the instance
	 */
	String getName();
	MarkupString getDescription();
	String getJavaType();
	String getJavaTypePackage();
}
