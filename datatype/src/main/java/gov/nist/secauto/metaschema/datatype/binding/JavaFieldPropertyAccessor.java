package gov.nist.secauto.metaschema.datatype.binding;

import java.lang.reflect.Field;

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
	public void setValue(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException {
		boolean accessable = field.canAccess(obj);
		field.setAccessible(true);
		field.set(obj, value);
		field.setAccessible(accessable);
	}

	@Override
	public Object getValue(Object obj) throws IllegalArgumentException, IllegalAccessException {
		boolean accessable = field.canAccess(obj);
		field.setAccessible(true);
		Object retval = field.get(obj);
		field.setAccessible(accessable);
		return retval;
	}

	@Override
	public Class<?> getContainingClass() {
		return field.getDeclaringClass();
	}

}
