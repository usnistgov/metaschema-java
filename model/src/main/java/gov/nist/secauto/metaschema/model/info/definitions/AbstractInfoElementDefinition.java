package gov.nist.secauto.metaschema.model.info.definitions;

import gov.nist.secauto.metaschema.model.Metaschema;

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
