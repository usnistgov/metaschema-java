package gov.nist.secauto.metaschema.model.info.instances;

import gov.nist.secauto.metaschema.model.info.Flag;
import gov.nist.secauto.metaschema.model.info.definitions.FlagDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.ManagedObject;

public interface FlagInstance extends InfoElementInstance, Flag {
	/**
	 * Retrieve the definition of the flag for this instance. A flag can be
	 * locally or globally defined. In the former case {@link #isReference()} will
	 * be {@code false}, and in the latter case {@link #isReference()} will be
	 * {@code true}.
	 * 
	 * @return the corresponding flag definition
	 */
	@Override
	FlagDefinition getDefinition();
	boolean isRequired();
	@Override
	ManagedObject getContainingDefinition();
	boolean isJsonKeyFlag();
	boolean isJsonValueKeyFlag();
}
