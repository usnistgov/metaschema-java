package gov.nist.secauto.metaschema.model.info;

import gov.nist.secauto.metaschema.model.info.definitions.DataType;

public interface Field extends InfoElement {
	String getFormalName();
	DataType getDatatype();
}
