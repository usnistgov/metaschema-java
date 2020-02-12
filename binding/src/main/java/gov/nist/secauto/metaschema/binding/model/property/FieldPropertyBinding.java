package gov.nist.secauto.metaschema.binding.model.property;

import java.util.Objects;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.annotations.Field;

// TODO: implement collapsible
public interface FieldPropertyBinding extends ModelItemPropertyBinding {

	public static DefaultFieldPropertyBinding fromJavaField(java.lang.reflect.Field field, Field fieldAnnotation) throws BindingException {
		Objects.requireNonNull(field, "field");
		Objects.requireNonNull(fieldAnnotation, "fieldAnnotation");
		PropertyInfo propertyInfo = PropertyInfo.newPropertyInfo(field);

		return new DefaultFieldPropertyBinding(propertyInfo, fieldAnnotation);
	}

	boolean isWrappedInXml();
//	Field getFieldAnnotation();
}
