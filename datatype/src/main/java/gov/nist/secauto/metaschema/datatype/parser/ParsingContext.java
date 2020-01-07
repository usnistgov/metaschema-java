package gov.nist.secauto.metaschema.datatype.parser;

import gov.nist.secauto.metaschema.datatype.binding.BindingContext;

public interface ParsingContext<READER> {
	BindingContext getBindingContext();
	ProblemHandler getProblemHandler();
	READER getEventReader();
}
