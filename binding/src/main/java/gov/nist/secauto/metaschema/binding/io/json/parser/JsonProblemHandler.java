package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.ProblemHandler;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyAccessor;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

public interface JsonProblemHandler extends ProblemHandler {
	<CLASS> boolean handleUnknownRootProperty(CLASS instance, AssemblyClassBinding<CLASS> classBinding, String fieldName,
			JsonParsingContext parsingContext) throws BindingException, IOException;

	boolean canHandleUnknownProperty(ClassBinding<?> classBinding, String propertyName, JsonParsingContext parsingContext) throws BindingException, IOException;
	Map<PropertyAccessor, Supplier<?>> handleUnknownProperty(ClassBinding<?> classBinding, String propertyName, JsonParsingContext parsingContext) throws BindingException, IOException;

	/**
	 * 
	 * @param <CLASS>
	 * @param obj
	 * @param classBinding
	 * @param missingPropertyBindings a map of field names to property bindings for missing fields
	 * @param parsingContext
	 */
	Map<PropertyBinding, Supplier<?>> handleMissingFields(ClassBinding<?> classBinding, Map<String, PropertyBinding> missingPropertyBindings, JsonParsingContext parsingContext) throws BindingException;
}
