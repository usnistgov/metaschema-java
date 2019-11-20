package gov.nist.secauto.metaschema.model;

import java.util.Map;

public interface FlagContainer {
	FlagInstance getFlagInstanceByName(String name);
	Map<String, ? extends FlagInstance> getFlagInstances();
}
