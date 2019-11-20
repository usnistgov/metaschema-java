package gov.nist.secauto.metaschema.model;

public abstract class AbstractInfoElementDefinition<METASCHEMA extends Metaschema> implements InfoElementDefinition {
	private final METASCHEMA metaschema;

	public AbstractInfoElementDefinition(METASCHEMA metaschema) {
		this.metaschema = metaschema;
	}

	@Override
	public METASCHEMA getContainingMetaschema() {
		return metaschema;
	}
}
