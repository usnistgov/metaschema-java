package gov.nist.secauto.metaschema.datatype.binding;

import java.lang.reflect.Type;

public interface PropertyInfo {
	Type getType();
	/**
	 * Get the general type of the declared class.
	 * @return
	 */
	Type getRawType();
	/**
	 * Get the type of the bound object.
	 * @return
	 */
	Type getItemType();
	PropertyAccessor getPropertyAccessor();

	/**
	 * The simple property name of the field or method in the pattern "somePropertyName".
	 * @return
	 */
	String getSimpleName();
//
//	String getLocalName(String provided);
//
//	String getNamespace(String provided) throws BindingException;


//
//	public static PropertyInfo newPropertyInfo(Type type, PropertyAccessor propertyAccessor) {
//		PropertyInfo retval = null;
//		if (type instanceof ParameterizedType) {
//			ParameterizedType parameterizedType = (ParameterizedType)type;
//			Class<?> owner = (Class<?>)parameterizedType.getRawType();
//			if (List.class.isAssignableFrom(owner)) {
//				retval = new ListPropertyInfo(parameterizedType, propertyAccessor);
//			} else if (Map.class.isAssignableFrom(owner)) {
//				retval = new MapPropertyInfo(parameterizedType, propertyAccessor);
//			}
//		} else {
//			retval = new SingletonPropertyInfo(type, propertyAccessor);
//		}
//		// TODO: throw some error
//		return retval;
//	}
}
