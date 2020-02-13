package gov.nist.secauto.metaschema.binding.io.json;

import java.io.IOException;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.property.CollectionPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.ListPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.MapPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;

public interface PropertyValueHandler {

	public static <CLASS, CLASS_BINDING extends ClassBinding<CLASS>, PROPERTY_BINDING extends PropertyBinding> PropertyValueHandler newPropertyValueHandler(
			CLASS_BINDING classBinding, PROPERTY_BINDING propertyBinding, JsonParsingContext parsingContext) throws BindingException {
		PropertyValueHandler retval;

		PropertyItemHandler propertyItemHandler = PropertyItemHandler.newPropertyItemHandler(propertyBinding, parsingContext.getBindingContext());

		PropertyInfo propertyInfo = propertyBinding.getPropertyInfo();
		if (propertyInfo instanceof CollectionPropertyInfo) {
			CollectionPropertyInfo collectionPropertyInfo = (CollectionPropertyInfo) propertyInfo;
			if (propertyInfo instanceof ListPropertyInfo) {
				retval = new ListPropertyValueHandler(classBinding, propertyItemHandler,
						JsonGroupAsBehavior.SINGLETON_OR_LIST.equals(collectionPropertyInfo.getJsonGroupAsBehavior()));
			} else if (propertyInfo instanceof MapPropertyInfo) {
				retval = new MapPropertyValueHandler(classBinding, propertyItemHandler, parsingContext.getBindingContext());
			} else {
				throw new BindingException(String.format("Unsupported collection type '%s'.",
						collectionPropertyInfo.getClass().getSimpleName()));
			}
		} else {
			retval = new SingletonPropertyValueHandler(classBinding, propertyItemHandler);
		}
		return retval;
	}

	/**
	 * Parses the next value.
	 * <p>
	 * When called, the current token is expected to be at the value which may be a
	 * {@link JsonToken#START_ARRAY} for an array of values, a
	 * {@link JsonToken#START_OBJECT} for a single object, or one of the
	 * {@link JsonToken} value types.
	 * <p>
	 * After this call completes the parser's current token is expected either at
	 * the start of the next value or at the end of the value sequence.
	 * <p>
	 * If at the start of the next value, the current token will be a
	 * {@link JsonToken#START_OBJECT} for the next object in an array, or one of the
	 * {@link JsonToken} value types.
	 * <p>
	 * Once the end of the value sequence is reached and all values have been
	 * parsed, the current token is expected to be at the
	 * {@link JsonToken#END_ARRAY} if the inside an array, or otherwise the
	 * {@link JsonToken#END_OBJECT} for this object.
	 * <p>
	 * 
	 * @param parsingContext the collection of objects used for parsing
	 * @return {@code true} if there are additional fields to parse, meaning that the end of the value sequence has not been seen, or
	 *     {@code false} otherwise.
	 * @throws BindingException if a binding error has occurred
	 * @throws IOException if an input error has occurred while parsing
	 */
	boolean parseNextFieldValue(JsonParsingContext parsingContext) throws BindingException, IOException;

	Supplier<? extends Object> getObjectSupplier();
}
