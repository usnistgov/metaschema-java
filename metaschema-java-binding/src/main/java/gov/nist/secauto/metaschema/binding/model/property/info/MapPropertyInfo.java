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
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.IBoundFlagInstance;
import gov.nist.secauto.metaschema.binding.model.property.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.model.common.util.XmlEventUtil;

import org.codehaus.stax2.XMLEventReader2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class MapPropertyInfo
    extends AbstractModelPropertyInfo<ParameterizedType>
    implements IModelPropertyInfo {

  @Override
  public Collection<?> getItemsFromValue(Object value) {
    return value == null ? List.of() : ((Map<?, ?>) value).values();
  }

  public MapPropertyInfo(IBoundNamedModelInstance property) {
    super(property);
    if (!Map.class.isAssignableFrom(property.getRawType())) {
      throw new IllegalArgumentException(String.format(
          "The field '%s' on class '%s' has data type '%s', which is not the expected '%s' derived data type.",
          property.getField().getName(), property.getParentClassBinding().getBoundClass().getName(),
          property.getField().getType().getName(), Map.class.getName()));
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
  public IPropertyCollector newPropertyCollector() {
    return new MapPropertyCollector(getProperty());
  }

  @Override
  public void readValue(IPropertyCollector collector, Object parentInstance, IJsonParsingContext context)
      throws IOException {
    JsonParser jsonParser = context.getReader();
    JsonUtil.assertAndAdvance(jsonParser, JsonToken.START_OBJECT);

    // parse items
    // IClassBinding classBinding = getClassBinding();
    // if (classBinding == null) {
    // throw new BindingException(
    // String.format("Unable to parse type '%s', which is not a known bound class", getItemType()));
    // }

    IBoundNamedModelInstance property = getProperty();
    // process all map items
    while (!JsonToken.END_OBJECT.equals(jsonParser.currentToken())) {

      List<Object> values = property.readItem(parentInstance, context);
      collector.addAll(values);
    }

    // advance to next token
    JsonUtil.assertAndAdvance(jsonParser, JsonToken.END_OBJECT);
  }

  @Override
  public boolean readValue(IPropertyCollector collector, Object parentInstance, StartElement start,
      IXmlParsingContext context) throws IOException, XMLStreamException {
    QName qname = getProperty().getXmlQName();
    XMLEventReader2 eventReader = context.getReader();

    // consume extra whitespace between elements
    XmlEventUtil.skipWhitespace(eventReader);

    boolean handled = false;
    XMLEvent event;
    while ((event = eventReader.peek()).isStartElement() && qname.equals(event.asStartElement().getName())) {

      // Consume the start element
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
  public boolean writeValue(Object parentInstance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    IBoundNamedModelInstance property = getProperty();
    @SuppressWarnings("unchecked")
    Map<String, ? extends Object> items = (Map<String, ? extends Object>) property.getValue(parentInstance);
    for (Object item : items.values()) {
      property.writeItem(item, parentName, context);
    }
    return true;
  }

  public static class MapPropertyCollector implements IPropertyCollector {

    private final Map<String, Object> map = new LinkedHashMap<>();
    private final IBoundFlagInstance jsonKey;

    protected MapPropertyCollector(IBoundNamedModelInstance property) {
      IClassBinding classBinding = property.getDataTypeHandler().getClassBinding();
      this.jsonKey = classBinding != null ? classBinding.getJsonKeyFlagInstance() : null;
      if (this.jsonKey == null) {
        throw new IllegalStateException("No JSON key found");
      }
    }

    protected IBoundFlagInstance getJsonKey() {
      return jsonKey;
    }

    @Override
    public void add(Object item) {
      assert item != null;

      // lookup the key
      String key = getJsonKey().getValue(item).toString();
      map.put(key, item);
    }

    @Override
    public void addAll(Collection<?> items) {
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
  public void writeValue(Object parentInstance, IJsonWritingContext context) throws IOException {
    Collection<? extends Object> items = getItemsFromParentInstance(parentInstance);

    if (items.isEmpty()) {
      // nothing to write
      return;
    }

    JsonGenerator writer = context.getWriter();

    writer.writeStartObject();

    getProperty().getDataTypeHandler().writeItems(items, false, context);

    writer.writeEndObject();
  }

  @Override
  public boolean isValueSet(Object parentInstance) throws IOException {
    Collection<? extends Object> items = getItemsFromParentInstance(parentInstance);
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
