package gov.nist.secauto.metaschema.datatype.binding;

public interface PropertyAccessor {
	Class<?> getContainingClass();
	/**
	 * Returns the field or method name for which the property is bound.
	 * @return the name
	 */
	String getSimpleName();
	String getPropertyName();


	void setValue(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;
	Object getValue(Object obj) throws IllegalArgumentException, IllegalAccessException;
}
