package gov.nist.secauto.metaschema.model.info.instances;

import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.info.Type;
import gov.nist.secauto.metaschema.model.info.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.FlagDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.JsonValueKeyEnum;
import gov.nist.secauto.metaschema.model.info.definitions.ManagedObject;

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
	public boolean isJsonKeyFlag() {
		return this.equals(getContainingDefinition().getJsonKeyFlagInstance());
	}

	@Override
	public boolean isJsonValueKeyFlag() {
		boolean retval;
		ManagedObject parent = getContainingDefinition();
		if (parent instanceof FieldDefinition) {
			FieldDefinition parentField = (FieldDefinition)parent;
			retval = parentField.hasJsonValueKey() && JsonValueKeyEnum.FLAG.equals(parentField.getJsonValueKeyType()) && this.equals(parentField.getJsonValueKeyFlagInstance());
		} else {
			retval = false;
		}
		return retval;
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
