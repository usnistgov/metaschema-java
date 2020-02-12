package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserDelegate;

import gov.nist.secauto.metaschema.binding.BindingContext;

public class DefaultJsonParsingContext implements JsonParsingContext {
	private static final Logger logger = LogManager.getLogger(AssemblyJsonReader.class);
	private final BindingContext bindingContext;
	private final JsonParser parser;
	private final JsonProblemHandler problemHandler;

	public DefaultJsonParsingContext(JsonParser parser, BindingContext bindingContext) {
		this(parser, bindingContext, new DefaultJsonProblemHandler());
	}

	public DefaultJsonParsingContext(JsonParser parser, BindingContext bindingContext, JsonProblemHandler problemHandler) {
		Objects.requireNonNull(parser, "parser");
		Objects.requireNonNull(bindingContext, "bindingContext");
		Objects.requireNonNull(problemHandler, "problemHandler");
		this.parser = new JsonParserDelegate(parser) {
//			private void logCurrent() throws IOException {
//				JsonToken token = currentToken();
//				switch (token) {
//				case FIELD_NAME:
//					logger.info("FIELD_NAME = {}", getCurrentName());
//					break;
//				case VALUE_NUMBER_FLOAT:
//					logger.info("VALUE_NUMBER_FLOAT = {}", getFloatValue());
//					break;
//				case VALUE_NUMBER_INT:
//					logger.info("VALUE_NUMBER_INT = {}", getIntValue());
//					break;
//				case VALUE_STRING:
//					logger.info("VALUE_STRING = {}", getText());
//					break;
//				case VALUE_EMBEDDED_OBJECT:
//				case VALUE_FALSE:
//				case VALUE_NULL:
//				case VALUE_TRUE: {
//					String tokenStr = token.toString();
//					logger.info("{}", tokenStr);
//					break;
//				}
//				default: {
//					String tokenStr = token.toString();
//					logger.info("{}", tokenStr);
//				}
//				}
//			}
//
//			@Override
//			public JsonToken nextToken() throws IOException {
//				JsonToken nextToken = super.nextToken();
//				logCurrent();
//				return nextToken;
//			}
//			@Override
//			public String nextFieldName() throws IOException {
//				String retval =super.nextFieldName();
//				logCurrent();
//				return retval;
//			}
//			
		};
		this.bindingContext = bindingContext;
		this.problemHandler =problemHandler;
	}

	@Override
	public BindingContext getBindingContext() {
		return bindingContext;
	}

	@Override
	public JsonProblemHandler getProblemHandler() {
		return problemHandler;
	}

	@Override
	public JsonParser getEventReader() {
		return parser;
	}
}
