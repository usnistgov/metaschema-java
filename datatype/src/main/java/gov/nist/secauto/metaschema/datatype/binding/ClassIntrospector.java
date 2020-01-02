package gov.nist.secauto.metaschema.datatype.binding;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gov.nist.secauto.metaschema.datatype.annotations.JsonKey;
import gov.nist.secauto.metaschema.datatype.annotations.XmlSchema;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class ClassIntrospector {
	private ClassIntrospector() {
		// disable construction
	}

	/**
	 * 
	 * @return the namespace, or {@code null} if no namespace is provided
	 */
	public static String getXmlSchemaNamespace(Class<?> clazz) {
		XmlSchema xmlSchema = clazz.getPackage().getAnnotation(XmlSchema.class);
		String retval = null;
		if (xmlSchema != null) {
			retval = xmlSchema.namespace();
			if (retval.isBlank()) {
				retval = null;
			}
		}
		return retval;
	}

	public static String getInfoElementName(Class<?> clazz) {
		return clazz.getSimpleName();
	}

	public static List<FlagPropertyBinding> getFlagPropertyBindings(Class<?> clazz) {
		List<FlagPropertyBinding> retval = new LinkedList<>();
		for (Field field : clazz.getDeclaredFields()) {

			gov.nist.secauto.metaschema.datatype.annotations.Flag flagAnnotation = field.getAnnotation(gov.nist.secauto.metaschema.datatype.annotations.Flag.class);
			if (flagAnnotation != null) {
				PropertyAccessor accessor = new JavaFieldPropertyAccessor(field);
				SingletonPropertyInfo info = new SingletonPropertyInfo(field.getGenericType(), accessor);
				FlagPropertyBinding binding = new FlagPropertyBinding(info, flagAnnotation);
				retval.add(binding);
			}
		}
		if (retval.isEmpty()) {
			retval = Collections.emptyList();
		} else {
			retval = Collections.unmodifiableList(retval);
		}
		return retval;
	}

	public static FieldValuePropertyBinding getFieldValueBinding(Class<?> clazz) {
		// TODO: check for multiple bindings, and raise error if found
		FieldValuePropertyBinding retval = null;
		for (Field javaField : clazz.getDeclaredFields()) {

			gov.nist.secauto.metaschema.datatype.annotations.FieldValue fieldValueAnnotation = javaField.getAnnotation(gov.nist.secauto.metaschema.datatype.annotations.FieldValue.class);
			if (fieldValueAnnotation != null) {
				retval = FieldValuePropertyBinding.fromJavaField(javaField, fieldValueAnnotation);
				break;
			}
		}
		return retval;
	}

	public static List<ModelItemPropertyBinding> getModelItemBindings(Class<?> clazz) throws BindingException {
		List<ModelItemPropertyBinding> retval = new LinkedList<>();
		for (Field javaField : clazz.getDeclaredFields()) {

			gov.nist.secauto.metaschema.datatype.annotations.Field fieldAnnotation = javaField.getAnnotation(gov.nist.secauto.metaschema.datatype.annotations.Field.class);
			if (fieldAnnotation != null) {
				FieldPropertyBinding binding = FieldPropertyBinding.fromJavaField(javaField, fieldAnnotation);
				retval.add(binding);
				continue;
			}

			gov.nist.secauto.metaschema.datatype.annotations.Assembly assemblyAnnotation = javaField.getAnnotation(gov.nist.secauto.metaschema.datatype.annotations.Assembly.class);
			if (assemblyAnnotation != null) {
				AssemblyPropertyBinding binding = AssemblyPropertyBinding.fromJavaField(javaField, assemblyAnnotation);
				retval.add(binding);
				continue;
			}
		}
		if (retval.isEmpty()) {
			retval = Collections.emptyList();
		} else {
			retval = Collections.unmodifiableList(retval);
		}
		return retval;
	}

	public static PropertyAccessor getJsonKey(Class<?> clazz) {
		for (Field javaField : clazz.getDeclaredFields()) {
			if (javaField.isAnnotationPresent(JsonKey.class)) {
				return new JavaFieldPropertyAccessor(javaField);
			}
		}
		return null;
	}
}
