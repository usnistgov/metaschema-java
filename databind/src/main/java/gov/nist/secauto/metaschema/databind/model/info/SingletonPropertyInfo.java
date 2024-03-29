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
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.IBoundNamedModelInstance;

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import edu.umd.cs.findbugs.annotations.NonNull;

class SingletonPropertyInfo
    extends AbstractModelPropertyInfo {

  public SingletonPropertyInfo(
      @NonNull IBoundNamedModelInstance property) {
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
  public void readValues(IPropertyCollector collector, Object parentInstance, IJsonParsingContext context)
      throws IOException {

    // JsonParser parser = context.getReader();
    //
    // boolean isObject = JsonToken.START_OBJECT.equals(parser.currentToken()); //
    // TODO: is this object
    // check needed?
    // if (isObject) {
    // // read the object's START_OBJECT
    // JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);
    // }

    Object value = getProperty().getDataTypeHandler().readItem(parentInstance, context);
    collector.add(value);

    // if (isObject) {
    // // read the object's END_OBJECT
    // JsonUtil.assertAndAdvance(context.getReader(), JsonToken.END_OBJECT);
    // }
  }

  @Override
  public boolean readValues(IPropertyCollector collector, Object parentInstance, StartElement start,
      IXmlParsingContext context) throws IOException, XMLStreamException {
    boolean handled = true;
    Object value = context.readModelInstanceValue(getProperty(), parentInstance, start);
    if (value != null) {
      collector.add(value);
      handled = true;
    }
    return handled;
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
    IBoundNamedModelInstance property = getProperty();
    getProperty().getDataTypeHandler().writeItem(
        ObjectUtils.notNull(property.getValue(parentObject)),
        context);
  }

  @Override
  public boolean isValueSet(Object parentInstance) throws IOException {
    return getProperty().getValue(parentInstance) != null;
  }

  @Override
  public void copy(@NonNull Object fromInstance, @NonNull Object toInstance, @NonNull IPropertyCollector collector)
      throws BindingException {
    IBoundNamedModelInstance property = getProperty();

    Object value = property.getValue(fromInstance);

    Object copiedValue = property.copyItem(ObjectUtils.requireNonNull(value), toInstance);

    collector.add(copiedValue);
  }

}
