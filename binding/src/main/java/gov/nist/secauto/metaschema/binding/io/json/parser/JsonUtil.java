package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

public class JsonUtil {
	private static final Logger logger = LogManager.getLogger(JsonUtil.class);
	private JsonUtil() {
		// disable construction
	}

	public static void skipValue(JsonParser parser) throws IOException {

		JsonToken currentToken = parser.currentToken();
		// skip the field name
		if (JsonToken.FIELD_NAME.equals(currentToken)) {
			currentToken = parser.nextToken();
		}

		switch (currentToken) {
		case START_ARRAY:
		case START_OBJECT:
			parser.skipChildren();
			break;
		case VALUE_FALSE:
		case VALUE_NULL:
		case VALUE_NUMBER_FLOAT:
		case VALUE_NUMBER_INT:
		case VALUE_STRING:
		case VALUE_TRUE:
			// do nothing
			break;
		default:
			// error
			String msg = String.format("Unhandled JsonToken '%s'",currentToken.toString());
			logger.error(msg);
			throw new UnsupportedOperationException(msg);
		}
	}

	public static boolean checkEndOfValue(JsonParser parser, JsonToken startToken) {
		JsonToken currentToken = parser.getCurrentToken();

		boolean retval;
		switch (startToken) {
		case START_OBJECT:
			retval = JsonToken.END_OBJECT.equals(currentToken);
			break;
		case START_ARRAY:
			retval = JsonToken.END_ARRAY.equals(currentToken);
			break;
		case VALUE_EMBEDDED_OBJECT:
		case VALUE_FALSE:
		case VALUE_NULL:
		case VALUE_NUMBER_FLOAT:
		case VALUE_NUMBER_INT:
		case VALUE_STRING:
		case VALUE_TRUE:
			retval = true;
			break;
		default:
			retval = false;
		}
		return retval;
	}

	public static JsonToken readNextToken(JsonParser parser, JsonToken expectedToken) throws IOException, BindingException {
		JsonToken token = parser.nextToken();
		if (!expectedToken.equals(token)) {
			throw new BindingException(String.format("Expected JsonToken '%s', but found JsonToken '%s.",expectedToken, token));
		}
		return token;
	}

	public static void expectCurrentToken(JsonParser parser, JsonToken expectedToken) {
		JsonToken token = parser.currentToken();
		assert (expectedToken != null && expectedToken.equals(token)) || (expectedToken == null && token == null) : String.format("Expected JsonToken '%s', but found JsonToken '%s.",expectedToken, token);
	}

	public static String toLocationContext(JsonParser parser, ClassBinding<?> classBinding, PropertyBinding propertyBinding) {
		StringBuilder builder = new StringBuilder();
		builder.append("property '");
		builder.append(propertyBinding.getPropertyInfo().getSimpleName());
		builder.append("' on class '");
		builder.append(classBinding.getClazz().getSimpleName());
		builder.append("' at location '");
		JsonLocation location = parser.getCurrentLocation();
		builder.append(location.getLineNr());
		builder.append(':');
		builder.append(location.getColumnNr());
		builder.append("'");
		return builder.toString();
	}

	public static String toString(JsonLocation location) {
		StringBuilder builder = new StringBuilder();
		builder.append(location.getLineNr());
		builder.append(':');
		builder.append(location.getColumnNr());
		return builder.toString();
	}

}
