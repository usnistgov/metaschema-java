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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.util.JsonUtil;
import gov.nist.secauto.metaschema.core.model.util.XmlEventUtil;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.IBoundModelInstance;

import org.codehaus.stax2.XMLEventReader2;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umd.cs.findbugs.annotations.NonNull;

class ListPropertyInfo
    extends AbstractModelPropertyInfo {

  public ListPropertyInfo(
      @NonNull IBoundModelInstance property) {
    super(property);
  }

  @Override
  public Class<?> getItemType() {
    ParameterizedType actualType = (ParameterizedType) getProperty().getType();
    // this is a List so there is only a single generic type
    return ObjectUtils.notNull((Class<?>) actualType.getActualTypeArguments()[0]);
  }

  @Override
  public ListPropertyCollector newPropertyCollector() {
    return new ListPropertyCollector();
  }

  @Override
  public List<? extends Object> getItemsFromParentInstance(Object parentInstance) {
    Object value = getProperty().getValue(parentInstance);
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
  public boolean readItems(
      IPropertyCollector collector,
      Object parentInstance,
      StartElement start,
      IXmlParsingContext context) throws IOException, XMLStreamException {
    XMLEventReader2 eventReader = context.getReader();

    // TODO: is this needed?
    // consume extra whitespace between elements
    XmlEventUtil.skipWhitespace(eventReader);

    IBoundModelInstance property = getProperty();

    boolean handled = false;
    XMLEvent event;

    while ((event = eventReader.peek()).isStartElement()
        && property.canHandleXmlQName(ObjectUtils.notNull(event.asStartElement().getName()))) {

      Object value = context.readModelInstanceValue(getProperty(), parentInstance, start);
      if (value != null) {
        collector.add(value);
        handled = true;
      }

      // consume extra whitespace between elements
      XmlEventUtil.skipWhitespace(eventReader);
    }

    return handled;
  }

  @SuppressWarnings({
      "resource", // not owned
      "PMD.ImplicitSwitchFallThrough" // false positive
  })

  @Override
  public void readItems(
      IPropertyCollector collector,
      Object parentInstance,
      IJsonParsingContext context)
      throws IOException {
    JsonParser parser = context.getReader();

    switch (parser.currentToken()) {
    case START_ARRAY: {
      // this is an array, we need to parse the array wrapper then each item
      JsonUtil.assertAndAdvance(parser, JsonToken.START_ARRAY);

      // parse items
      while (!JsonToken.END_ARRAY.equals(parser.currentToken())) {
        Object value = getProperty().readItem(parentInstance, context, null);
        collector.add(value);
      }

      // this is the other side of the array wrapper, advance past it
      JsonUtil.assertAndAdvance(parser, JsonToken.END_ARRAY);
      break;
    }
    case VALUE_NULL: {
      JsonUtil.assertAndAdvance(parser, JsonToken.VALUE_NULL);
      break;
    }
    default:
      // this is a singleton, just parse the value as a single item
      Object value = getProperty().readItem(parentInstance, context, null);
      collector.add(value);
    }
  }

  @Override
  public void writeValues(Object value, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    IBoundModelInstance property = getProperty();
    List<? extends Object> items = getItemsFromValue(value);
    for (Object item : items) {
      context.writeInstanceValue(property, ObjectUtils.requireNonNull(item), parentName);
    }
  }

  @Override
  public void writeValues(Object parentInstance, IJsonWritingContext context) throws IOException {
    List<? extends Object> items = getItemsFromParentInstance(parentInstance);

    @SuppressWarnings("resource") // not owned
    JsonGenerator writer = context.getWriter(); // NOPMD - intentional

    boolean writeArray = false;
    if (JsonGroupAsBehavior.LIST.equals(getProperty().getJsonGroupAsBehavior())
        || JsonGroupAsBehavior.SINGLETON_OR_LIST.equals(getProperty().getJsonGroupAsBehavior()) && items.size() > 1) {
      // write array, then items
      writeArray = true;
      writer.writeStartArray();
    } // only other option is a singleton value, write item

    for (Object targetObject : items) {
      assert targetObject != null;
      getProperty().writeItem(targetObject, context, null);
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
  public void copy(@NonNull Object fromInstance, @NonNull Object toInstance, @NonNull IPropertyCollector collector)
      throws BindingException {
    IBoundModelInstance property = getProperty();

    for (Object item : getItemsFromParentInstance(fromInstance)) {
      collector.add(property.deepCopyItem(ObjectUtils.requireNonNull(item), toInstance));
    }
  }
}
