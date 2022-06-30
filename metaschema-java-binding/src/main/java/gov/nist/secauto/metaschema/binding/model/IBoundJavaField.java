/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.binding.model;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

interface IBoundJavaField extends IBoundNamedInstance {
  /**
   * Gets the bound Java field associated with this property.
   * 
   * @return the Java field
   */
  @NotNull
  Field getField();

  @SuppressWarnings("null")
  @NotNull
  default String getJavaPropertyName() {
    return getField().getName();
  }

  @Override
  default String getName() {
    return getJavaPropertyName();
  }

  /**
   * Get the type of the bound object. This may be the same as the what is returned by
   * {@link #getItemType()}, or may be a Java collection class.
   * 
   * @return the raw type of the bound object
   */
  @Override
  @SuppressWarnings("null")
  @NotNull
  default Type getType() {
    return getField().getGenericType();
  }

  /**
   * Get the raw type of the bound object. For a collection, this will be the type of the collection.
   * 
   * @return the raw type of the property
   */
  @Override
  @SuppressWarnings("null")
  @NotNull
  default Class<?> getRawType() {
    Type type = getType();
    return (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
  }

  /**
   * Get the item type of the bound object. An item type is the primitive or specialized type that
   * represents that data associated with this binding.
   * 
   * @return the item type of the bound object
   */
  @Override
  @NotNull
  default Class<?> getItemType() {
    return (Class<?>) getType();
  }

  /**
   * Get the current value from the provided {@code parentInstance} object. The provided object must
   * be of the type associated with the definition containing this property.
   * 
   * @param parentInstance
   *          the object associated with the definition containing this property
   * @return the value if available, or {@code null} otherwise
   */
  @Override
  default Object getValue(@NotNull Object parentInstance) {
    Field field = getField();
    boolean accessable = field.canAccess(parentInstance);
    field.setAccessible(true); // NOPMD - intentional
    Object retval;
    try {
      Object result = field.get(parentInstance);
      retval = result;
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new IllegalArgumentException(
          String.format("Unable to get the value of field '%s' in class '%s'.", field.getName(),
              field.getDeclaringClass().getName()),
          ex);
    } finally {
      field.setAccessible(accessable); // NOPMD - intentional
    }
    return retval;
  }

  @Override
  default void setValue(Object obj, Object value) {
    Field field = getField();
    boolean accessable = field.canAccess(obj);
    field.setAccessible(true); // NOPMD - intentional
    try {
      field.set(obj, value);
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new IllegalArgumentException(
          String.format("Unable to set the value of field '%s' in class '%s'.", field.getName(),
              field.getDeclaringClass().getName()),
          ex);
    } finally {
      field.setAccessible(accessable); // NOPMD - intentional
    }
  }
}
