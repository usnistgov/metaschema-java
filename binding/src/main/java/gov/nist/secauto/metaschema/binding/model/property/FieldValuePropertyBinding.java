package gov.nist.secauto.metaschema.binding.model.property;

import java.lang.reflect.Field;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.parser.FieldValueXmlPropertyParser;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueName;

public interface FieldValuePropertyBinding extends PropertyBinding {

	public static FieldValuePropertyBinding fromJavaField(FieldClassBinding<?> classBinding, Field javaField, FieldValue fieldValueAnnotation, JsonFieldValueName jsonValue) {
		JavaFieldPropertyAccessor propertyAccesor = new JavaFieldPropertyAccessor(javaField);
		BasicPropertyInfo propertyInfo = new BasicPropertyInfo(javaField.getType(), propertyAccesor);
		return new DefaultFieldValuePropertyBinding(classBinding, propertyInfo, fieldValueAnnotation, jsonValue);
	}

	@Override
	FieldValueXmlPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException;
}
