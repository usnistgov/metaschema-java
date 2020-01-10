package gov.nist.secauto.metaschema.binding.property;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.DefaultFieldValuePropertyParser;
import gov.nist.secauto.metaschema.binding.parser.xml.FieldValueXmlPropertyParser;

public class DefaultFieldValuePropertyBinding extends AbstractPropertyBinding implements FieldValuePropertyBinding {

	private final FieldValue fieldValueAnnotation;

	public DefaultFieldValuePropertyBinding(BasicPropertyInfo propertyInfo, FieldValue fieldValueAnnotation) {
		super(propertyInfo);
		this.fieldValueAnnotation = fieldValueAnnotation;
	}

	protected FieldValue getFieldValueAnnotation() {
		return fieldValueAnnotation;
	}

	@Override
	public FieldValueXmlPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException {
		return new DefaultFieldValuePropertyParser(this, bindingContext);
	}

}
