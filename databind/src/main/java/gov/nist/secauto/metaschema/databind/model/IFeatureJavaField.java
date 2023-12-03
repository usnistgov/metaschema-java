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

package gov.nist.secauto.metaschema.databind.model;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IFeatureJavaField {

  /**
   * Gets the bound Java field associated with this instance.
   *
   * @return the Java field
   */
  @NonNull
  Field getField();

  @NonNull
  default String getJavaFieldName() {
    return ObjectUtils.notNull(getField().getName());
  }

  /**
   * Get the actual Java type of the underlying bound object.
   * <p>
   * This may be the same as the what is returned by {@link #getItemType()}, or
   * may be a Java collection class.
   *
   * @return the raw type of the bound object
   */
  @SuppressWarnings("null")
  @NonNull
  default Type getType() {
    return getField().getGenericType();
  }

  /**
   * Get the item type of the bound object. An item type is the primitive or
   * specialized type that represents that data associated with this binding.
   *
   * @return the item type of the bound object
   */
  @NonNull
  default Class<?> getItemType() {
    return (Class<?>) getType();
  }

  /**
   * Get the current value from the provided {@code parentInstance} object. The
   * provided object must be of the type associated with the definition containing
   * this instance.
   *
   * @param parent
   *          the object associated with the definition containing this instance
   * @return the value if available, or {@code null} otherwise
   */
  default Object getValue(@NonNull Object parent) {
    Field field = getField();
    boolean accessable = field.canAccess(parent);
    field.setAccessible(true); // NOPMD - intentional
    Object retval;
    try {
      Object result = field.get(parent);
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

  /**
   * Set the provided value on the provided object. The provided object must be of
   * the item's type associated with this instance.
   *
   * @param parentObject
   *          the object
   * @param value
   *          a value, which may be a simple {@link Type} or a
   *          {@link ParameterizedType} for a collection
   */
  default void setValue(@NonNull Object parentObject, Object value) {
    Field field = getField();
    boolean accessable = field.canAccess(parentObject);
    field.setAccessible(true); // NOPMD - intentional
    try {
      field.set(parentObject, value);
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new IllegalArgumentException(
          String.format(
              "Unable to set the value of field '%s' in class '%s'." +
                  " Perhaps this is a data type adapter problem on the declared class?",
              field.getName(),
              field.getDeclaringClass().getName()),
          ex);
    } finally {
      field.setAccessible(accessable); // NOPMD - intentional
    }
  }

}
