package gov.nist.secauto.metaschema.schemagen;

public interface AssemblyInstance extends InfoElementInstance, Assembly {
	/**
	 * Retrieve the definition of the assembly referenced by this instance.
	 * 
	 * @return the referenced assembly definition
	 */
	AssemblyDefinition getAssemblyDefinition();
}
