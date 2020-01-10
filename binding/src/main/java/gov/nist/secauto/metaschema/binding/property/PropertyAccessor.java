package gov.nist.secauto.metaschema.binding.property;

import java.lang.reflect.Field;

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
	 * @return the name
	 */
	String getPropertyName();


	void setValue(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;
	<TYPE> TYPE getValue(Object obj) throws IllegalArgumentException, IllegalAccessException;
}
