package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;

public class DefaultJsonProblemHandler implements JsonProblemHandler {
	private static final String SCHEMA_PROPERTY_NAME = "$schema";
	private static final Set<String> ignoredPropertyNames;
	
	static {
		ignoredPropertyNames = new HashSet<>();
		ignoredPropertyNames.add(SCHEMA_PROPERTY_NAME);
	}

	@Override
	public boolean handleUnknownProperty(Object obj, String propertyName, JsonParsingContext parsingContext) throws IOException {
		if (ignoredPropertyNames.contains(propertyName)) {
			JsonParser parser = parsingContext.getEventReader();
			JsonUtil.skipValue(parser);
			return true;
		}
		return false;
	}

}
