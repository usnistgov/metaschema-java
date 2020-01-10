package gov.nist.secauto.metaschema.binding.property;

import java.util.Objects;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.annotations.Field;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.DefaultXmlObjectPropertyParser;

public class DefaultFieldPropertyBinding extends AbstractModelItemPropertyBinding implements FieldPropertyBinding {
	
	private final Field fieldAnnotation;

	public DefaultFieldPropertyBinding(PropertyInfo propertyInfo, Field fieldAnnotation) {
		super(propertyInfo);
		Objects.requireNonNull(fieldAnnotation,"fieldAnnotation");
		this.fieldAnnotation = fieldAnnotation;
	}

	@Override
	public boolean isRequired() {
		return getFieldAnnotation().required();
	}

	@Override
	public boolean isWrappedInXml() {
		return getFieldAnnotation().inXmlWrapped();
	}

	@Override
	public Field getFieldAnnotation() {
		return fieldAnnotation;
	}

	@Override
	public DefaultXmlObjectPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException {
		return new DefaultXmlObjectPropertyParser(this, bindingContext);
	}

	@Override
	public String getLocalName() {
		return ModelUtil.resolveLocalName(getFieldAnnotation().name(), getPropertyInfo().getPropertyAccessor().getSimpleName());
	}

	@Override
	public String getNamespace() throws BindingException {
		return ModelUtil.resolveNamespace(getFieldAnnotation().namespace(), getPropertyInfo().getPropertyAccessor().getContainingClass());
	}

}
