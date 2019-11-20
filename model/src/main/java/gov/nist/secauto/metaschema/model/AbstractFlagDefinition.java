package gov.nist.secauto.metaschema.model;

public abstract class AbstractFlagDefinition<METASCHEMA extends Metaschema> extends AbstractInfoElementDefinition<METASCHEMA> implements FlagDefinition {
	public AbstractFlagDefinition(METASCHEMA metaschema) {
		super(metaschema);
	}

	@Override
	public Type getType() {
		return Type.FLAG;
	}
}
