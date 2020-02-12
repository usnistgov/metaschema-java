package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.PropertyValueHandler;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

public abstract class AbstractJsonReader<CLASS, CLASS_BINDING extends ClassBinding<CLASS>> implements JsonReader<CLASS> {
	private static final Logger logger = LogManager.getLogger(AbstractJsonReader.class);

	private final CLASS_BINDING classBinding;

	public AbstractJsonReader(CLASS_BINDING classBinding) {
		this.classBinding = classBinding;
	}

	protected CLASS_BINDING getClassBinding() {
		return classBinding;
	}

	protected Map<String, NamedPropertyBinding> getNamedPropertyBindings(BindingContext bindingContext, NamedPropertyBindingFilter filter) throws BindingException {
		return Collections.unmodifiableMap(classBinding.getNamedPropertyBindings(bindingContext, filter));
	}

	protected void parseBody(CLASS obj, NamedPropertyBindingFilter filter, JsonParsingContext parsingContext) throws BindingException, IOException {
		// we are at a start object

//		JsonParser parser = parsingContext.getEventReader();
//		FlagPropertyBinding jsonKey =  getClassBinding().getJsonKeyFlagPropertyBinding();
//		if (jsonKey != null) {
//			String keyValue = parser.nextFieldName();
//			jsonKey.getPropertyInfo().setValue(obj, keyValue);
//			
//			JsonUtil.readNextToken(parser, JsonToken.START_OBJECT);
//			parseObject(obj, parsingContext);
//			JsonUtil.expectCurrentToken(parser, JsonToken.START_OBJECT);
//			JsonUtil.readNextToken(parser, JsonToken.END_OBJECT);
//		} else {
			parseObject(obj, filter, parsingContext);
//		}
	}

	protected void parseObject(CLASS obj, NamedPropertyBindingFilter filter, JsonParsingContext parsingContext) throws BindingException, IOException {
		JsonParser parser = parsingContext.getEventReader();

		Map<String, NamedPropertyBinding> namedPropertyBindings = getNamedPropertyBindings(parsingContext.getBindingContext(), filter);

//		for (Map.Entry<String, NamedPropertyBinding> entry : namedPropertyBindings.entrySet()) {
//			logger.info("  Binding: {} = {}", entry.getKey(), entry.getValue().getClass().getSimpleName());
//		}

		JsonUtil.expectCurrentToken(parser, JsonToken.START_OBJECT);
		// parse to the first field
		parser.nextToken();

		Set<String> parsedProperties = new HashSet<>();
		String nextFieldName;
		while (JsonToken.FIELD_NAME.equals(parser.currentToken()) && (nextFieldName = parser.getCurrentName()) != null) {
			NamedPropertyBinding propertyBinding = namedPropertyBindings.get(nextFieldName);
			if (propertyBinding == null) {
				parsingContext.getProblemHandler().handleUnknownProperty(obj, nextFieldName, parsingContext);
			} else {
//				logger.info("  Parsing field '{}'", nextFieldName);

				parseProperty(obj, propertyBinding, parsingContext);
				parsedProperties.add(nextFieldName);
			}
		}

		// Parse to after the end object
		JsonUtil.expectCurrentToken(parser, JsonToken.END_OBJECT);
		parser.nextToken();

		Set<String> keySet = new LinkedHashSet<>(namedPropertyBindings.keySet());
		keySet.removeAll(parsedProperties);
//		for (String key : keySet) {
//			logger.info("  Unparsed field '{}'", key);
//		}
	}


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
		JsonToken currentToken = parser.nextToken();
		while (propertyValueHandler.parseNextFieldValue(parsingContext)) {
			// after calling parseNextField the current token is expected to be at the next
			// field to parse or at the END_OBJECT for the containing object
		}

		propertyBinding.getPropertyInfo().setValue(obj, propertyValueHandler.getObject());
	}
}
