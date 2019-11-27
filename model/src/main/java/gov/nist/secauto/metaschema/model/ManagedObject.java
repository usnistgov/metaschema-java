package gov.nist.secauto.metaschema.model;

public interface ManagedObject extends FlagContainer, InfoElementDefinition {
	boolean hasJsonKey();
	FlagInstance getJsonKeyFlagInstance();
}
