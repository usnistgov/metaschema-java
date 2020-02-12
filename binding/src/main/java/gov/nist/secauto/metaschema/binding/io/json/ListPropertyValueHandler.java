package gov.nist.secauto.metaschema.binding.io.json;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonUtil;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;

public class ListPropertyValueHandler extends AbstractPropertyValueHandler {
	private final boolean allowSingletonValue;
	private final List<Object> values = new LinkedList<>();
	private boolean firstValueParsed = false;

	public ListPropertyValueHandler(ClassBinding<?> classBinding,
			PropertyItemHandler propertyItemHandler, boolean requireSingletonValue) {
		super(classBinding, propertyItemHandler);
		this.allowSingletonValue = requireSingletonValue;
	}

	protected boolean isRequireSingletonValue() {
		return allowSingletonValue;
	}

	@Override
	public boolean parseNextFieldValue(JsonParsingContext parsingContext) throws BindingException, IOException {
		JsonParser parser = parsingContext.getEventReader();
		JsonToken currentToken = parser.currentToken();

		if (!firstValueParsed) {
			if (JsonToken.START_ARRAY.equals(currentToken)) {
				// advance to the start of the first value item
				currentToken = parser.nextToken();
			} else if (!allowSingletonValue) {
				throw new BindingException(String.format("Found unexpected '%s' token when parsing %s. This list doesn't allow a singleton object.",
						currentToken, JsonUtil.toLocationContext(parser, getClassBinding(), getPropertyBinding())));
			}
			firstValueParsed = true;
		}

		// Parse the value at the current token; after parsing the current token is
		// expected to be at the next START_OBJECT, END_ARRAY, or FIELD_NAME
		PropertyItemHandler propertyItemHandler = getPropertyItemHandler();
		List<Object> parsedValues = propertyItemHandler.parse(parsingContext, null);

//		if (!JsonUtil.checkEndOfValue(parser, currentToken)) {
//			throw new BindingException(String.format("Unexpected end state token '%s' after parsing %s.", currentToken,
//					JsonUtil.toLocationContext(parser, getClassBinding(), getPropertyBinding())));
//		}
//
//		// advance past the end
		currentToken = parser.currentToken();

		boolean retval;
		if (parsedValues != null) {
			values.addAll(parsedValues);
		}

		if (JsonToken.END_ARRAY.equals(currentToken)) {
			if (!firstValueParsed) {
				throw new BindingException(String.format("Found unexpected END_ARRAY token when parsing %s.",
						JsonUtil.toLocationContext(parser, getClassBinding(), getPropertyBinding())));
			} else {
				retval = false;
				// skip past the END_ARRAY
				currentToken = parser.nextToken();
			}
		} else if (JsonToken.END_OBJECT.equals(currentToken) || JsonToken.FIELD_NAME.equals(currentToken)) {
			retval = false;
		} else {
			retval = true;
		}
		return retval;
	}

	@Override
	public Object getObject() {
		return values;
	}
}
