package gov.nist.secauto.metaschema.model;

public abstract class AbstractFlagInstance implements FlagInstance {
	private final ManagedObject parent;

	public AbstractFlagInstance(ManagedObject parent) {
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
	public FlagDefinition getDefinition() {
		FlagDefinition localFlagDefinition = getLocalFlagDefinition();
		return localFlagDefinition == null ? getContainingMetaschema().getFlagDefinitionByName(getName()) : localFlagDefinition;
	}

	@Override
	public ManagedObject getContainingDefinition() {
		return parent;
	}
}
