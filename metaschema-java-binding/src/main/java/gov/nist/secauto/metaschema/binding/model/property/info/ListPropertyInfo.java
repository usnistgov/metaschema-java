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

package gov.nist.secauto.metaschema.binding.model.property.info;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonUtil;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.model.common.instance.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.util.XmlEventUtil;

import org.codehaus.stax2.XMLEventReader2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class ListPropertyInfo
    extends AbstractModelPropertyInfo<ParameterizedType>
    implements IModelPropertyInfo {

  public ListPropertyInfo(IBoundNamedModelInstance property) {
    super(property);
    if (!List.class.isAssignableFrom(property.getRawType())) {
      throw new IllegalArgumentException(String.format(
          "The field '%s' on class '%s' has data type '%s', which is not the expected '%s' derived data type.",
          property.getField().getName(), property.getParentClassBinding().getBoundClass().getName(),
          property.getField().getType().getName(), List.class.getName()));
    }
  }

  @Override
  public Class<?> getItemType() {
    ParameterizedType actualType = getType();
    // this is a List so there is only a single generic type
    return (Class<?>) actualType.getActualTypeArguments()[0];
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
  public List<?> getItemsFromValue(Object value) {
    return value == null ? List.of() : (List<?>) value;
  }

  @Override
  public boolean readValue(IPropertyCollector collector, Object parentInstance, StartElement start,
      IXmlParsingContext context) throws IOException, XMLStreamException {
    XMLEventReader2 eventReader = context.getReader();

    // TODO: is this needed?
    // consume extra whitespace between elements
    XmlEventUtil.skipWhitespace(eventReader);

    QName expectedFieldItemQName = getProperty().getXmlQName();

    boolean handled = false;
    XMLEvent event;

    while ((event = eventReader.peek()).isStartElement()
        && expectedFieldItemQName.equals(event.asStartElement().getName())) {

      Object value = getProperty().readItem(parentInstance, start, context);
      if (value != null) {
        collector.add(value);
        handled = true;
      }

      // consume extra whitespace between elements
      XmlEventUtil.skipWhitespace(eventReader);
    }

    return handled;
  }

  @Override
  public void readValue(IPropertyCollector collector, Object parentInstance, IJsonParsingContext context)
      throws IOException {
    JsonParser parser = context.getReader();

    boolean parseArray = true;
    if (JsonGroupAsBehavior.SINGLETON_OR_LIST.equals(getProperty().getJsonGroupAsBehavior())
        && !JsonToken.START_ARRAY.equals(parser.currentToken())) {
      // this is a singleton
      parseArray = false;
    }

    if (parseArray) {
      // advance past the array
      JsonUtil.assertAndAdvance(parser, JsonToken.START_ARRAY);

      // parse items
      while (!JsonToken.END_ARRAY.equals(parser.currentToken())) {

        boolean isObject = JsonToken.START_OBJECT.equals(parser.currentToken());
        if (isObject) {
          // read the object's START_OBJECT
          JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);
        }

        List<Object> values = getProperty().readItem(parentInstance, context);
        collector.addAll(values);

        if (isObject) {
          // read the object's END_OBJECT
          JsonUtil.assertAndAdvance(context.getReader(), JsonToken.END_OBJECT);
        }
      }

      // advance past the END_ARRAY
      JsonUtil.assertAndAdvance(parser, JsonToken.END_ARRAY);
    } else {
      // just parse the object
      boolean isObject = JsonToken.START_OBJECT.equals(parser.currentToken());
      if (isObject) {
        // read the object's START_OBJECT
        JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);
      }

      List<Object> values = getProperty().readItem(parentInstance, context);
      collector.addAll(values);

      if (isObject) {
        // read the object's END_OBJECT
        JsonUtil.assertAndAdvance(context.getReader(), JsonToken.END_OBJECT);
      }
    }
  }

  @Override
  public boolean writeValue(Object parentInstance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    IBoundNamedModelInstance property = getProperty();
    List<? extends Object> items = getItemsFromParentInstance(parentInstance);
    for (Object item : items) {
      property.writeItem(item, parentName, context);
    }
    return true;
  }

  @Override
  public void writeValue(Object parentInstance, IJsonWritingContext context) throws IOException {
    List<? extends Object> items = getItemsFromParentInstance(parentInstance);

    JsonGenerator writer = context.getWriter();

    boolean writeArray = true;
    if (JsonGroupAsBehavior.LIST.equals(getProperty().getJsonGroupAsBehavior())
        || JsonGroupAsBehavior.SINGLETON_OR_LIST.equals(getProperty().getJsonGroupAsBehavior()) && items.size() > 1) {
      // write array, then items
      writer.writeStartArray();
    } // only other option is a singleton value, write item

    getProperty().getDataTypeHandler().writeItems(items, true, context);

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
  public void copy(@NotNull Object fromInstance, @NotNull Object toInstance, @NotNull IPropertyCollector collector)
      throws BindingException {
    IBoundNamedModelInstance property = getProperty();

    for (Object item : getItemsFromParentInstance(fromInstance)) {
      collector.add(property.copyItem(item, toInstance));
    }
  }
}
