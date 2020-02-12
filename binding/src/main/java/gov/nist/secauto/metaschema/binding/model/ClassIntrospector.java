package gov.nist.secauto.metaschema.binding.model;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueName;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonKey;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueKey;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlSchema;
import gov.nist.secauto.metaschema.binding.model.property.AssemblyPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.DefaultFlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyAccessor;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;

public class ClassIntrospector {
	private ClassIntrospector() {
		// disable construction
	}

	/**
	 * 
	 * @return the namespace, or {@code null} if no namespace is provided
	 */
	public static String getXmlSchemaNamespace(Class<?> clazz) {
		Package classPackage = clazz.getPackage();
		XmlSchema xmlSchema = classPackage.getAnnotation(XmlSchema.class);
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

	public static List<FlagPropertyBinding> getFlagPropertyBindings(Class<?> clazz) throws BindingException {
		List<FlagPropertyBinding> retval = new LinkedList<>();
		for (Field field : clazz.getDeclaredFields()) {

			gov.nist.secauto.metaschema.binding.model.annotations.Flag flagAnnotation = field.getAnnotation(gov.nist.secauto.metaschema.binding.model.annotations.Flag.class);
			if (flagAnnotation != null) {
				PropertyInfo info = PropertyInfo.newPropertyInfo(field);
				FlagPropertyBinding binding = new DefaultFlagPropertyBinding(info, flagAnnotation, field.isAnnotationPresent(JsonKey.class), field.isAnnotationPresent(JsonFieldValueKey.class));
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

	public static <CLASS> FieldValuePropertyBinding getFieldValueBinding(FieldClassBinding<CLASS> classBinding, Class<CLASS> clazz) {
		// TODO: check for multiple bindings, and raise error if found
		FieldValuePropertyBinding retval = null;
		for (Field javaField : clazz.getDeclaredFields()) {

			gov.nist.secauto.metaschema.binding.model.annotations.FieldValue fieldValueAnnotation = javaField.getAnnotation(gov.nist.secauto.metaschema.binding.model.annotations.FieldValue.class);
			if (fieldValueAnnotation != null) {
				JsonFieldValueName jsonValue = javaField.getAnnotation(JsonFieldValueName.class);
				retval = FieldValuePropertyBinding.fromJavaField(classBinding, javaField, fieldValueAnnotation, jsonValue);
				break;
			}
		}
		return retval;
	}

	public static List<ModelItemPropertyBinding> getModelItemBindings(Class<?> clazz) throws BindingException {
		List<ModelItemPropertyBinding> retval = new LinkedList<>();
		for (Field javaField : clazz.getDeclaredFields()) {

			gov.nist.secauto.metaschema.binding.model.annotations.Field fieldAnnotation = javaField.getAnnotation(gov.nist.secauto.metaschema.binding.model.annotations.Field.class);
			if (fieldAnnotation != null) {
				FieldPropertyBinding binding = FieldPropertyBinding.fromJavaField(javaField, fieldAnnotation);
				retval.add(binding);
				continue;
			}

			gov.nist.secauto.metaschema.binding.model.annotations.Assembly assemblyAnnotation = javaField.getAnnotation(gov.nist.secauto.metaschema.binding.model.annotations.Assembly.class);
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
				return PropertyAccessor.newPropertyAccessor(javaField);
			}
		}
		return null;
	}
}
