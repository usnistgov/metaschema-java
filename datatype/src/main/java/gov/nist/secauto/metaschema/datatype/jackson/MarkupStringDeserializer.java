package gov.nist.secauto.metaschema.datatype.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import gov.nist.secauto.metaschema.datatype.MarkupString;

public class MarkupStringDeserializer extends StdDeserializer<MarkupString> {

	/**
	 * the serial UID
	 */
	private static final long serialVersionUID = 1L;

	public MarkupStringDeserializer() {
		this(MarkupString.class);
	}

	public MarkupStringDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public MarkupString deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		return MarkupString.fromMarkdown(p.getText());
	}

}
