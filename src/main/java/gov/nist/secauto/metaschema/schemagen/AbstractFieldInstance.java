package gov.nist.secauto.metaschema.schemagen;

public abstract class AbstractFieldInstance implements FieldInstance {
	private final InfoElementDefinition parent;

	public AbstractFieldInstance(InfoElementDefinition parent) {
		this.parent = parent;
	}

	@Override
	public boolean isReference() {
		return true;
	}

	@Override
	public Type getType() {
		return Type.FIELD;
	}

	@Override
	public Metaschema getContainingMetaschema() {
		return parent.getContainingMetaschema();
	}

	@Override
	public FieldDefinition getFieldDefinition() {
		return getContainingMetaschema().getFieldDefinitionByName(getName());
	}

	@Override
	public FieldDefinition getInfoElementDefinition() {
		return getFieldDefinition();
	}

	@Override
	public InfoElementDefinition getContainingDefinition() {
		return parent;
	}
}
