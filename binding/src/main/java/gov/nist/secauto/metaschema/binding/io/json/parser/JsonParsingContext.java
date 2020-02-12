package gov.nist.secauto.metaschema.binding.io.json.parser;

import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.io.ParsingContext;

public interface JsonParsingContext extends ParsingContext<JsonParser, JsonProblemHandler> {

}
