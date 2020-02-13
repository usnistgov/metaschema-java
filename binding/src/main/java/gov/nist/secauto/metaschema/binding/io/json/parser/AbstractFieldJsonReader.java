package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;

public abstract class AbstractFieldJsonReader<CLASS, OBJECT_PARSER extends BoundObjectParser<CLASS, FieldClassBinding<CLASS>>> extends AbstractJsonReader<CLASS, FieldClassBinding<CLASS>, OBJECT_PARSER> implements FieldJsonReader<CLASS> {

	public AbstractFieldJsonReader(FieldClassBinding<CLASS> classBinding) {
		super(classBinding);
	}

	protected Map<PropertyBinding, Supplier<?>> handleUnknownProperty(String fieldName, Set<String> unknownFieldNames,
			JsonParsingContext parsingContext) throws BindingException {
		FlagPropertyBinding jsonValueKeyFlagPropertyBinding = getClassBinding().getJsonValueKeyFlagPropertyBinding();

		Map<PropertyBinding, Supplier<?>> retval = Collections.emptyMap();
		if (jsonValueKeyFlagPropertyBinding != null) {
			if (unknownFieldNames.isEmpty()) {
				retval = new HashMap<>();
				// parse the first unknown property using JSON value key with flag semantics
				// first set the key
				{
					PropertyInfo propertyInfo = jsonValueKeyFlagPropertyBinding.getPropertyInfo();
					JavaTypeAdapter<?> javaTypeAdapter = parsingContext.getBindingContext().getJavaTypeAdapter(propertyInfo.getItemType());
					retval.put(jsonValueKeyFlagPropertyBinding, javaTypeAdapter.parseAndSupply(fieldName));
				}

				// now parse the value
				{
					FieldValuePropertyBinding fieldValuePropertyBinding = getClassBinding().getFieldValuePropertyBinding();
					PropertyInfo propertyInfo = fieldValuePropertyBinding.getPropertyInfo();
					JavaTypeAdapter<?> javaTypeAdapter = parsingContext.getBindingContext().getJavaTypeAdapter(propertyInfo.getItemType());
					retval.put(fieldValuePropertyBinding, javaTypeAdapter.parseAndSupply(parsingContext));
				}
			} else {
				JsonParser parser = parsingContext.getEventReader();
				JsonLocation location = parser.getCurrentLocation();
				throw new BindingException(String.format("Unable to parse field '%s' for class '%s' at location %d:%d. This class expects a JSON value key mapped by a key flag. This feature cannot be used with multiple unbound fields.", fieldName, getClassBinding().getClazz().getName(), location.getLineNr(), location.getColumnNr()));
			}
		} else {
			JsonParser parser = parsingContext.getEventReader();
			try {
				JsonUtil.skipValue(parser);
			} catch (IOException ex) {
				throw new BindingException(ex);
			}
		}
		return retval;
	}

}
