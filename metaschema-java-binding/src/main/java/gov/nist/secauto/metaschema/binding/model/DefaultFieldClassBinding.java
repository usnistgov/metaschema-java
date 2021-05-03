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
import gov.nist.secauto.metaschema.binding.io.json.CollapseKeyBuilder;
import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonUtil;
import gov.nist.secauto.metaschema.binding.io.json.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.annotations.Field;
import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.binding.model.property.DefaultFieldValueProperty;
import gov.nist.secauto.metaschema.binding.model.property.FieldValueProperty;
import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.NamedProperty;
import gov.nist.secauto.metaschema.binding.model.property.Property;
import gov.nist.secauto.metaschema.binding.model.property.info.ListPropertyCollector;
import gov.nist.secauto.metaschema.binding.model.property.info.PropertyCollector;
import gov.nist.secauto.metaschema.datatypes.util.XmlEventUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public class DefaultFieldClassBinding
    extends AbstractClassBinding
    implements FieldClassBinding {
  private static final Logger logger = LogManager.getLogger(DefaultFieldClassBinding.class);

  /**
   * Create a new {@link ClassBinding} for a Java bean annotated with the {@link Field} annotation.
   * 
   * @param clazz
   *          the Java bean class
   * @param bindingContext
   *          the Metaschema binding environment context
   * @return the Metaschema field binding for the class
   */
  public static DefaultFieldClassBinding createInstance(Class<?> clazz, BindingContext bindingContext) {
    Objects.requireNonNull(clazz, "clazz");
    if (!clazz.isAnnotationPresent(MetaschemaField.class)) {
      throw new IllegalArgumentException(
          String.format("Class '%s' is missing the '%' annotation.", clazz.getName(), MetaschemaField.class.getName()));
    }
    DefaultFieldClassBinding retval = new DefaultFieldClassBinding(clazz, bindingContext);
    retval.initialize();
    return retval;
  }

  private final MetaschemaField metaschemaField;
  private FieldValueProperty fieldValue;
  private FlagProperty jsonValueKey;

  /**
   * Construct a new {@link ClassBinding} for a Java bean annotated with the {@link Field} annotation.
   * 
   * @param clazz
   *          the Java bean class
   * @param bindingContext
   *          the class binding context for which this class is participating
   */
  protected DefaultFieldClassBinding(Class<?> clazz, BindingContext bindingContext) {
    super(clazz, bindingContext);
    this.metaschemaField = clazz.getAnnotation(MetaschemaField.class);
  }

  @Override
  protected void initialize() {
    super.initialize();

    if (fieldValue == null) {
      throw new IllegalArgumentException(String.format("Class '%s' is missing the '%' annotation on one of its fields.",
          getBoundClass().getName(), FieldValue.class.getName()));
    }
  }

  @Override
  protected boolean initializeField(java.lang.reflect.Field field) {
    boolean handled = false;
    FieldValue fieldValueAnnotation = field.getAnnotation(FieldValue.class);
    if (fieldValueAnnotation != null) {
      this.fieldValue = new DefaultFieldValueProperty(this, field);
      handled = true;
    }
    return handled;
  }

  @Override
  protected void initializeFlag(FlagProperty flag) {
    if (flag.isJsonValueKey()) {
      this.jsonValueKey = flag;
    }
  }

  protected MetaschemaField getMetaschemaFieldAnnotation() {
    return metaschemaField;
  }

  @Override
  public FieldValueProperty getFieldValue() {
    return fieldValue;
  }

  @Override
  public FlagProperty getJsonValueKeyFlag() {
    return jsonValueKey;
  }

  @Override
  public Map<String, ? extends NamedProperty> getProperties(Predicate<FlagProperty> flagFilter) {
    FieldValueProperty fieldValue = getFieldValue();
    String valuePropertyName = fieldValue.getJsonPropertyName();
    Map<String, ? extends NamedProperty> retval;
    if (valuePropertyName != null) {
      retval = Stream.concat(super.getProperties(flagFilter).values().stream(), Stream.of(fieldValue))
          .collect(Collectors.toMap(Property::getJsonPropertyName, Function.identity()));
    } else {
      retval = super.getProperties(flagFilter);
    }
    return retval;
  }

  @Override
  public boolean isCollapsible() {
    return getMetaschemaFieldAnnotation().isCollapsible();
  }

  @Override
  protected void readBody(Object instance, StartElement start, XmlParsingContext context)
      throws IOException, XMLStreamException, BindingException {
    if (!getFieldValue().read(instance, start, context)) {
      throw new IOException(String.format("Missing field value at '%s", XmlEventUtil.toLocation(start)));
    }
  }

  @Override
  public boolean readItem(PropertyCollector collector, Object parentInstance, JsonParsingContext context)
      throws IOException, BindingException {
    boolean handled;
    if (isCollapsible()) {
      handled = readCollapsed(collector, parentInstance, context);
    } else {
      handled = readNormal(collector, parentInstance, context);
    }
    return handled;
  }

  private boolean readNormal(PropertyCollector collector, Object parentInstance, JsonParsingContext context)
      throws IOException, BindingException {
    Predicate<FlagProperty> flagFilter = null;

    FlagProperty jsonKey = getJsonKey();
    if (jsonKey != null) {
      flagFilter = (flag) -> {
        return !jsonKey.equals(flag);
      };
    }

    FlagProperty jsonValueKey = getJsonValueKeyFlag();
    if (jsonValueKey != null) {
      if (flagFilter == null) {
        flagFilter = (flag) -> {
          return !jsonValueKey.equals(flag);
        };
      } else {
        flagFilter = flagFilter.and((flag) -> {
          return !jsonValueKey.equals(flag);
        });
      }
    }

    Map<String, ? extends NamedProperty> properties = getProperties(flagFilter);

    Object instance = newInstance();

    callBeforeDeserialize(instance, parentInstance);

    JsonParser jsonParser = context.getReader();
    if (jsonKey != null) {
      // if there is a json key, the first field will be the key
      String key = jsonParser.nextFieldName();
      jsonKey.setValue(instance, jsonKey.readValueFromString(key));

      // next the value will be a start object
      JsonUtil.consumeAndAssert(jsonParser, JsonToken.START_OBJECT);
    }

    boolean parsedValue = false;
    Set<String> handledProperties = new HashSet<>();
    // now parse each property until the end object is reached
    while (!jsonParser.hasTokenId(JsonToken.END_OBJECT.id())) {
      String propertyName = jsonParser.getCurrentName();
      NamedProperty property = properties.get(propertyName);

      boolean handled = false;
      if (property != null) {
        if (property instanceof FieldValueProperty && !parsedValue) {
          parsedValue = true;
        }

        PropertyCollector propertyCollector = property.newPropertyCollector();
        Object value = property.readValue(propertyCollector, parentInstance, context);
        if (value != null) {
          property.setValue(instance, propertyCollector.getValue());
          handled = true;
        }
      } else if (jsonValueKey != null && !parsedValue) {
        String key = jsonParser.nextFieldName();
        jsonValueKey.setValue(instance, jsonValueKey.readValueFromString(key));

        PropertyCollector propertyCollector = fieldValue.newPropertyCollector();
        Object value = fieldValue.readValue(propertyCollector, parentInstance, context);
        if (value != null) {
          fieldValue.setValue(instance, propertyCollector.getValue());
          handled = true;
        }
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
      JsonUtil.consumeAndAssert(jsonParser, JsonToken.END_OBJECT);
    } else {
      JsonUtil.assertCurrent(jsonParser, JsonToken.END_OBJECT);
    }

    // // read the END_OBJECT for the field
    // JsonUtil.consumeAndAssert(jsonParser, JsonToken.END_OBJECT);

    callAfterDeserialize(instance, parentInstance);
    collector.add(instance);

    return true;
  }

  private boolean readCollapsed(PropertyCollector collector, Object parentInstance, JsonParsingContext context)
      throws IOException, BindingException {

    Predicate<FlagProperty> flagFilter = null;

    FlagProperty jsonKey = getJsonKey();
    if (jsonKey != null) {
      flagFilter = (flag) -> {
        return !jsonKey.equals(flag);
      };
    }

    FlagProperty jsonValueKey = getJsonValueKeyFlag();
    if (jsonValueKey != null) {
      if (flagFilter == null) {
        flagFilter = (flag) -> {
          return !jsonValueKey.equals(flag);
        };
      } else {
        flagFilter = flagFilter.and((flag) -> {
          return !jsonValueKey.equals(flag);
        });
      }
    }

    Map<String, ? extends NamedProperty> properties = getProperties(flagFilter);

    Map<NamedProperty, Supplier<? extends Object>> parsedProperties = new HashMap<>();
    JsonParser jsonParser = context.getReader();

    // JsonUtil.assertAndAdvance(jsonParser, JsonToken.START_OBJECT);

    if (jsonKey != null) {
      // if there is a json key, the first field will be the key
      String key = jsonParser.currentName();
      Supplier<? extends Object> jsonKeySupplier = jsonKey.readValueAndSupply(key);
      parsedProperties.put(jsonKey, jsonKeySupplier);

      // next the value will be a start object
      JsonUtil.consumeAndAssert(jsonParser, JsonToken.START_OBJECT);
      // advance to the next field name
      jsonParser.nextToken();
    }

    List<? extends Object> values = null;
    Set<String> handledProperties = new HashSet<>();
    // now parse each property until the end object is reached
    while (!jsonParser.hasTokenId(JsonToken.END_OBJECT.id())) {
      String propertyName = jsonParser.getCurrentName();
      // advance past the field name
      jsonParser.nextToken();

      NamedProperty property = properties.get(propertyName);

      boolean handled = false;
      if (property != null) {
        if (property instanceof FieldValueProperty && values == null) {
          values = handleCollapsedValues(parentInstance, context);
          handled = true;
        } else if (property instanceof FlagProperty) {
          Supplier<? extends Object> supplier = ((FlagProperty) property).readValueAndSupply(context);
          parsedProperties.put(property, supplier);
          handled = true;
        } else {
          throw new BindingException(String.format("non-flag property found '%s' at '%s'", propertyName,
              JsonUtil.toString(jsonParser.getCurrentLocation())));
        }
      } else if (jsonValueKey != null && values == null) {
        String key = jsonParser.nextFieldName();
        Supplier<? extends Object> supplier = jsonValueKey.readValueAndSupply(key);
        parsedProperties.put(jsonValueKey, supplier);

        values = handleCollapsedValues(parentInstance, context);
        handled = true;
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
        parsedProperties.put(property, () -> {
          return property.newPropertyCollector().getValue();
        });
      }
    }

    if (jsonKey != null) {
      // read the END_OBJECT for the JSON key value
      JsonUtil.assertAndAdvance(jsonParser, JsonToken.END_OBJECT);
    }

    // // read the END_OBJECT for the field
    // JsonUtil.assertAndAdvance(jsonParser, JsonToken.END_OBJECT);

    // now we need to clone one item per value
    // TODO: handle the case where there are no values
    if (values == null) {
      Object item = newInstance();

      callBeforeDeserialize(item, parentInstance);

      for (Map.Entry<NamedProperty, Supplier<? extends Object>> entry : parsedProperties.entrySet()) {
        NamedProperty property = entry.getKey();
        Supplier<? extends Object> supplier = entry.getValue();

        property.setValue(item, supplier.get());
      }

      callAfterDeserialize(item, parentInstance);

      collector.add(item);
    } else {
      for (Object value : values) {
        Object item = newInstance();

        callBeforeDeserialize(item, parentInstance);

        fieldValue.setValue(item, value);

        for (Map.Entry<NamedProperty, Supplier<? extends Object>> entry : parsedProperties.entrySet()) {
          NamedProperty property = entry.getKey();
          Supplier<? extends Object> supplier = entry.getValue();

          property.setValue(item, supplier.get());
        }

        callAfterDeserialize(item, parentInstance);

        collector.add(item);
      }
    }
    return true;
  }

  private List<? extends Object> handleCollapsedValues(Object parentInstance, JsonParsingContext context)
      throws IOException, BindingException {
    FieldValueProperty fieldValue = getFieldValue();

    JsonParser jsonParser = context.getReader();

    ListPropertyCollector collector = new ListPropertyCollector();
    if (jsonParser.hasToken(JsonToken.START_ARRAY)) {
      while (!jsonParser.hasToken(JsonToken.END_ARRAY)) {
        fieldValue.readValue(collector, parentInstance, context);
      }
    } else {
      fieldValue.readValue(collector, parentInstance, context);
    }
    return collector.getValue();
  }

  @Override
  protected void writeBody(Object instance, QName parentName, XmlWritingContext context)
      throws XMLStreamException, IOException {
    getFieldValue().write(instance, parentName, context);
  }

  @Override
  public void writeItems(Collection<? extends Object> items, boolean writeObjectWrapper, JsonWritingContext context)
      throws IOException {
    if (isCollapsible()) {
      writeCollapsed(items, writeObjectWrapper, context);
    } else {
      writeNormal(items, writeObjectWrapper, context);
    }
  }

  private void writeCollapsed(Collection<? extends Object> items, boolean writeObjectWrapper,
      JsonWritingContext context) throws IOException {
    CollapseKeyBuilder builder = new CollapseKeyBuilder(this);
    builder.addAll(items);

    builder.write(writeObjectWrapper, context);
  }

  private void writeNormal(Collection<? extends Object> items, boolean writeObjectWrapper, JsonWritingContext context)
      throws IOException {
    if (items.isEmpty()) {
      return;
    }

    Predicate<FlagProperty> flagFilter = null;

    FlagProperty jsonKey = getJsonKey();
    if (jsonKey != null) {
      flagFilter = (flag) -> {
        return !jsonKey.equals(flag);
      };
    }

    FlagProperty jsonValueKey = getJsonValueKeyFlag();
    if (jsonValueKey != null) {
      if (flagFilter == null) {
        flagFilter = (flag) -> {
          return !jsonValueKey.equals(flag);
        };
      } else {
        flagFilter = flagFilter.and((flag) -> {
          return !jsonValueKey.equals(flag);
        });
      }
    }

    Map<String, ? extends NamedProperty> properties = getProperties(flagFilter);

    JsonGenerator writer = context.getWriter();

    for (Object item : items) {
      if (writeObjectWrapper) {
        writer.writeStartObject();
      }

      if (jsonKey != null) {
        // if there is a json key, the first field will be the key
        String key = jsonKey.getValueAsString(item);
        if (key == null) {
          throw new NullPointerException("Null key value");
        }
        writer.writeFieldName(key);

        // next the value will be a start object
        writer.writeStartObject();
      }

      for (NamedProperty property : properties.values()) {
        property.write(item, context);
      }

      Object fieldValue = getFieldValue().getValue(item);
      if (fieldValue != null) {
        String valueKeyName;
        if (jsonValueKey != null) {
          valueKeyName = jsonValueKey.getValueAsString(jsonValueKey.getValue(item));
        } else {
          valueKeyName = getFieldValue().getJsonPropertyName();
        }
        writer.writeFieldName(valueKeyName);
        getFieldValue().writeValue(fieldValue, context);
      }

      if (jsonKey != null) {
        writer.writeEndObject();
      }

      if (writeObjectWrapper) {
        writer.writeEndObject();
      }
    }
  }

}
