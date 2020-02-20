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
  public void setValue(Object obj, Object value) throws BindingException {
    boolean accessable = field.canAccess(obj);
    field.setAccessible(true);
    try {
      field.set(obj, value);
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new BindingException(String.format("Unable to set the value of field '%s' in class '%s'.", field.getName(),
          field.getDeclaringClass().getName()), ex);
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
      throw new BindingException(String.format("Unable to get the value of field '%s' in class '%s'.", field.getName(),
          field.getDeclaringClass().getName()));
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
