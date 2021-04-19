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
import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonUtil;
import gov.nist.secauto.metaschema.binding.io.json.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.ModelProperty;
import gov.nist.secauto.metaschema.datatypes.util.XmlEventUtil;

import org.codehaus.stax2.XMLEventReader2;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class MapPropertyInfo
    extends AbstractModelPropertyInfo<ParameterizedType>
    implements ModelPropertyInfo {

  public MapPropertyInfo(ModelProperty property) {
    super(property);
    if (!Map.class.isAssignableFrom(property.getRawType())) {
      throw new RuntimeException(
          String.format(
              "The field '%s' on class '%s' has data type '%s', which is not the expected '%s' derived data type.",
              property.getField().getName(),
              property.getParentClassBinding().getBoundClass().getName(),
              property.getField().getType().getName(),
              Map.class.getName()));
    }
  }

  public Class<?> getKeyType() {
    ParameterizedType actualType = getType();
    // this is a Map so the first generic type is the key
    return (Class<?>) actualType.getActualTypeArguments()[0];
  }

  @Override
  public Class<?> getItemType() {
    return getValueType();
  }

  public Class<?> getValueType() {
    ParameterizedType actualType = getType();
    // this is a Map so the second generic type is the value
    return (Class<?>) actualType.getActualTypeArguments()[1];
  }

  @Override
  public PropertyCollector newPropertyCollector() {
    return new MapPropertyCollector(getProperty());
  }

  @Override
  public boolean readValue(PropertyCollector collector, Object parentInstance, JsonParsingContext context)
      throws IOException, BindingException {
    JsonParser jsonParser = context.getReader();
    JsonUtil.assertAndAdvance(jsonParser, JsonToken.START_OBJECT);

    // parse items
    // ClassBinding classBinding = getClassBinding();
    // if (classBinding == null) {
    // throw new BindingException(
    // String.format("Unable to parse type '%s', which is not a known bound class", getItemType()));
    // }

    ModelProperty property = getProperty();
    boolean handled = false;
    // process all map items
    while (!JsonToken.END_OBJECT.equals(jsonParser.currentToken())) {
      if (property.readItem(collector, parentInstance, context)) {
        handled = true;
      }
    }

    // advance to next token
    JsonUtil.assertAndAdvance(jsonParser, JsonToken.END_OBJECT);

    return handled;
  }

  @Override
  public boolean readValue(PropertyCollector collector, Object parentInstance, StartElement start,
      XmlParsingContext context)
      throws IOException, BindingException, XMLStreamException {
    QName qname = getProperty().getXmlQName();
    XMLEventReader2 eventReader = context.getReader();

    // consume extra whitespace between elements
    XmlEventUtil.skipWhitespace(eventReader);

    boolean handled = false;
    XMLEvent event;
    while ((event = eventReader.peek()).isStartElement() && qname.equals(event.asStartElement().getName())) {
      // Consume the start element
      if (getProperty().readItem(collector, parentInstance, start, context)) {
        handled = true;
      }

      // consume extra whitespace between elements
      XmlEventUtil.skipWhitespace(eventReader);
    }

    return handled;
  }

  @Override
  public boolean writeValue(Object parentInstance, QName parentName, XmlWritingContext context)
      throws XMLStreamException, IOException {
    ModelProperty property = getProperty();
    @SuppressWarnings("unchecked")
    Map<String, ? extends Object> items = (Map<String, ? extends Object>)property.getValue(parentInstance);
    for (Object item : items.values()) {
      property.writeItem(item, parentName, context);
    }
    return true;
  }

  public static class MapPropertyCollector
      implements PropertyCollector {

    private final Map<String, Object> map = new LinkedHashMap<>();
    private final FlagProperty jsonKey;

    protected MapPropertyCollector(ModelProperty property) {
      ClassBinding classBinding = property.getBindingSupplier().getClassBinding();
      this.jsonKey = classBinding != null ? classBinding.getJsonKey() : null;
      if (this.jsonKey == null) {
        throw new IllegalStateException("No JSON key found");
      }
    }

    protected FlagProperty getJsonKey() {
      return jsonKey;
    }

    @Override
    public void add(Object item) throws IOException {
      assert item != null;

      // lookup the key
      String key = getJsonKey().getValue(item).toString();
      map.put(key, item);
    }

    @Override
    public void addAll(Collection<?> items) throws IllegalStateException, IOException {
      for (Object item : items) {
        add(item);
      }
    }

    @Override
    public Object getValue() {
      return map;
    }
  }
  

  @Override
  public void writeValue(Object parentInstance, JsonWritingContext context) throws IOException {
    ModelProperty property = getProperty();
    @SuppressWarnings("unchecked")
    Map<String, ? extends Object> items = (Map<String, ? extends Object>) property.getValue(parentInstance);

    if (items.isEmpty()) {
      // nothing to write
      return;
    }

    JsonGenerator writer = context.getWriter();

    writer.writeStartObject();

    getProperty().getBindingSupplier().writeItems(items.values(), false, context);

    writer.writeEndObject();
  }

  @Override
  public boolean isValueSet(Object parentInstance) throws IOException {
    ModelProperty property = getProperty();
    @SuppressWarnings("unchecked")
    Map<String, ? extends Object> items = (Map<String, ? extends Object>) property.getValue(parentInstance);
    return items != null && !items.isEmpty();
  }

}
