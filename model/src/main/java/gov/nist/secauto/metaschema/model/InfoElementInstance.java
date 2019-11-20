package gov.nist.secauto.metaschema.model;

public interface InfoElementInstance extends InfoElement {
	boolean isReference();
	InfoElementDefinition getInfoElementDefinition();
	InfoElementDefinition getContainingDefinition();
}
