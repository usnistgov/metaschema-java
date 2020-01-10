package gov.nist.secauto.metaschema.binding.writer;

import gov.nist.secauto.metaschema.binding.BindingContext;

public interface WritingContext<WRITER> {
	BindingContext getBindingContext();
	WRITER getEventWriter();
}
