package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.BindingContext;

public class DefaultJsonParsingContext implements JsonParsingContext {
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
		this.parser = parser;
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
