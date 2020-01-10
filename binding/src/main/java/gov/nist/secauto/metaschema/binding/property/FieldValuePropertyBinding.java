package gov.nist.secauto.metaschema.binding.property;

import java.lang.reflect.Field;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.FieldValueXmlPropertyParser;

public interface FieldValuePropertyBinding extends PropertyBinding {

	public static FieldValuePropertyBinding fromJavaField(Field javaField, FieldValue fieldValueAnnotation) {
		JavaFieldPropertyAccessor propertyAccesor = new JavaFieldPropertyAccessor(javaField);
		BasicPropertyInfo propertyInfo = new BasicPropertyInfo(javaField.getType(), propertyAccesor);
		return new DefaultFieldValuePropertyBinding(propertyInfo, fieldValueAnnotation);
	}

	@Override
	FieldValueXmlPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException;
}
