package gov.nist.secauto.metaschema.model.info.instances;

import gov.nist.secauto.metaschema.model.info.Assembly;
import gov.nist.secauto.metaschema.model.info.definitions.AssemblyDefinition;

public interface AssemblyInstance extends ModelInstance, Assembly {
	/**
	 * Retrieve the definition of the assembly referenced by this instance.
	 * 
	 * @return the referenced assembly definition
	 */
	@Override
	AssemblyDefinition getDefinition();
}
