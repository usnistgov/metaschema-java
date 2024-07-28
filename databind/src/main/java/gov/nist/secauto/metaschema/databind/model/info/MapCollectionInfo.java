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

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class MapCollectionInfo<ITEM>
    extends AbstractModelInstanceCollectionInfo<ITEM> {

  public MapCollectionInfo(@NonNull IBoundInstanceModel<ITEM> instance) {
    super(instance);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<ITEM> getItemsFromValue(Object value) {
    return value == null ? CollectionUtil.emptyList() : ObjectUtils.notNull(((Map<?, ITEM>) value).values());
  }

  @Override
  public int size(Object value) {
    return value == null ? 0 : ((Map<?, ?>) value).size();
  }

  @Override
  public boolean isEmpty(@Nullable Object value) {
    return value == null || ((Map<?, ?>) value).isEmpty();
  }

  @SuppressWarnings("null")
  @NonNull
  public Class<?> getKeyType() {
    ParameterizedType actualType = (ParameterizedType) getInstance().getType();
    // this is a Map so the first generic type is the key
    return (Class<?>) actualType.getActualTypeArguments()[0];
  }

  @Override
  public Class<? extends ITEM> getItemType() {
    return getValueType();
  }

  @SuppressWarnings({ "null", "unchecked" })
  @NonNull
  public Class<? extends ITEM> getValueType() {
    ParameterizedType actualType = (ParameterizedType) getInstance().getType();
    // this is a Map so the second generic type is the value
    return (Class<? extends ITEM>) actualType.getActualTypeArguments()[1];
  }

  @Override
  public Map<String, ITEM> deepCopyItems(@NonNull IBoundObject fromInstance, @NonNull IBoundObject toInstance)
      throws BindingException {

    IBoundInstanceModel<ITEM> instance = getInstance();
    Map<String, ITEM> copy = emptyValue();
    for (ITEM item : getItemsFromParentInstance(fromInstance)) {
      assert item != null;

      IBoundInstanceFlag jsonKey = instance.getItemJsonKey(item);
      assert jsonKey != null;

      ITEM itemCopy = instance.deepCopyItem(ObjectUtils.requireNonNull(item), toInstance);
      String key = ObjectUtils.requireNonNull(jsonKey.getValue(itemCopy)).toString();
      copy.put(key, itemCopy);
    }
    return copy;
  }

  @Override
  public Map<String, ITEM> emptyValue() {
    return new LinkedHashMap<>();
  }

  @Override
  public Map<String, ITEM> readItems(IModelInstanceReadHandler<ITEM> handler) throws IOException {
    return handler.readMap();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void writeItems(
      IModelInstanceWriteHandler<ITEM> handler,
      Object value) throws IOException {
    handler.writeMap((Map<String, ITEM>) value);
  }
}
