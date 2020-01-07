package gov.nist.secauto.metaschema.datatype.binding.property;

import java.util.Objects;

import gov.nist.secauto.metaschema.datatype.annotations.Field;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

// TODO: implement collapsible
public interface FieldPropertyBinding extends ModelItemPropertyBinding {

	public static DefaultFieldPropertyBinding fromJavaField(java.lang.reflect.Field field, Field fieldAnnotation) throws BindingException {
		Objects.requireNonNull(field, "field");
		Objects.requireNonNull(fieldAnnotation, "fieldAnnotation");
		CollectionPropertyInfo propertyInfo = CollectionPropertyInfo.newCollectionPropertyInfo(field);

		return new DefaultFieldPropertyBinding(propertyInfo, fieldAnnotation);
	}

	boolean isRequired();
	boolean isWrappedInXml();
	
	Field getFieldAnnotation();
}
