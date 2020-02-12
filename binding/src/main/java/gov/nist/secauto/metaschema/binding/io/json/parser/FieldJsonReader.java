package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.util.Set;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;

public class FieldJsonReader<CLASS> extends AbstractJsonReader<CLASS, FieldClassBinding<CLASS>> {

	public FieldJsonReader(FieldClassBinding<CLASS> classBinding) {
		super(classBinding);
	}

	@Override
	protected boolean handleUnknownProperty(CLASS obj, String fieldName, Set<String> unknownFieldNames,
			JsonParsingContext parsingContext) throws BindingException {
		FlagPropertyBinding jsonValueKeyFlagPropertyBinding = getClassBinding().getJsonValueKeyFlagPropertyBinding();

		boolean retval = false;
		if (jsonValueKeyFlagPropertyBinding != null) {
			if (unknownFieldNames.isEmpty()) {
				// parse the first unknown property using JSON value key with flag semantics
				// first set the key
				{
					PropertyInfo propertyInfo = jsonValueKeyFlagPropertyBinding.getPropertyInfo();
					JavaTypeAdapter<?> javaTypeAdapter = parsingContext.getBindingContext().getJavaTypeAdapter(propertyInfo.getItemType());
					propertyInfo.setValue(obj, javaTypeAdapter.parse(fieldName));
				}

				// now parse the value
				{
					FieldValuePropertyBinding fieldValuePropertyBinding = getClassBinding().getFieldValuePropertyBinding();
					PropertyInfo propertyInfo = fieldValuePropertyBinding.getPropertyInfo();
					JavaTypeAdapter<?> javaTypeAdapter = parsingContext.getBindingContext().getJavaTypeAdapter(propertyInfo.getItemType());
					propertyInfo.setValue(obj, javaTypeAdapter.parse(parsingContext));
				}
				
				retval = true;
			} else {
				JsonParser parser = parsingContext.getEventReader();
				JsonLocation location = parser.getCurrentLocation();
				throw new BindingException(String.format("Unable to parse field '%s' for class '%s' at location %d:%d. This class expects a JSON value key mapped by a key flag. This feature cannot be used with multiple unbound fields.", fieldName, obj.getClass().getName(), location.getLineNr(), location.getColumnNr()));
			}
		}
		return retval;
	}

}
