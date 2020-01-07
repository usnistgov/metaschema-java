package gov.nist.secauto.metaschema.datatype.writer;

import gov.nist.secauto.metaschema.datatype.binding.BindingContext;

public interface WritingContext<WRITER> {
	BindingContext getBindingContext();
	WRITER getEventWriter();
}
