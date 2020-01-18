package gov.nist.secauto.metaschema.model.info.instances;

import gov.nist.secauto.metaschema.model.info.InfoElement;
import gov.nist.secauto.metaschema.model.info.definitions.InfoElementDefinition;

public interface InfoElementInstance extends InfoElement {
	boolean isReference();
	InfoElementDefinition getDefinition();
	InfoElementDefinition getContainingDefinition();
}
