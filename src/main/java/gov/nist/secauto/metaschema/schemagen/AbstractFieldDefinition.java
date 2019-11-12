package gov.nist.secauto.metaschema.schemagen;

public abstract class AbstractFieldDefinition<METASCHEMA extends Metaschema> extends AbstractInfoElementDefinition<METASCHEMA> implements FieldDefinition {

	public AbstractFieldDefinition(METASCHEMA metaschema) {
		super(metaschema);
	}

	@Override
	public Type getType() {
		return Type.FIELD;
	}

	@Override
	public FlagInstance getFlagInstanceByName(String name) {
		return getFlagInstances().get(name);
	}
}
