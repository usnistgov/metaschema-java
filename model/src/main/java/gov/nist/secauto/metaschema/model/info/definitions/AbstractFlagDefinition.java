package gov.nist.secauto.metaschema.model.info.definitions;

import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.info.Type;

public abstract class AbstractFlagDefinition<METASCHEMA extends Metaschema> extends AbstractInfoElementDefinition<METASCHEMA> implements FlagDefinition {
	public AbstractFlagDefinition(METASCHEMA metaschema) {
		super(metaschema);
	}

	@Override
	public Type getType() {
		return Type.FLAG;
	}
}
