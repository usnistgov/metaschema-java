package gov.nist.secauto.metaschema.binding.property;

import java.util.Objects;

import gov.nist.secauto.metaschema.binding.annotations.Field;
import gov.nist.secauto.metaschema.binding.parser.BindingException;

// TODO: implement collapsible
public interface FieldPropertyBinding extends ModelItemPropertyBinding {

	public static DefaultFieldPropertyBinding fromJavaField(java.lang.reflect.Field field, Field fieldAnnotation) throws BindingException {
		Objects.requireNonNull(field, "field");
		Objects.requireNonNull(fieldAnnotation, "fieldAnnotation");
		PropertyInfo propertyInfo = PropertyInfo.newPropertyInfo(field);

		return new DefaultFieldPropertyBinding(propertyInfo, fieldAnnotation);
	}

	boolean isRequired();
	boolean isWrappedInXml();
	
	Field getFieldAnnotation();
}
