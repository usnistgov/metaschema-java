package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyAccessor;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public class SingleBoundObjectParser<CLASS, CLASS_BINDING extends ClassBinding<CLASS>>
		extends AbstractBoundObjectParser<CLASS, CLASS_BINDING> {
	private final Map<String, PropertyBinding> jsonPropertyBindings;
	private final CLASS instance;

	public SingleBoundObjectParser(CLASS_BINDING classBinding, PropertyBindingFilter filter,
			JsonParsingContext parsingContext, UnknownPropertyHandler unknownPropertyHandler) throws BindingException {
		super(classBinding, parsingContext, unknownPropertyHandler);
		this.jsonPropertyBindings = Collections
				.unmodifiableMap(classBinding.getJsonPropertyBindings(parsingContext.getBindingContext(), filter));

		this.instance = getClassBinding().newInstance();
	}

	protected Map<String, PropertyBinding> getJsonPropertyBindings() {
		return jsonPropertyBindings;
	}

	protected CLASS getInstance() {
		return instance;
	}

	protected void applyToInstance(PropertyAccessor accessor, Supplier<?> supplier) throws BindingException {
		accessor.setValue(getInstance(), supplier.get());
	}

	@Override
	public List<CLASS> parseObjects() throws BindingException {
		Map<String, PropertyBinding> propertyBindings = getJsonPropertyBindings();

		try {
			parseProperties(propertyBindings);
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
		
		CLASS instance = getInstance();
		
		return instance != null ? new LinkedList<CLASS>(Collections.singletonList(instance)) : null;
	}

	@Override
	protected PropertyBindingSupplier getPropertyBindingSupplier() {
		return (binding, supplier) -> applyToInstance(binding.getPropertyInfo(), supplier);
	}

	@Override
	protected PropertyAccessorSupplier getPropertyAccessorSupplier() {
		return (accessor, supplier) -> applyToInstance(accessor, supplier);
	}


}
