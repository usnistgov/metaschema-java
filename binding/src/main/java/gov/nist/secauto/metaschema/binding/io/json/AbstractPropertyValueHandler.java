package gov.nist.secauto.metaschema.binding.io.json;

import java.util.Objects;

import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

public abstract class AbstractPropertyValueHandler implements PropertyValueHandler {
	private final ClassBinding<?> classBinding;
	private final PropertyItemHandler propertyItemHandler;

	public AbstractPropertyValueHandler(ClassBinding<?> classBinding,
			PropertyItemHandler propertyItemHandler) {
		Objects.requireNonNull(classBinding, "classBinding");
		Objects.requireNonNull(propertyItemHandler, "propertyItemHandler");
		this.classBinding = classBinding;
		this.propertyItemHandler = propertyItemHandler;
	}

	protected ClassBinding<?> getClassBinding() {
		return classBinding;
	}

	protected PropertyBinding getPropertyBinding() {
		return getPropertyItemHandler().getPropertyBinding();
	}

	protected PropertyItemHandler getPropertyItemHandler() {
		return propertyItemHandler;
	}
}
