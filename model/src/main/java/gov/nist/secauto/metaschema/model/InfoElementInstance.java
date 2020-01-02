package gov.nist.secauto.metaschema.model;

public interface InfoElementInstance extends InfoElement {
	boolean isReference();
	InfoElementDefinition getDefinition();
	InfoElementDefinition getContainingDefinition();
}
