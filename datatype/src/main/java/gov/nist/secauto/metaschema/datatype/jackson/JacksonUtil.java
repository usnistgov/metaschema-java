package gov.nist.secauto.metaschema.datatype.jackson;

import java.io.IOException;
import java.io.Reader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonUtil {

	private JacksonUtil() {
		// disable construction
	}

	public static <T> T parse(Reader reader, Class<T> clazz) throws JsonParseException, IOException {
		JsonFactory jsonFactory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(jsonFactory);

		JsonParser parser = jsonFactory.createParser(reader);

		if (!JsonToken.START_OBJECT.equals(parser.nextToken())) {
			String msg = String.format("Expected START_OBJECT. Invalid token: %s", parser.getCurrentToken());
			throw new RuntimeException(msg);
		}
		findobject: while (!JsonToken.END_OBJECT.equals(parser.nextToken())) {
//		logger.trace("token: {}", parser.getCurrentToken());
			if (JsonToken.FIELD_NAME.equals(parser.getCurrentToken())) {
//			logger.trace("{}: {}",parser.getCurrentToken(), parser.getText());
				switch (parser.getText()) {

				case "$schema":
					// skip the value string
					if (!JsonToken.VALUE_STRING.equals(parser.nextToken())) {
						String msg = String.format("Expected VALUE_STRING. Invalid token: %s",
								parser.getCurrentToken());
						throw new RuntimeException(msg);
					}
					break;
				default:
					// advance to the object
					parser.nextToken();
					break findobject;
				}
			}
		}

//		mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
		T result = mapper.readValue(parser, clazz);
		return result;
	}
}
