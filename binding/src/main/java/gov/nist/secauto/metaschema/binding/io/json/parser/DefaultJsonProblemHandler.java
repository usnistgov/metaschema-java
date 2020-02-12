package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

public class DefaultJsonProblemHandler implements JsonProblemHandler {
	private static final String SCHEMA_PROPERTY_NAME = "$schema";
	private static final Set<String> ignoredPropertyNames;

	static {
		ignoredPropertyNames = new HashSet<>();
		ignoredPropertyNames.add(SCHEMA_PROPERTY_NAME);
	}

	@Override
	public <CLASS> boolean handleUnknownProperty(CLASS obj, ClassBinding<CLASS> classBinding, String propertyName,
			JsonParsingContext parsingContext) throws IOException {
		if (ignoredPropertyNames.contains(propertyName)) {
			JsonParser parser = parsingContext.getEventReader();
			JsonUtil.skipValue(parser);
			return true;
		}
		return false;
	}

	@Override
	public <CLASS> void handleMissingFields(CLASS obj, ClassBinding<CLASS> classBinding, Map<String, PropertyBinding> missingPropertyBindings,
			JsonParsingContext parsingContext) {
		// do nothing
	}

}
