package gov.nist.secauto.metaschema.model.info;

import gov.nist.secauto.metaschema.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.Metaschema;

public interface InfoElement {
	String getName();
	Type getType();
	MarkupLine getDescription();
	Metaschema getContainingMetaschema();
}
