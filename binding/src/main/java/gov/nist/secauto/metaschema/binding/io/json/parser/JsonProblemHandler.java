package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;

import gov.nist.secauto.metaschema.binding.io.ProblemHandler;

public interface JsonProblemHandler extends ProblemHandler {

	boolean handleUnknownProperty(Object obj, String propertyName, JsonParsingContext parsingContext) throws IOException;

}
