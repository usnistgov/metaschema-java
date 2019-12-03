package gov.nist.secauto.metaschema.model;

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

	public JsonValueKeyEnum getJsonValueKeyType() {
		JsonValueKeyEnum retval = JsonValueKeyEnum.NONE;
		if (hasJsonValueKey()) {
			FlagInstance valueKeyFlag = getJsonValueKeyFlagInstance();
			if (valueKeyFlag != null) {
				retval = JsonValueKeyEnum.FLAG;
			} else {
				retval = JsonValueKeyEnum.LABEL;
			}
		}
		return retval;
	}
}
