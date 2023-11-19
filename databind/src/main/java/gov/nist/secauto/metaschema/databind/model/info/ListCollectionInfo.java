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

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.IBoundModelInstance;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;

class ListCollectionInfo
    extends AbstractModelInstanceCollectionInfo {

  public ListCollectionInfo(
      @NonNull IBoundModelInstance instance) {
    super(instance);
  }

  @Override
  public Class<?> getItemType() {
    ParameterizedType actualType = (ParameterizedType) getInstance().getType();
    // this is a List so there is only a single generic type
    return ObjectUtils.notNull((Class<?>) actualType.getActualTypeArguments()[0]);
  }

  @Override
  public List<? extends Object> getItemsFromParentInstance(Object parentInstance) {
    Object value = getInstance().getValue(parentInstance);
    return getItemsFromValue(value);
  }

  @Override
  public List<? extends Object> getItemsFromValue(Object value) {
    return value == null ? CollectionUtil.emptyList() : (List<?>) value;
  }

  @Override
  public int getItemCount(Object value) {
    return value == null ? 0 : ((List<?>) value).size();
  }

  @Override
  public void writeValues(Object value, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    IBoundModelInstance instance = getInstance();
    List<? extends Object> items = getItemsFromValue(value);
    for (Object item : items) {
      context.writeInstanceValue(instance, ObjectUtils.requireNonNull(item), parentName);
    }
  }

  @Override
  public void writeValues(Object parentInstance, IJsonWritingContext context) throws IOException {
    List<? extends Object> items = getItemsFromParentInstance(parentInstance);

    @SuppressWarnings("resource") // not owned
    JsonGenerator writer = context.getWriter(); // NOPMD - intentional

    boolean writeArray = false;
    if (JsonGroupAsBehavior.LIST.equals(getInstance().getJsonGroupAsBehavior())
        || JsonGroupAsBehavior.SINGLETON_OR_LIST.equals(getInstance().getJsonGroupAsBehavior()) && items.size() > 1) {
      // write array, then items
      writeArray = true;
      writer.writeStartArray();
    } // only other option is a singleton value, write item

    for (Object targetObject : items) {
      assert targetObject != null;
      getInstance().writeItem(targetObject, context, null);
    }

    if (writeArray) {
      // write the end array
      writer.writeEndArray();
    }
  }

  @Override
  public boolean isValueSet(Object parentInstance) throws IOException {
    List<? extends Object> items = getItemsFromParentInstance(parentInstance);
    return !items.isEmpty();
  }

  @Override
  public List<?> copy(@NonNull Object fromInstance, @NonNull Object toInstance)
      throws BindingException {
    IBoundModelInstance instance = getInstance();

    List<Object> copy = emptyValue();
    for (Object item : getItemsFromParentInstance(fromInstance)) {
      copy.add(instance.deepCopyItem(ObjectUtils.requireNonNull(item), toInstance));
    }
    return copy;
  }

  @Override
  public List<Object> emptyValue() {
    return new LinkedList<>();
  }

  @Override
  public List<?> readItems(IModelInstanceCollectionInfo.IReadHandler handler) throws IOException {
    return handler.readList();
  }

  @Override
  public void writeItems(IModelInstanceCollectionInfo.IWriteHandler handler, Object value) {
    handler.writeList((List<?>) value);
  }
}
