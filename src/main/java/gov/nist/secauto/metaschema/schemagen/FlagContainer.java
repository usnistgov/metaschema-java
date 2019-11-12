package gov.nist.secauto.metaschema.schemagen;

import java.util.Map;

public interface FlagContainer {
	FlagInstance getFlagInstanceByName(String name);
	Map<String, ? extends FlagInstance> getFlagInstances();
}
