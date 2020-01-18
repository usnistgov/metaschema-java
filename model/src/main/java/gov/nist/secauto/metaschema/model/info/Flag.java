package gov.nist.secauto.metaschema.model.info;

import gov.nist.secauto.metaschema.model.info.definitions.DataType;

public interface Flag extends InfoElement {
	String getFormalName();
	DataType getDatatype();
}
