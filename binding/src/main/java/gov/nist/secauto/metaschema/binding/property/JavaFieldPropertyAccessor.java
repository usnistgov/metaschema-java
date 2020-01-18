package gov.nist.secauto.metaschema.binding.property;

import java.lang.reflect.Field;

import gov.nist.secauto.metaschema.binding.parser.BindingException;

public class JavaFieldPropertyAccessor implements PropertyAccessor {
	private final Field field;

	public JavaFieldPropertyAccessor(Field field) {
		this.field = field;
	}

	@Override
	public String getSimpleName() {
		return field.getName();
	}

	@Override
	public String getPropertyName() {
		return field.getName();
	}

	@Override
	public void setValue(Object obj, Object value) throws BindingException {
		boolean accessable = field.canAccess(obj);
		field.setAccessible(true);
		try {
			field.set(obj, value);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new BindingException(String.format("Unable to set the value of field '%s' in class '%s'.", field.getName(), field.getDeclaringClass().getName()));
		} finally {
			field.setAccessible(accessable);
		}
	}

	@Override
	public Object getValue(Object obj) throws BindingException {
		boolean accessable = field.canAccess(obj);
		field.setAccessible(true);
		Object retval;
		try {
			Object result = field.get(obj);
			retval = result;
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new BindingException(String.format("Unable to get the value of field '%s' in class '%s'.", field.getName(), field.getDeclaringClass().getName()));
		} finally {
			field.setAccessible(accessable);
		}
		return retval;
		
	}

	@Override
	public Class<?> getContainingClass() {
		return field.getDeclaringClass();
	}

}
