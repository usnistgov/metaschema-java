package gov.nist.secauto.metaschema.binding.io.json;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonUtil;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;

public class MapPropertyValueHandler extends AbstractPropertyValueHandler {
	private final LinkedHashMap<String, Object> values = new LinkedHashMap<>();
	private final PropertyInfo jsonKeyPropertyInfo;
	private boolean parsedStartObject = false;

	public MapPropertyValueHandler(ClassBinding<?> classBinding,
			PropertyItemHandler propertyItemHandler, BindingContext bindingContext) throws BindingException {
		super(classBinding, propertyItemHandler);
		
		PropertyInfo propertyInfo = getPropertyBinding().getPropertyInfo();
		ClassBinding<?> itemClassBinding = bindingContext.getClassBinding(propertyInfo.getItemType());
		this.jsonKeyPropertyInfo = itemClassBinding.getJsonKeyFlagPropertyBinding().getPropertyInfo();
	}

	protected PropertyInfo getJsonKeyPropertyInfo() {
		return jsonKeyPropertyInfo;
	}

	@Override
	public boolean parseNextFieldValue(JsonParsingContext parsingContext) throws BindingException, IOException {
		/*
		 * JSON will look like this:
		 * 
		 * {
		 *   "keyValue1": VALUE,
		 *   "keyValue2": VALUE
		 * }
		 */
		JsonParser parser = parsingContext.getEventReader();

		JsonToken currentToken;
		if (!parsedStartObject) {
			// a map will always start with a START_OBJECT. Advance past this start object.
			JsonUtil.expectCurrentToken(parser, JsonToken.START_OBJECT);
			currentToken = parser.nextToken();
			parsedStartObject = true;
		} else {
			currentToken = parser.currentToken();
			if (JsonToken.END_OBJECT.equals(currentToken)) {
				// we parsed the last value item
				return false;
			}
		}

		// get the map key
		String jsonKeyName = parser.currentName();
		
		// parse to the value
		currentToken = parser.nextToken();

		// Parse the value(s) at the current token; after this the current token is
		// expected to be the end of the value (e.g., VALUE, END_OBJECT
		PropertyItemHandler propertyItemHandler = getPropertyItemHandler();
		List<Object> parsedValues = propertyItemHandler.parse(parsingContext, null);

//		// Check end of parsed value
//		JsonUtil.checkEndOfValue(parser, currentToken);

		if (parsedValues != null) {
			// post process the values to append the key, and add to the map
			PropertyInfo jsonKeyPropertyInfo = getJsonKeyPropertyInfo();
			for (Object obj : parsedValues) {
				JavaTypeAdapter<?> adapter = parsingContext.getBindingContext().getJavaTypeAdapter(jsonKeyPropertyInfo.getItemType());
				Object value = adapter.parse(jsonKeyName);
				jsonKeyPropertyInfo.setValue(obj, value);
				values.put(jsonKeyName, obj);
			}
		}

		currentToken = parser.currentToken();
		if (JsonToken.END_OBJECT.equals(currentToken)) {
			// we parsed the last value item
			// now parse past the END_OBJECT for the map
			parser.nextToken();
			return false;
		}
		
		return true;
	}

	@Override
	public Object getObject() {
		return values;
	}
}
