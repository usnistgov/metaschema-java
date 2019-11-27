package gov.nist.secauto.metaschema.datatype.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import gov.nist.secauto.metaschema.datatype.MarkupString;

public class MarkupStringSerializer extends StdSerializer<MarkupString> {

	/**
	 * the serial UID
	 */
	private static final long serialVersionUID = 1L;

	public MarkupStringSerializer() {
		this(MarkupString.class);
	}

	public MarkupStringSerializer(Class<?> t, boolean dummy) {
		super(t, dummy);
	}

	public MarkupStringSerializer(Class<MarkupString> t) {
		super(t);
	}

	@Override
	public void serialize(MarkupString value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeString(value.toMarkdown());
	}
}
