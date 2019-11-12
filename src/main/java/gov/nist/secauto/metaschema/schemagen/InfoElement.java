package gov.nist.secauto.metaschema.schemagen;

public interface InfoElement {
	String getName();
	Type getType();
	Metaschema getContainingMetaschema();
}
