package gov.nist.secauto.metaschema.model.info.instances;

import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.info.Type;
import gov.nist.secauto.metaschema.model.info.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.InfoElementDefinition;

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
	public FieldDefinition getDefinition() {
		return getContainingMetaschema().getFieldDefinitionByName(getName());
	}

	@Override
	public InfoElementDefinition getContainingDefinition() {
		return parent;
	}
}
