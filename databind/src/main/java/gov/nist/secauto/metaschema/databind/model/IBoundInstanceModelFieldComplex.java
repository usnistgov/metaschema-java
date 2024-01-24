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
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureComplexItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;

import java.io.IOException;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IBoundInstanceModelFieldComplex
    extends IBoundInstanceModelField,
    IFeatureComplexItemValueHandler {

  @Override
  default IBoundInstanceModelFieldComplex getInstance() {
    return this;
  }

  @Override
  IBoundDefinitionFieldComplex getDefinition();

  @Override
  default boolean isValueWrappedInXml() {
    // always wrapped
    return true;
  }

  @Override
  @NonNull
  default QName getXmlQName() {
    return ObjectUtils.notNull(IBoundInstanceModelField.super.getXmlQName());
  }

  @Override
  default String getJsonKeyFlagName() {
    return IBoundInstanceModelField.super.getJsonKeyFlagName();
  }

  @Override
  default Object getValue(Object parent) {
    return IBoundInstanceModelField.super.getValue(parent);
  }

  @Override
  default void setValue(Object parentObject, Object value) {
    IBoundInstanceModelField.super.setValue(parentObject, value);
  }

  @Override
  default Object readItem(Object parent, IItemReadHandler handler) throws IOException {
    return handler.readItemField(ObjectUtils.requireNonNull(parent, "parent"), this);
  }

  @Override
  default Object deepCopyItem(Object item, Object parentInstance) throws BindingException {
    return getDefinition().deepCopyItem(item, parentInstance);
  }

  @Override
  default Class<?> getBoundClass() {
    return getDefinition().getBoundClass();
  }

  @Override
  default void callBeforeDeserialize(Object targetObject, Object parentObject) throws BindingException {
    getDefinition().callBeforeDeserialize(targetObject, parentObject);
  }

  @Override
  default void callAfterDeserialize(Object targetObject, Object parentObject) throws BindingException {
    getDefinition().callAfterDeserialize(targetObject, parentObject);
  }
}
