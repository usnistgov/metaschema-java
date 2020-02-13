package gov.nist.secauto.metaschema.model.info;

import gov.nist.secauto.metaschema.datatype.markup.MarkupString;
import gov.nist.secauto.metaschema.model.Metaschema;

public interface InfoElement {
	String getName();
	Type getType();
	MarkupString getDescription();
	Metaschema getContainingMetaschema();
}
