package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.Map;

import gov.nist.secauto.metaschema.binding.io.ProblemHandler;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

public interface JsonProblemHandler extends ProblemHandler {

	<CLASS> boolean handleUnknownProperty(CLASS obj, ClassBinding<CLASS> classBinding, String propertyName, JsonParsingContext parsingContext) throws IOException;

	/**
	 * 
	 * @param <CLASS>
	 * @param obj
	 * @param classBinding
	 * @param missingPropertyBindings a map of field names to property bindings for missing fields
	 * @param parsingContext
	 */
	<CLASS> void handleMissingFields(CLASS obj, ClassBinding<CLASS> classBinding, Map<String, PropertyBinding> missingPropertyBindings, JsonParsingContext parsingContext);

}
