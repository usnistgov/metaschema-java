package gov.nist.secauto.metaschema.model;

import gov.nist.secauto.metaschema.datatype.MarkupString;

public interface InfoElement {
	String getName();
	Type getType();
	MarkupString getDescription();
	Metaschema getContainingMetaschema();
}
