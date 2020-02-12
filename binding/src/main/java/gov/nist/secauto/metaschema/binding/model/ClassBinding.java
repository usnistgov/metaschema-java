package gov.nist.secauto.metaschema.binding.model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonReader;
import gov.nist.secauto.metaschema.binding.io.json.writer.AssemblyJsonWriter;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWriter;
import gov.nist.secauto.metaschema.binding.model.annotations.Assembly;
import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.Flag;
import gov.nist.secauto.metaschema.binding.model.annotations.RootWrapper;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public interface ClassBinding<CLASS> {
	Class<CLASS> getClazz();

	List<FlagPropertyBinding> getFlagPropertyBindings();

	FlagPropertyBinding getJsonKeyFlagPropertyBinding();

	Map<String, PropertyBinding> getJsonPropertyBindings(BindingContext bindingContext, PropertyBindingFilter filter) throws BindingException;

	boolean hasRootWrapper();

	RootWrapper getRootWrapper();

	XmlParsePlan<CLASS> getXmlParsePlan(BindingContext bindingContext) throws BindingException;

	XmlWriter getXmlWriter() throws BindingException;

	AssemblyJsonWriter<CLASS> getAssemblyJsonWriter(BindingContext bindingContext) throws BindingException;

	JsonReader<CLASS> getJsonReader(BindingContext bindingContext) throws BindingException;

	
	CLASS newInstance() throws BindingException;

	public static <CLASS> ClassBinding<CLASS> newClassBinding(Class<CLASS> clazz) throws BindingException {
		boolean hasFlag = false;
		boolean hasFieldValue = false;
		boolean hasModelProperty = false;
		for (Field javaField : clazz.getDeclaredFields()) {
			if (javaField.isAnnotationPresent(FieldValue.class)) {
				hasFieldValue = true;
			} else if (javaField.isAnnotationPresent(Flag.class)) {
				hasFlag = true;
			} else if (javaField.isAnnotationPresent(gov.nist.secauto.metaschema.binding.model.annotations.Field.class)
					|| javaField.isAnnotationPresent(Assembly.class)) {
				hasModelProperty = true;
			}
		}

		if (hasFieldValue && hasModelProperty) {
			throw new BindingException(String.format(
					"Class '%s' contains a FieldValue annotation and Field and/or Assembly annotations. FieldValue can only be used with Flag annotations.",
					clazz.getName()));
		}

		ClassBinding<CLASS> retval;
		if (hasFieldValue) {
			retval = new FieldClassBindingImpl<CLASS>(clazz);
		} else if (hasFlag || hasModelProperty) {
			retval = new AssemblyClassBindingImpl<CLASS>(clazz);
		} else {
			retval = null;
		}
		return retval;
	}
}
