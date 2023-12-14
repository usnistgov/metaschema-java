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

package gov.nist.secauto.metaschema.databind.model.info;

import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;

import java.io.IOException;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

class SingletonCollectionInfo
    extends AbstractModelInstanceCollectionInfo {

  public SingletonCollectionInfo(@NonNull IBoundInstanceModel instance) {
    super(instance);
  }

  @Override
  public List<?> getItemsFromValue(Object value) {
    return value == null ? CollectionUtil.emptyList() : CollectionUtil.singletonList(value);
  }

  @Override
  public int getItemCount(Object value) {
    return value == null ? 0 : 1;
  }

  @Override
  public Class<?> getItemType() {
    return getInstance().getItemType();
  }

  @Override
  public Object deepCopyItems(@NonNull Object fromObject, @NonNull Object toObject)
      throws BindingException {
    IBoundInstanceModel instance = getInstance();

    Object value = instance.getValue(fromObject);

    return value == null ? null : instance.deepCopyItem(ObjectUtils.requireNonNull(value), toObject);
  }

  @Override
  public Object emptyValue() {
    return getInstance().getDefaultValue();
  }

  @Override
  public Object readItems(IModelInstanceReadHandler handler) throws IOException {
    Object value = handler.readSingleton();
    return value == null ? emptyValue() : value;
  }

  @Override
  public void writeItems(IModelInstanceWriteHandler handler, Object value) throws IOException {
    handler.writeSingleton(value);
  }
}
