package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.parser.DefaultXmlObjectPropertyParser;
import gov.nist.secauto.metaschema.binding.model.annotations.Field;

public class DefaultFieldPropertyBinding extends AbstractModelItemPropertyBinding implements FieldPropertyBinding {
	
	private final Field fieldAnnotation;

	public DefaultFieldPropertyBinding(PropertyInfo propertyInfo, Field fieldAnnotation) {
		super(propertyInfo, fieldAnnotation.name(), fieldAnnotation.namespace());
		this.fieldAnnotation = fieldAnnotation;
	}

	@Override
	public PropertyBindingType getPropertyBindingType() {
		return PropertyBindingType.FIELD;
	}

	@Override
	public boolean isWrappedInXml() {
		return getFieldAnnotation().inXmlWrapped();
	}

	protected Field getFieldAnnotation() {
		return fieldAnnotation;
	}

	@Override
	public DefaultXmlObjectPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException {
		return new DefaultXmlObjectPropertyParser(this, bindingContext);
	}

	@Override
	public int getMinimumOccurance() {
		PropertyInfo propertyInfo = getPropertyInfo();

		int retval;
		if (propertyInfo instanceof CollectionPropertyInfo) {
			retval = ((CollectionPropertyInfo)propertyInfo).getMinimumOccurance();
		} else {
			retval = getFieldAnnotation().required() ? 1 : 0;
		}
		return retval;
	}
}
