package gov.nist.secauto.metaschema.model.info.definitions;

import java.util.Map;

import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;

public interface FlagContainer {
	
	FlagInstance getFlagInstanceByName(String name);
	Map<String, ? extends FlagInstance> getFlagInstances();
}
