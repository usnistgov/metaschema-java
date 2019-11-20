package gov.nist.secauto.metaschema.model;

public abstract class AbstractAssemblyInstance implements AssemblyInstance, Assembly {
	private final InfoElementDefinition parent;

	public AbstractAssemblyInstance(InfoElementDefinition parent) {
		this.parent = parent;
	}

	@Override
	public boolean isReference() {
		return true;
	}

	@Override
	public Type getType() {
		return Type.ASSEMBLY;
	}

	@Override
	public Metaschema getContainingMetaschema() {
		return parent.getContainingMetaschema();
	}

	@Override
	public AssemblyDefinition getAssemblyDefinition() {
		return getContainingMetaschema().getAssemblyDefinitionByName(getName());
	}

	@Override
	public AssemblyDefinition getInfoElementDefinition() {
		return getAssemblyDefinition();
	}

	@Override
	public InfoElementDefinition getContainingDefinition() {
		return parent;
	}

}
