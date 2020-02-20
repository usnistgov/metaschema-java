/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface PropertyInfo extends PropertyAccessor {
  //
  // public static FlagPropertyInfo newFlagPropertyInfo(java.lang.reflect.Field field) throws
  // BindingException {
  // if (!field.isAnnotationPresent(Flag.class)) {
  // throw new BindingException(String.format("In class '%s' the field '%s' must have a %s
  // annotation", field.getDeclaringClass().getName(), field.getName(), Flag.class.getName()));
  // }
  // PropertyAccessor propertyAccessor = new JavaFieldPropertyAccessor(field);
  // Type type = field.getGenericType();
  // Flag flag = field.getAnnotation(Flag.class);
  // return new DefaultFlagPropertyInfo(type, propertyAccessor, flag,
  // field.isAnnotationPresent(JsonKey.class));
  // }

  public static PropertyInfo newPropertyInfo(java.lang.reflect.Field field) throws BindingException {
    PropertyAccessor propertyAccessor = new JavaFieldPropertyAccessor(field);
    Type type = field.getGenericType();

    PropertyInfo retval;
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      GroupAs groupAs = field.getAnnotation(GroupAs.class);
      if (groupAs == null) {
        throw new BindingException(
            String.format("In class '%s' the field '%s' must have a %s annotation, since it is a collection type.",
                field.getDeclaringClass().getName(), field.getName(), GroupAs.class.getName()));
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
   * 
   * @return
   */
  Class<?> getRawType();

  /**
   * Get the type of the bound object.
   * 
   * @return
   */
  Class<?> getItemType();

  @Override
  String getSimpleName();

  PropertyCollector newPropertyCollector();

  //
  // String getLocalName(String provided);
  //
  // String getNamespace(String provided) throws BindingException;

  //
  // public static PropertyInfo newPropertyInfo(Type type, PropertyAccessor propertyAccessor) {
  // PropertyInfo retval = null;
  // if (type instanceof ParameterizedType) {
  // ParameterizedType parameterizedType = (ParameterizedType)type;
  // Class<?> owner = (Class<?>)parameterizedType.getRawType();
  // if (List.class.isAssignableFrom(owner)) {
  // retval = new ListPropertyInfo(parameterizedType, propertyAccessor);
  // } else if (Map.class.isAssignableFrom(owner)) {
  // retval = new MapPropertyInfo(parameterizedType, propertyAccessor);
  // }
  // } else {
  // retval = new SingletonPropertyInfo(type, propertyAccessor);
  // }
  // // TODO: throw some error
  // return retval;
  // }
}
