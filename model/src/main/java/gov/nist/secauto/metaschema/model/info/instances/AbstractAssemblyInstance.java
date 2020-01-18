package gov.nist.secauto.metaschema.model.info.instances;

import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.info.Assembly;
import gov.nist.secauto.metaschema.model.info.Type;
import gov.nist.secauto.metaschema.model.info.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.InfoElementDefinition;

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
	public AssemblyDefinition getDefinition() {
		return getContainingMetaschema().getAssemblyDefinitionByName(getName());
	}

	@Override
	public InfoElementDefinition getContainingDefinition() {
		return parent;
	}

}
