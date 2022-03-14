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

package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractProperty<CLASS_BINDING extends IClassBinding> implements IBoundInstance {
  @NotNull
  private final Field field;
  @NotNull
  private final CLASS_BINDING parentClassBinding;

  /**
   * Construct a new bound instance based on a Java property.
   * 
   * @param field
   *          the Java field to bind to
   * @param parentClassBinding
   *          the class binding for the field's containing class
   */
  public AbstractProperty(@NotNull Field field, @NotNull CLASS_BINDING parentClassBinding) {
    this.parentClassBinding = parentClassBinding;
    this.field = field;
  }

  @Override
  public Field getField() {
    return field;
  }

  @Override
  public Type getType() {
    return ObjectUtils.notNull(getField().getGenericType());
  }

  @Override
  public CLASS_BINDING getParentClassBinding() {
    return parentClassBinding;
  }

  @Override
  public String getJavaPropertyName() {
    return ObjectUtils.notNull(field.getName());
  }

  @Override
  public Class<?> getRawType() {
    Type type = getType();
    return ObjectUtils.notNull(
        (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type));
  }

  @Override
  public Class<?> getItemType() {
    return (Class<?>) getType();
  }

  @Override
  public void setValue(Object obj, Object value) {
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

  @Override
  public Object getValue(Object obj) {
    boolean accessable = field.canAccess(obj);
    field.setAccessible(true); // NOPMD - intentional
    Object retval;
    try {
      Object result = field.get(obj);
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
}
