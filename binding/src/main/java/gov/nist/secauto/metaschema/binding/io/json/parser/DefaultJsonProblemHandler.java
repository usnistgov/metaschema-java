package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyAccessor;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

public class DefaultJsonProblemHandler implements JsonProblemHandler {
	private static final String JSON_SCHEMA_ROOT_FIELD_NAME = "$schema";
	private static final Set<String> ignoredRootFieldNames;

	static {
		ignoredRootFieldNames = new HashSet<>();
		ignoredRootFieldNames.add(JSON_SCHEMA_ROOT_FIELD_NAME);
	}

	@Override
	public <CLASS> boolean handleUnknownRootProperty(CLASS instance, AssemblyClassBinding<CLASS> classBinding,
			String fieldName, JsonParsingContext parsingContext) throws BindingException {
		if (ignoredRootFieldNames.contains(fieldName)) {
			JsonParser parser = parsingContext.getEventReader();
			try {
				JsonUtil.skipValue(parser);
			} catch (IOException ex) {
				
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canHandleUnknownProperty(ClassBinding<?> classBinding, String propertyName,
			JsonParsingContext parsingContext) throws IOException {
		return false;
	}

	@Override
	public Map<PropertyAccessor, Supplier<?>> handleUnknownProperty(ClassBinding<?> classBinding, String propertyName,
			JsonParsingContext parsingContext) throws IOException {
		return Collections.emptyMap();
	}

	@Override
	public Map<PropertyBinding, Supplier<?>> handleMissingFields(ClassBinding<?> classBinding,
			Map<String, PropertyBinding> missingPropertyBindings, JsonParsingContext parsingContext) {
		return Collections.emptyMap();
	}

}
