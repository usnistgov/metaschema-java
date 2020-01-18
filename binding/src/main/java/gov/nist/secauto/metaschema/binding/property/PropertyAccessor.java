package gov.nist.secauto.metaschema.binding.property;

import java.lang.reflect.Field;

import gov.nist.secauto.metaschema.binding.parser.BindingException;

public interface PropertyAccessor {
	public static PropertyAccessor newPropertyAccessor(Field field) {
		return new JavaFieldPropertyAccessor(field);
	}

	Class<?> getContainingClass();
	/**
	 * Returns the field or method name for which the property is bound.
	 * @return the name
	 */
	String getSimpleName();
	/**
	 * Returns the property name, not the field or method name.
	 * @return the name in the pattern "somePropertyName"
	 */
	String getPropertyName();


	void setValue(Object obj, Object value) throws BindingException;
	Object getValue(Object obj) throws BindingException;
}
