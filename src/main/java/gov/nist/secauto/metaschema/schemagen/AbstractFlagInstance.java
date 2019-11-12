package gov.nist.secauto.metaschema.schemagen;

public abstract class AbstractFlagInstance implements FlagInstance {
	private final InfoElementDefinition parent;

	public AbstractFlagInstance(InfoElementDefinition parent) {
		super();
		this.parent = parent;
	}

	protected abstract FlagDefinition getLocalFlagDefinition();

	@Override
	public boolean isReference() {
		return getLocalFlagDefinition() == null;
	}

	@Override
	public Type getType() {
		return Type.FLAG;
	}

	@Override
	public Metaschema getContainingMetaschema() {
		return parent.getContainingMetaschema();
	}

	@Override
	public FlagDefinition getFlagDefinition() {
		FlagDefinition localFlagDefinition = getLocalFlagDefinition();
		return localFlagDefinition == null ? getContainingMetaschema().getFlagDefinitionByName(getName()) : localFlagDefinition;
	}

	@Override
	public FlagDefinition getInfoElementDefinition() {
		return getFlagDefinition();
	}

	@Override
	public InfoElementDefinition getContainingDefinition() {
		return parent;
	}
}
