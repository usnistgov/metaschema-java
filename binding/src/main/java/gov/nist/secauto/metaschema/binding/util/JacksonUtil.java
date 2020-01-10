package gov.nist.secauto.metaschema.binding.util;

public class JacksonUtil {

	private JacksonUtil() {
		// disable construction
	}
/*
	public static <T> T parseJson(Reader reader, Class<T> clazz) throws JsonParseException, IOException {
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

	public static <T> T parseXml(Reader reader, Class<T> clazz) throws JsonParseException, IOException {
		XmlFactory factory = new XmlFactory();
		ObjectMapper mapper = new XmlMapper(factory);
		mapper.addHandler(new XmlDeserializationProblemHandler());
		return mapper.readValue(reader, clazz);
	}

	public static void writeXml(Writer writer, Object obj) throws JsonParseException, IOException {
		ObjectMapper mapper = new XmlMapper();
		mapper.writeValue(writer, obj);
	}

	private static class XmlDeserializationProblemHandler extends DeserializationProblemHandler {

		@Override
		public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p,
				JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {
			switch (propertyName) {
			case "schemaLocation":
				break;
			default:
				return super.handleUnknownProperty(ctxt, p, deserializer, beanOrClass, propertyName);
			}
			return true;
		}
		
	}
*/
}
