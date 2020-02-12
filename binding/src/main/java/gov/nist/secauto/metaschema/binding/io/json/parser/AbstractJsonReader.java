package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.PropertyValueHandler;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public abstract class AbstractJsonReader<CLASS, CLASS_BINDING extends ClassBinding<CLASS>> implements JsonReader<CLASS> {
	private final CLASS_BINDING classBinding;

	public AbstractJsonReader(CLASS_BINDING classBinding) {
		this.classBinding = classBinding;
	}

	protected CLASS_BINDING getClassBinding() {
		return classBinding;
	}

	protected Map<String, PropertyBinding> getJsonPropertyBindings(BindingContext bindingContext, PropertyBindingFilter filter) throws BindingException {
		return Collections.unmodifiableMap(classBinding.getJsonPropertyBindings(bindingContext, filter));
	}

	protected void parseObject(CLASS obj, PropertyBindingFilter filter, JsonParsingContext parsingContext) throws BindingException, IOException {
		// we are at a start object
		JsonParser parser = parsingContext.getEventReader();

		Map<String, PropertyBinding> propertyBindings = getJsonPropertyBindings(parsingContext.getBindingContext(), filter);

		JsonUtil.expectCurrentToken(parser, JsonToken.START_OBJECT);
		// parse to the first field
		parser.nextToken();

		Set<String> parsedProperties = new HashSet<>();
		Set<String> unknownProperties = new HashSet<>();
		String nextFieldName;
		while (JsonToken.FIELD_NAME.equals(parser.currentToken()) && (nextFieldName = parser.getCurrentName()) != null) {
			PropertyBinding propertyBinding = propertyBindings.get(nextFieldName);
			if (propertyBinding == null) {
				// Parse to the value
				parser.nextToken();

				// Give the problem handler a chance to handle the property
				if (!parsingContext.getProblemHandler().handleUnknownProperty(obj, getClassBinding(), nextFieldName, parsingContext)) {

					// handle it internally if possible (e.g., valueKey with flag)
					if (!handleUnknownProperty(obj, nextFieldName, Collections.unmodifiableSet(unknownProperties), parsingContext)) {
						JsonUtil.skipValue(parser);					
					}

					unknownProperties.add(nextFieldName);
				}
			} else {
//				logger.info("  Parsing field '{}'", nextFieldName);

				parseProperty(obj, propertyBinding, parsingContext);
				parsedProperties.add(nextFieldName);
			}
		}

		// Parse to after the end object
		JsonUtil.expectCurrentToken(parser, JsonToken.END_OBJECT);
		parser.nextToken();

		Map<String, PropertyBinding> missingPropertyBindings = new HashMap<>(propertyBindings);
		Set<String> keySet = missingPropertyBindings.keySet();
		keySet.removeAll(parsedProperties);
		parsingContext.getProblemHandler().handleMissingFields(obj, getClassBinding(), missingPropertyBindings, parsingContext);
		
//		for (String key : keySet) {
//			logger.info("  Unparsed field '{}'", key);
//		}
	}

	protected abstract boolean handleUnknownProperty(CLASS obj, String nextFieldName, Set<String> unmodifiableSet,
			JsonParsingContext parsingContext) throws BindingException;

	/**
	 * Parses the field and value for the bound property provided by the
	 * {@code propertyBinding} parameter.
	 * <p>
	 * When called, the parser's current token is expected to be at the
	 * {@link JsonToken#FIELD_NAME}.
	 * <p>
	 * When this method returns, the parser's current token is expected to be at the
	 * next {@link JsonToken#FIELD_NAME} if there are more fields to parse, or at
	 * the {@link JsonToken#END_OBJECT} if the last field has been parsed.
	 * 
	 * @param obj
	 * @param propertyBinding
	 * @param parsingContext
	 * @throws BindingException
	 * @throws IOException
	 */
	public void parseProperty(CLASS obj, PropertyBinding propertyBinding, JsonParsingContext parsingContext)
			throws BindingException, IOException {

		JsonParser parser = parsingContext.getEventReader();
		JsonUtil.expectCurrentToken(parser, JsonToken.FIELD_NAME);

		PropertyValueHandler propertyValueHandler = PropertyValueHandler.newPropertyValueHandler(classBinding,
				propertyBinding, parsingContext);

		// parse to "value" token, which may be a START_ARRAY, START_OBJECT, or one of
		// the "VALUE" tokens
		parser.nextToken();
		while (propertyValueHandler.parseNextFieldValue(parsingContext)) {
			// after calling parseNextField the current token is expected to be at the next
			// field to parse or at the END_OBJECT for the containing object
		}

		propertyBinding.getPropertyInfo().setValue(obj, propertyValueHandler.getObject());
	}
}
