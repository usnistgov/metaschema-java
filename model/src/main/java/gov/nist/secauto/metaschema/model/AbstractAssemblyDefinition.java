package gov.nist.secauto.metaschema.model;

public abstract class AbstractAssemblyDefinition<METASCHEMA extends Metaschema> extends AbstractInfoElementDefinition<METASCHEMA> implements AssemblyDefinition {

	public AbstractAssemblyDefinition(METASCHEMA metaschema) {
		super(metaschema);
	}

	@Override
	public Type getType() {
		return Type.ASSEMBLY;
	}

	@Override
	public FlagInstance getFlagInstanceByName(String name) {
		return getFlagInstances().get(name);
	}

	@Override
	public FieldInstance getFieldInstanceByName(String name) {
		return getFieldInstances().get(name);
	}

	@Override
	public AssemblyInstance getAssemblyInstanceByName(String name) {
		return getAssemblyInstances().get(name);
	}
}
