package gov.nist.secauto.metaschema.binding.io;

import gov.nist.secauto.metaschema.binding.BindingContext;

public interface ParsingContext<READER, PROBLEM_HANDLER extends ProblemHandler> {
	BindingContext getBindingContext();
	PROBLEM_HANDLER getProblemHandler();
	READER getEventReader();
}
