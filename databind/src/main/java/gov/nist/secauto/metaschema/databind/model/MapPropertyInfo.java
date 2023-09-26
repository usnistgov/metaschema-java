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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.core.model.util.XmlEventUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.json.JsonUtil;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;

import org.codehaus.stax2.XMLEventReader2;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class MapPropertyInfo
    extends AbstractModelPropertyInfo {

  @SuppressWarnings("null")
  @Override
  public Collection<?> getItemsFromValue(Object value) {
    return value == null ? List.of() : ((Map<?, ?>) value).values();
  }

  @Override
  public int getItemCount(Object value) {
    return value == null ? 0 : ((Map<?, ?>) value).size();
  }

  public MapPropertyInfo(
      @NonNull IBoundNamedModelInstance property,
      @NonNull Supplier<IDataTypeHandler> dataTypeHandlerSupplier) {
    super(property, dataTypeHandlerSupplier);
  }

  @SuppressWarnings("null")
  @NonNull
  public Class<?> getKeyType() {
    ParameterizedType actualType = (ParameterizedType) getProperty().getType();
    // this is a Map so the first generic type is the key
    return (Class<?>) actualType.getActualTypeArguments()[0];
  }

  @Override
  public Class<?> getItemType() {
    return getValueType();
  }

  @SuppressWarnings("null")
  @NonNull
  public Class<?> getValueType() {
    ParameterizedType actualType = (ParameterizedType) getProperty().getType();
    // this is a Map so the second generic type is the value
    return (Class<?>) actualType.getActualTypeArguments()[1];
  }

  @Override
  public IPropertyCollector newPropertyCollector() {
    return new MapPropertyCollector();
  }

  @Override
  public void readValue(IPropertyCollector collector, Object parentInstance, IJsonParsingContext context)
      throws IOException {
    @SuppressWarnings("resource") // not owned
    JsonParser jsonParser = context.getReader(); // NOPMD - intentional

    // A map value is always wrapped in a START_OBJECT, since fields are used for the keys
    JsonUtil.assertAndAdvance(jsonParser, JsonToken.START_OBJECT);

    // process all map items
    while (!JsonToken.END_OBJECT.equals(jsonParser.currentToken())) {

      // a map item will always start with a FIELD_NAME, since this represents the key
      JsonUtil.assertCurrent(jsonParser, JsonToken.FIELD_NAME);

      Object value = getDataTypeHandler().get(parentInstance, true, context);
      collector.add(value);

      // the next item will be a FIELD_NAME, or we will encounter an END_OBJECT if all items have been
      // read
      JsonUtil.assertCurrent(jsonParser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
    }

    // A map value will always end with an end object, which needs to be consumed
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

  @Override
  public void writeValue(Object value, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    IBoundNamedModelInstance property = getProperty();
    @SuppressWarnings("unchecked") Map<String, ? extends Object> items = (Map<String, ? extends Object>) value;
    for (Object item : items.values()) {
      property.writeItem(ObjectUtils.notNull(item), parentName, context);
    }
  }

  public class MapPropertyCollector implements IPropertyCollector {
    @NonNull
    private final Map<String, Object> map = new LinkedHashMap<>(); // NOPMD - single threaded
    @Nullable
    private final IBoundFlagInstance jsonKey;

    protected MapPropertyCollector() {
      IClassBinding classBinding = getDataTypeHandler().getClassBinding();
      this.jsonKey = classBinding == null ? null : classBinding.getJsonKeyFlagInstance();
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
        add(ObjectUtils.requireNonNull(item));
      }
    }

    @NonNull
    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "this is a data holder")
    public Map<String, Object> getValue() {
      return map;
    }
  }

  @Override
  public void writeValue(Object parentInstance, IJsonWritingContext context) throws IOException {
    Collection<? extends Object> items = getItemsFromParentInstance(parentInstance);

    if (!items.isEmpty()) {
      @SuppressWarnings("resource") // not owned
      JsonGenerator writer = context.getWriter(); // NOPMD not closable here

      writer.writeStartObject();

      getDataTypeHandler().writeItems(items, false, context);

      writer.writeEndObject();
    }
  }

  @Override
  public boolean isValueSet(Object parentInstance) throws IOException {
    Collection<? extends Object> items = getItemsFromParentInstance(parentInstance);
    return !items.isEmpty();
  }

  @Override
  public void copy(@NonNull Object fromInstance, @NonNull Object toInstance, @NonNull IPropertyCollector collector)
      throws BindingException {
    IBoundNamedModelInstance property = getProperty();

    for (Object item : getItemsFromParentInstance(fromInstance)) {
      collector.add(property.copyItem(ObjectUtils.requireNonNull(item), toInstance));
    }
  }
}
