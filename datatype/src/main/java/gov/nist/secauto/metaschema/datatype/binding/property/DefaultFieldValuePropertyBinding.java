package gov.nist.secauto.metaschema.datatype.binding.property;

import java.lang.reflect.Type;

import gov.nist.secauto.metaschema.datatype.annotations.FieldValue;
import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.DefaultFieldValuePropertyParser;
import gov.nist.secauto.metaschema.datatype.parser.xml.FieldValueXmlPropertyParser;

public class DefaultFieldValuePropertyBinding extends AbstractPropertyBinding<BasicPropertyInfo<Type>> implements FieldValuePropertyBinding {

	private final FieldValue fieldValueAnnotation;

	public DefaultFieldValuePropertyBinding(BasicPropertyInfo<Type> propertyInfo, FieldValue fieldValueAnnotation) {
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
