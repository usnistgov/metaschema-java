/**
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

import gov.nist.secauto.metaschema.binding.BindingException;

import java.lang.reflect.Type;
import java.util.Objects;

public abstract class AbstractPropertyInfo<TYPE extends Type> implements PropertyInfo {
  private final TYPE type;
  private final PropertyAccessor propertyAccessor;

  public AbstractPropertyInfo(TYPE type, PropertyAccessor propertyAccessor) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(propertyAccessor, "propertyAccessor");

    this.type = type;
    this.propertyAccessor = propertyAccessor;
  }

  @Override
  public TYPE getType() {
    return type;
  }

  @Override
  public Class<?> getRawType() {
    return (Class<?>) getType();
  }

  @Override
  public Class<?> getItemType() {
    return (Class<?>) getType();
  }

  protected PropertyAccessor getPropertyAccessor() {
    return propertyAccessor;
  }

  @Override
  public String getSimpleName() {
    return getPropertyAccessor().getSimpleName();
  }

  @Override
  public Class<?> getContainingClass() {
    return getPropertyAccessor().getContainingClass();
  }

  @Override
  public String getPropertyName() {
    return getPropertyAccessor().getPropertyName();
  }

  @Override
  public void setValue(Object obj, Object value) throws BindingException {
    getPropertyAccessor().setValue(obj, value);
  }

  @Override
  public Object getValue(Object obj) throws BindingException {
    return getPropertyAccessor().getValue(obj);
  }

}
