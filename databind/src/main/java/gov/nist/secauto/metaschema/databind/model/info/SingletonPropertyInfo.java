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

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.IBoundModelInstance;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class SingletonPropertyInfo
    extends AbstractModelPropertyInfo {

  public SingletonPropertyInfo(
      @NonNull IBoundModelInstance property) {
    super(property);
  }

  @SuppressWarnings("null")
  @Override
  public List<?> getItemsFromValue(Object value) {
    return value == null ? List.of() : List.of(value);
  }

  @Override
  public int getItemCount(Object value) {
    return value == null ? 0 : 1;
  }

  @Override
  public Class<?> getItemType() {
    return (Class<?>) getProperty().getType();
  }

  @Override
  public IPropertyCollector newPropertyCollector() {
    return new SingletonPropertyCollector();
  }

  @Override
  public void writeValues(@NonNull Object value, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    context.writeInstanceValue(getProperty(), value, parentName);
  }

  @Override
  public void writeValues(Object parentObject, IJsonWritingContext context) throws IOException {
    IBoundModelInstance property = getProperty();
    getProperty().writeItem(
        ObjectUtils.notNull(property.getValue(parentObject)),
        context,
        null);
  }

  @Override
  public boolean isValueSet(Object parentInstance) throws IOException {
    return getProperty().getValue(parentInstance) != null;
  }

  @Override
  public void copy(@NonNull Object fromInstance, @NonNull Object toInstance, @NonNull IPropertyCollector collector)
      throws BindingException {
    IBoundModelInstance property = getProperty();

    Object value = property.getValue(fromInstance);

    Object copiedValue = property.deepCopyItem(ObjectUtils.requireNonNull(value), toInstance);

    collector.add(copiedValue);
  }

  @Override
  public boolean readItems(
      IModelPropertyInfo.IReadHandler handler,
      IModelPropertyInfo.IPropertyCollector collector) throws IOException {
    return handler.readSingleton(collector);
  }

  @Override
  public void writeItems(IModelPropertyInfo.IWriteHandler handler, Object value) {
    handler.writeSingleton(value);
  }

  private static class SingletonPropertyCollector implements IModelPropertyInfo.IPropertyCollector {
    private Object object;

    @Override
    public void add(Object item) {
      if (object != null) {
        throw new IllegalStateException("A value has already been set for this singleton");
      }
      object = item;
    }

    @Override
    public void addAll(Collection<?> items) {
      int size = items.size();
      if (size > 1) {
        throw new IllegalStateException("Multiple values cannot be set for this singleton");
      } else if (size == 1) {
        add(ObjectUtils.notNull(items.iterator().next()));
      }
    }

    @Nullable
    @Override
    public Object getValue() {
      return object;
    }
  }
}
