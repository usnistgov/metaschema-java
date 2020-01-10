package gov.nist.secauto.metaschema.binding.parser;

import gov.nist.secauto.metaschema.binding.BindingContext;

public interface ParsingContext<READER> {
	BindingContext getBindingContext();
	ProblemHandler getProblemHandler();
	READER getEventReader();
}
