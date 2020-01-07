package gov.nist.secauto.metaschema.datatype.binding.property;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import gov.nist.secauto.metaschema.datatype.annotations.GroupAs;
import gov.nist.secauto.metaschema.datatype.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.datatype.annotations.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public interface CollectionPropertyInfo extends PropertyInfo {

	public static CollectionPropertyInfo newCollectionPropertyInfo(java.lang.reflect.Field field) throws BindingException {
		PropertyAccessor propertyAccessor = new JavaFieldPropertyAccessor(field);
		Type type = field.getGenericType();

		CollectionPropertyInfo retval;
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			GroupAs groupAs = field.getAnnotation(GroupAs.class);
			if (groupAs == null) {
				throw new BindingException(String.format("In class '%s' the field '%s' must have a GroupAs annotation, since it is a collection type.", field.getDeclaringClass().getName(), field.getName()));
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
			retval = new SingletonPropertyInfo(type, propertyAccessor);
		}

		return retval;
	}

	PropertyCollector newPropertyCollector();

	boolean isList();
	boolean isMap();

	boolean isGrouped();

	String getGroupLocalName();
	String getGroupNamespace() throws BindingException;

	XmlGroupAsBehavior getXmlGroupAsBehavior();
	JsonGroupAsBehavior getJsonGroupAsBehavior();

}
