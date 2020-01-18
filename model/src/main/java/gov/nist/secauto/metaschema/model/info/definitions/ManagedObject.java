package gov.nist.secauto.metaschema.model.info.definitions;

import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;

public interface ManagedObject extends FlagContainer, InfoElementDefinition {
	boolean hasJsonKey();
	FlagInstance getJsonKeyFlagInstance();
	String getPackageName();
	String getClassName();
}
