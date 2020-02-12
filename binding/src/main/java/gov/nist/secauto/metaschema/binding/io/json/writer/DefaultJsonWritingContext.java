package gov.nist.secauto.metaschema.binding.io.json.writer;

import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.binding.BindingContext;

public class DefaultJsonWritingContext implements JsonWritingContext {
	private final BindingContext bindingContext;
	private final JsonGenerator jsonGenerator;

	public DefaultJsonWritingContext(JsonGenerator jsonGenerator,
			BindingContext bindingContext) {
		Objects.requireNonNull(jsonGenerator, "jsonGenerator");
		Objects.requireNonNull(bindingContext, "bindingContext");
		this.jsonGenerator = jsonGenerator;
		this.bindingContext = bindingContext;
	}

	@Override
	public BindingContext getBindingContext() {
		return bindingContext;
	}

	@Override
	public JsonGenerator getEventWriter() {
		return jsonGenerator;
	}

}
