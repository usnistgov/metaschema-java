package gov.nist.secauto.metaschema.schemagen;

public interface FlagInstance extends InfoElementInstance {
	/**
	 * Retrieve the definition of the flag for this instance. A flag can be
	 * locally or globally defined. In the former case {@link #isReference()} will
	 * be {@code false}, and in the latter case {@link #isReference()} will be
	 * {@code true}.
	 * 
	 * @return the corresponding flag definition
	 */
	FlagDefinition getFlagDefinition();
}
