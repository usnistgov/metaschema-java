package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.ListPropertyValueHandler;
import gov.nist.secauto.metaschema.binding.io.json.PropertyItemHandler;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyAccessor;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingType;

public class CollapsedFieldObjectParser<CLASS> extends AbstractBoundObjectParser<CLASS, FieldClassBinding<CLASS>> {
	private final Map<String, PropertyBinding> jsonPropertyBindings;
	private final Map<PropertyAccessor, Supplier<?>> accessors = new HashMap<>();
	private final Map<PropertyBinding, Supplier<?>> bindings = new HashMap<>();
	private List<Object> values = Collections.emptyList();

	public CollapsedFieldObjectParser(FieldClassBinding<CLASS> classBinding, PropertyBindingFilter filter, JsonParsingContext parsingContext,
			UnknownPropertyHandler unknownPropertyHandler) throws BindingException {
		super(classBinding, parsingContext, unknownPropertyHandler);
		this.jsonPropertyBindings = Collections
				.unmodifiableMap(classBinding.getJsonPropertyBindings(parsingContext.getBindingContext(), filter));
	}

	protected Map<String, PropertyBinding> getJsonPropertyBindings() {
		return jsonPropertyBindings;
	}

	@Override
	protected PropertyBindingSupplier getPropertyBindingSupplier() {
		return (binding, supplier) -> bindings.put(binding, supplier);
	}

	@Override
	protected PropertyAccessorSupplier getPropertyAccessorSupplier() {
		return (accessor, supplier) -> accessors.put(accessor, supplier);
	}

	@Override
	protected PropertyParser getPropertyParser() {
		return (binding) -> parseProperty(binding);
	}

	private boolean parseProperty(PropertyBinding propertyBinding) throws BindingException {
		boolean retval = false;
		if (PropertyBindingType.FIELD_VALUE.equals(propertyBinding.getPropertyBindingType())) {
			JsonParsingContext parsingContext = getParsingContext();

			PropertyItemHandler propertyItemHandler = PropertyItemHandler.newPropertyItemHandler(propertyBinding, parsingContext.getBindingContext());
			ListPropertyValueHandler propertyValueHandler = new ListPropertyValueHandler(getClassBinding(), propertyItemHandler, true);
			try {
				while (propertyValueHandler.parseNextFieldValue(parsingContext)) {
					// after calling parseNextField the current token is expected to be at the next
					// field to parse or at the END_OBJECT for the containing object
				}

				this.values = propertyValueHandler.getObjectSupplier().get();
			} catch (IOException ex) {
				throw new BindingException(ex);
			}
			retval = true;
		}
		return retval;
	}

	@Override
	public List<CLASS> parseObjects() throws BindingException {
		Map<String, PropertyBinding> propertyBindings = getJsonPropertyBindings();
		FieldValuePropertyBinding valuePropertyBinding = getClassBinding().getFieldValuePropertyBinding();

		List<CLASS> retval = new LinkedList<CLASS>();
		try {
			parseProperties(propertyBindings);
		} catch (IOException ex) {
			throw new BindingException(ex);
		}

		for (Object fieldValue : values) {
			CLASS obj = getClassBinding().newInstance();
			for (Map.Entry<PropertyBinding, Supplier<?>> entry : bindings.entrySet()) {
				PropertyBinding propertyBinding = entry.getKey();
				Supplier<?> supplier = entry.getValue();
				Object value = supplier.get();
				propertyBinding.getPropertyInfo().setValue(obj, value);
			}

			for (Map.Entry<PropertyAccessor, Supplier<?>> entry : accessors.entrySet()) {
				PropertyAccessor propertyAccessor = entry.getKey();
				Supplier<?> supplier = entry.getValue();
				Object value = supplier.get();
				propertyAccessor.setValue(obj, value);
			}

			valuePropertyBinding.getPropertyInfo().setValue(obj, fieldValue);
		}
		if (retval.isEmpty()) {
			retval = null;
		}
		return retval;
	}

}
