package gov.nist.secauto.metaschema.binding.io.json;

import java.io.IOException;
import java.util.List;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.AssemblyPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

/**
 * Represents the type of item, which will be one of: Flag, FieldValue, Assembly
 * as an Assembly object, Field (with Flags) as a Field object, Field (without
 * Flags) as a scalar value
 */
public interface PropertyItemHandler {

	static PropertyItemHandler newPropertyItemHandler(PropertyBinding propertyBinding, BindingContext bindingContext)
			throws BindingException {
		Class<?> itemClass = propertyBinding.getPropertyInfo().getItemType();
		ClassBinding<?> itemClassBinding = bindingContext.getClassBinding(itemClass);

		PropertyItemHandler retval;
		if (itemClassBinding != null) {
			if (itemClassBinding instanceof FieldClassBinding) {
				FieldClassBinding<?> fieldClassBinding = (FieldClassBinding<?>) itemClassBinding;
				if (fieldClassBinding.isCollapsible()) {
					retval = new CollapsibleFieldPropertyItemHandler(fieldClassBinding, (FieldPropertyBinding) propertyBinding);
				} else {
					retval = new FieldPropertyItemHandler(fieldClassBinding, (FieldPropertyBinding) propertyBinding);
				}
			} else if (itemClassBinding instanceof AssemblyClassBinding) {
				retval = new AssemblyPropertyItemHandler((AssemblyClassBinding<?>) itemClassBinding,
						(AssemblyPropertyBinding) propertyBinding);
			} else {
				throw new UnsupportedOperationException(String.format("Unsupported class binding '%s' for class '%s'",
						itemClassBinding.getClass().getName(), itemClassBinding.getClazz().getName()));
			}
		} else {
			retval = new DataTypePropertyItemHandler(propertyBinding);
		}
		return retval;
	}

	PropertyBinding getPropertyBinding();

	List<Object> parse(JsonParsingContext parsingContext, NamedPropertyBindingFilter filter) throws BindingException, IOException;

	void writeValue(Object value, JsonWritingContext writingContext, NamedPropertyBindingFilter filter)
			throws BindingException, IOException;
}
