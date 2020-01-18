package gov.nist.secauto.metaschema.binding.property;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import gov.nist.secauto.metaschema.binding.annotations.GroupAs;
import gov.nist.secauto.metaschema.binding.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.parser.BindingException;

public interface PropertyInfo extends PropertyAccessor {
//
//	public static FlagPropertyInfo newFlagPropertyInfo(java.lang.reflect.Field field) throws BindingException {
//		if (!field.isAnnotationPresent(Flag.class)) {
//			throw new BindingException(String.format("In class '%s' the field '%s' must have a %s annotation", field.getDeclaringClass().getName(), field.getName(), Flag.class.getName()));
//		}
//		PropertyAccessor propertyAccessor = new JavaFieldPropertyAccessor(field);
//		Type type = field.getGenericType();
//		Flag flag = field.getAnnotation(Flag.class);
//		return new DefaultFlagPropertyInfo(type, propertyAccessor, flag, field.isAnnotationPresent(JsonKey.class));
//	}

	public static PropertyInfo newPropertyInfo(java.lang.reflect.Field field) throws BindingException {
		PropertyAccessor propertyAccessor = new JavaFieldPropertyAccessor(field);
		Type type = field.getGenericType();

		PropertyInfo retval;
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			GroupAs groupAs = field.getAnnotation(GroupAs.class);
			if (groupAs == null) {
				throw new BindingException(String.format("In class '%s' the field '%s' must have a %s annotation, since it is a collection type.", field.getDeclaringClass().getName(), field.getName(), GroupAs.class.getName()));
			}

			if (groupAs.maxOccurs() == -1 || groupAs.maxOccurs() > 1) {
				if (JsonGroupAsBehavior.KEYED.equals(groupAs.inJson())) {
					retval = new MapPropertyInfo(parameterizedType, propertyAccessor, groupAs);
				} else {
					retval = new ListPropertyInfo(parameterizedType, propertyAccessor, groupAs);
				}
			} else {
				throw new BindingException("Invalid GroupAs");
			}
		} else {
			retval = new BasicPropertyInfo(type, propertyAccessor);
		}

		return retval;
	}

	Type getType();
	/**
	 * Get the general type of the declared class.
	 * @return
	 */
	Class<?> getRawType();
	/**
	 * Get the type of the bound object.
	 * @return
	 */
	Class<?> getItemType();

	@Override
	String getSimpleName();

	PropertyCollector newPropertyCollector();

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
