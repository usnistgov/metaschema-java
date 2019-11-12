package gov.nist.secauto.metaschema.schemagen;

public interface InfoElementInstance extends InfoElement {
	boolean isReference();
	InfoElementDefinition getInfoElementDefinition();
	InfoElementDefinition getContainingDefinition();
}
