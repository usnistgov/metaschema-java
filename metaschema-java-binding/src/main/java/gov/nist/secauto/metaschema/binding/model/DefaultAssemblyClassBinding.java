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

package gov.nist.secauto.metaschema.binding.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonUtil;
import gov.nist.secauto.metaschema.binding.io.json.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.annotations.Assembly;
import gov.nist.secauto.metaschema.binding.model.annotations.Field;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.property.DefaultAssemblyProperty;
import gov.nist.secauto.metaschema.binding.model.property.DefaultFieldProperty;
import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.ModelProperty;
import gov.nist.secauto.metaschema.binding.model.property.NamedProperty;
import gov.nist.secauto.metaschema.binding.model.property.info.PropertyCollector;
import gov.nist.secauto.metaschema.datatypes.util.XmlEventUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class DefaultAssemblyClassBinding
    extends AbstractClassBinding
    implements AssemblyClassBinding {
  private static final Logger logger = LogManager.getLogger(DefaultAssemblyClassBinding.class);

  /**
   * Create a new {@link ClassBinding} for a Java bean annotated with the {@link Assembly} annotation.
   * 
   * @param clazz
   *          the Java bean class
   * @param bindingContext
   *          the Metaschema binding environment context
   * @return the Metaschema assembly binding for the class
   * @throws BindingException
   *           if an error occurred while parsing the class data
   */
  public static DefaultAssemblyClassBinding createInstance(Class<?> clazz, BindingContext bindingContext) {
    DefaultAssemblyClassBinding retval = new DefaultAssemblyClassBinding(clazz, bindingContext);
    retval.initialize();
    return retval;
  }

  private MetaschemaAssembly metaschemaAssembly;
  private List<ModelProperty> modelProperties;
  private final QName xmlRootQName;

  /**
   * Construct a new {@link ClassBinding} for a Java bean annotated with the {@link Assembly}
   * annotation.
   * 
   * @param clazz
   *          the Java bean class
   * @throws BindingException
   *           if an error occurred while parsing the class data
   */
  protected DefaultAssemblyClassBinding(Class<?> clazz, BindingContext bindingContext) {
    super(clazz, bindingContext);
    Objects.requireNonNull(clazz, "clazz");
    if (!clazz.isAnnotationPresent(MetaschemaAssembly.class)) {
      throw new IllegalArgumentException(String.format("Class '%s' is missing the '%' annotation.",
          clazz.getName(), Assembly.class.getName()));
    }
    this.metaschemaAssembly = clazz.getAnnotation(MetaschemaAssembly.class);
    String namespace = ModelUtil.resolveNamespace(this.metaschemaAssembly.rootNamespace(), this, false);
    String localName = ModelUtil.resolveLocalName(this.metaschemaAssembly.rootName(), null);
    if (localName != null) {
      if (namespace != null) {
        this.xmlRootQName = new QName(namespace, localName);
      } else {
        this.xmlRootQName = new QName(localName);
      }
    } else {
      this.xmlRootQName = null;
    }
  }

  @Override
  protected boolean initializeField(java.lang.reflect.Field field) {
    boolean handled = false;

    Assembly assemblyAnnotation = field.getAnnotation(Assembly.class);
    if (assemblyAnnotation != null) {
      DefaultAssemblyProperty property;
      property = DefaultAssemblyProperty.createInstance(
          this,
          field);
      modelProperties.add(property);
      handled = true;
    } else {
      Field fieldAnnotation = field.getAnnotation(Field.class);
      if (fieldAnnotation != null) {
        DefaultFieldProperty property;
        property = DefaultFieldProperty.createInstance(
            this,
            field);
        modelProperties.add(property);
        handled = true;
      }
    }
    return handled;
  }

  @Override
  protected void initialize() {
    this.modelProperties = new LinkedList<>();

    super.initialize();

    this.modelProperties
        = modelProperties.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(this.modelProperties);
  }

  /**
   * Get the {@link MetaschemaAssembly} annotation associated with this class. This annotation
   * provides information used by this class binding to control binding behavior.
   * 
   * @return the annotation
   */
  protected MetaschemaAssembly getMetaschemaAssemblyAnnotation() {
    return metaschemaAssembly;
  }

  @Override
  public String getJsonRootName() {
    QName qname = getXmlRootQName();
    return qname != null ? qname.getLocalPart() : null;
  }

  @Override
  public QName getXmlRootQName() {
    return xmlRootQName;
  }

  @Override
  public List<ModelProperty> getModelProperties() {
    return modelProperties;
  }

  @Override
  public Map<String, ? extends NamedProperty> getProperties(Predicate<FlagProperty> flagFilter) {
    return Stream.concat(
        super.getProperties(flagFilter).values().stream(), getModelProperties().stream())
        .collect(Collectors.toMap(NamedProperty::getJsonPropertyName, Function.identity()));
  }

  @Override
  public Object readRoot(XmlParsingContext context) throws XMLStreamException, BindingException, IOException {

    QName rootQName = getXmlRootQName();
    XMLEventReader2 eventReader = context.getReader();

    if (eventReader.peek().isStartDocument()) {
      eventReader.next();
    }

    XmlEventUtil.skipWhitespace(eventReader);

    XmlEventUtil.assertNext(eventReader, XMLEvent.START_ELEMENT, rootQName);

    Object instance = newInstance();

    callBeforeDeserialize(instance, null);

    StartElement rootStart = eventReader.nextEvent().asStartElement();
    readInternal(null, instance, rootStart, context);

    XmlEventUtil.consumeAndAssert(eventReader, XMLEvent.END_ELEMENT, rootQName);

    XmlEventUtil.consumeAndAssert(eventReader, XMLEvent.END_DOCUMENT);

    callAfterDeserialize(instance, null);

    return instance;
  }

  @Override
  public Object readRoot(JsonParsingContext context) throws BindingException, IOException {
    String[] ignoreFieldsArray = getMetaschemaAssemblyAnnotation().ignoreRootJsonProperties();

    Set<String> ignoreRootFields;
    if (ignoreFieldsArray == null || ignoreFieldsArray.length == 0) {
      ignoreRootFields = Collections.emptySet();
    } else {
      ignoreRootFields = new HashSet<>(Arrays.asList(ignoreFieldsArray));
    }

    String rootFieldName = getJsonRootName();
    JsonToken token;
    JsonParser parser = context.getReader();

    // first read the initial START_OBJECT
    JsonUtil.consumeAndAssert(parser, JsonToken.START_OBJECT);

    Object instance = newInstance();

    callBeforeDeserialize(instance, null);

    boolean foundRoot = false;
    while (!JsonToken.END_OBJECT.equals(token = parser.nextToken())) {
      if (!JsonToken.FIELD_NAME.equals(token)) {
        throw new BindingException(String.format("Expected FIELD_NAME token, found '%s'", token.toString()));
      }

      String fieldName = parser.currentName();
      if (fieldName.equals(rootFieldName)) {
        foundRoot = true;
        // process the object value, bound to the requested class
        JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);
        JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);
        readInternal(null, instance, context);

      } else if (ignoreRootFields.contains(fieldName)) {
        // ignore the field
        JsonUtil.skipNextValue(parser);
      } else {
        if (!context.getProblemHandler().handleUnknownRootProperty(instance, this,
            fieldName, context)) {
          logger.warn("Skipping unhandled top-level JSON field '{}'.", fieldName);
          JsonUtil.skipNextValue(parser);
        }
      }
    }

    JsonUtil.assertCurrent(parser, JsonToken.END_OBJECT);

    // advance past the end object
    parser.nextToken();

    if (!foundRoot) {
      throw new BindingException(String.format("Failed to find root field '%s'.", rootFieldName));
    }

    callAfterDeserialize(instance, null);

    return instance;
  }

  @Override
  protected void readBody(Object instance, StartElement start, XmlParsingContext context)
      throws IOException, XMLStreamException, BindingException {
    Set<ModelProperty> unhandledProperties = new HashSet<>();
    for (ModelProperty modelProperty : getModelProperties()) {
      if (!modelProperty.read(instance, start, context)) {
        unhandledProperties.add(modelProperty);
      }
    }

    // process all properties that did not get a value
    for (ModelProperty property : unhandledProperties) {
      // use the default value of the collector
      property.setValue(instance, property.newPropertyCollector().getValue());

    }
  }

  @Override
  public boolean readItem(PropertyCollector collector, Object parentInstance, JsonParsingContext context)
      throws IOException, BindingException {

    Object instance = newInstance();

    callBeforeDeserialize(instance, parentInstance);

    readInternal(parentInstance, instance, context);

    callAfterDeserialize(instance, parentInstance);

    collector.add(instance);
    return true;
  }

  protected void readInternal(Object parentInstance, Object instance, JsonParsingContext context)
      throws BindingException, IOException {
    JsonParser jsonParser = context.getReader();

    JsonUtil.assertCurrent(jsonParser, JsonToken.FIELD_NAME);

    FlagProperty jsonKey = getJsonKey();
    Map<String, ? extends NamedProperty> properties;
    if (jsonKey != null) {
      properties = getProperties((flag) -> {
        return !jsonKey.equals(flag);
      });

      // if there is a json key, the first field will be the key
      String key = jsonParser.getCurrentName();
      // advance past the FIELD_NAME
      jsonParser.nextToken();
      Object value = jsonKey.readValueFromString(key);
      jsonKey.setValue(instance, value.toString());

      // next the value will be a start object
      JsonUtil.assertAndAdvance(jsonParser, JsonToken.START_OBJECT);
    } else {
      properties = getProperties(null);
    }

    Set<String> handledProperties = new HashSet<>();
    while (!jsonParser.hasTokenId(JsonToken.END_OBJECT.id())) {
      String propertyName = jsonParser.getCurrentName();
      NamedProperty property = properties.get(propertyName);

      boolean handled = false;
      if (property != null) {
        handled = property.read(instance, context);
      }

      if (handled) {
        handledProperties.add(propertyName);
      } else {
        logger.warn("Unrecognized property named '{}' at '{}'", propertyName,
            JsonUtil.toString(jsonParser.getCurrentLocation()));
        JsonUtil.skipNextValue(jsonParser);
      }
    }

    // set undefined properties
    for (Map.Entry<String, ? extends NamedProperty> entry : properties.entrySet()) {
      if (!handledProperties.contains(entry.getKey())) {
        // use the default value of the collector
        NamedProperty property = entry.getValue();
        property.setValue(instance, property.newPropertyCollector().getValue());
      }
    }

    if (jsonKey != null) {
      // read the END_OBJECT for the JSON key value
      JsonUtil.assertAndAdvance(jsonParser, JsonToken.END_OBJECT);
    }
  }

  @Override
  public void writeRoot(Object instance, XmlWritingContext context) throws XMLStreamException, IOException {

    XMLStreamWriter2 writer = context.getWriter();

    writer.writeStartDocument("UTF-8", "1.0");

    QName rootQName = getXmlRootQName();

    NamespaceContext nsContext = writer.getNamespaceContext();
    String prefix = nsContext.getPrefix(rootQName.getNamespaceURI());
    if (prefix == null) {
      prefix = "";
    }

    writer.writeStartElement(prefix, rootQName.getLocalPart(), rootQName.getNamespaceURI());

    writeItem(instance, rootQName, context);

    writer.writeEndElement();

  }

  @Override
  public void writeRoot(Object instance, JsonWritingContext context) throws IOException {

    JsonGenerator writer = context.getWriter();

    // first read the initial START_OBJECT
    writer.writeStartObject();

    writer.writeFieldName(getJsonRootName());

    writeInternal(instance, true, context);

    // end of root object
    writer.writeEndObject();
  }

  /**
   * 
   * @param instance
   * @param writeObjectWrapper 
   * @param context
   * @throws IOException
   * @throws NullPointerException
   *           if there is a JSON key configured and the key property's value is {@code null}
   */
  protected void writeInternal(Object instance, boolean writeObjectWrapper, JsonWritingContext context) throws IOException {
    JsonGenerator writer = context.getWriter();

    if (writeObjectWrapper) {
      writer.writeStartObject();
    }

    FlagProperty jsonKey = getJsonKey();
    Map<String, ? extends NamedProperty> properties;
    if (jsonKey != null) {
      properties = getProperties((flag) -> {
        return !jsonKey.equals(flag);
      });

      // if there is a json key, the first field will be the key
      String key = jsonKey.getValueAsString(instance);
      if (key == null) {
        throw new NullPointerException("Null key value");
      }
      writer.writeFieldName(key);

      // next the value will be a start object
      writer.writeStartObject();
    } else {
      properties = getProperties(null);
    }

    for (NamedProperty property : properties.values()) {
      property.write(instance, context);
    }

    if (jsonKey != null) {
      // write the END_OBJECT for the JSON key value
      writer.writeEndObject();
    }

    if (writeObjectWrapper) {
      writer.writeEndObject();
    }
  }

  @Override
  protected void writeBody(Object instance, QName parentName, XmlWritingContext context)
      throws XMLStreamException, IOException {
    for (ModelProperty modelProperty : getModelProperties()) {
      modelProperty.write(instance, parentName, context);
    }
  }

  @Override
  public void writeItems(Collection<? extends Object> items, boolean writeObjectWrapper, JsonWritingContext context)
      throws IOException {
    for (Object item : items) {
      writeInternal(item, writeObjectWrapper, context);
    }
  }
}
