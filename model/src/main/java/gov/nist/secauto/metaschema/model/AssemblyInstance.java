package gov.nist.secauto.metaschema.model;

public interface AssemblyInstance extends ModelInstance, Assembly {
	/**
	 * Retrieve the definition of the assembly referenced by this instance.
	 * 
	 * @return the referenced assembly definition
	 */
	@Override
	AssemblyDefinition getDefinition();
}
