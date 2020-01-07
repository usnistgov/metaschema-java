package gov.nist.secauto.metaschema.datatype.binding.property;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import gov.nist.secauto.metaschema.datatype.annotations.FieldValue;
import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.FieldValueXmlPropertyParser;

public interface FieldValuePropertyBinding extends PropertyBinding {

	public static FieldValuePropertyBinding fromJavaField(Field javaField, FieldValue fieldValueAnnotation) {
		JavaFieldPropertyAccessor propertyAccesor = new JavaFieldPropertyAccessor(javaField);
		BasicPropertyInfo<Type> propertyInfo = new BasicPropertyInfo<Type>(javaField.getGenericType(), propertyAccesor);
		return new DefaultFieldValuePropertyBinding(propertyInfo, fieldValueAnnotation);
	}

	@Override
	FieldValueXmlPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException;
}
