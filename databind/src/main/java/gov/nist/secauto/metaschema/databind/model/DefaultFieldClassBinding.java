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

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IMetaschema;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.InternalModelSource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstraintSupport;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.CollapseKeyBuilder;
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.json.JsonUtil;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.Ignore;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaFieldValue;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.talsmasoftware.lazy4j.Lazy;

@SuppressWarnings("PMD.GodClass")
public class DefaultFieldClassBinding
    extends AbstractClassBinding
    implements IFieldClassBinding, IValueConstraintFeature {
  private static final Logger LOGGER = LogManager.getLogger(DefaultFieldClassBinding.class);

  @NonNull
  private final MetaschemaField metaschemaField;
  private IBoundFieldValueInstance fieldValue;
  private IBoundFlagInstance jsonValueKeyFlagInstance;
  private final Lazy<IValueConstraintSupport> constraints;

  /**
   * Create a new {@link IClassBinding} for a Java bean annotated with the {@link BoundField}
   * annotation.
   *
   * @param clazz
   *          the Java bean class
   * @param bindingContext
   *          the Metaschema binding environment context
   * @return the Metaschema field binding for the class
   */
  @NonNull
  public static DefaultFieldClassBinding createInstance(@NonNull Class<?> clazz,
      @NonNull IBindingContext bindingContext) {
    Objects.requireNonNull(clazz, "clazz");
    if (!clazz.isAnnotationPresent(MetaschemaField.class)) {
      throw new IllegalArgumentException(
          String.format("Class '%s' is missing the '%s' annotation.",
              clazz.getName(),
              MetaschemaField.class.getName()));
    }
    return new DefaultFieldClassBinding(clazz, bindingContext);
  }

  /**
   * Construct a new {@link IClassBinding} for a Java bean annotated with the {@link BoundField}
   * annotation.
   *
   * @param clazz
   *          the Java bean class
   * @param bindingContext
   *          the class binding context for which this class is participating
   */
  protected DefaultFieldClassBinding(@NonNull Class<?> clazz, @NonNull IBindingContext bindingContext) {
    super(clazz, bindingContext);
    this.metaschemaField = ObjectUtils.notNull(clazz.getAnnotation(MetaschemaField.class));
    this.constraints = Lazy.lazy(() -> new ValueConstraintSupport(
        clazz.getAnnotation(ValueConstraints.class),
        InternalModelSource.instance()));
  }

  @NonNull
  public MetaschemaField getMetaschemaFieldAnnotation() {
    return metaschemaField;
  }

  @Override
  public IValueConstraintSupport getConstraintSupport() {
    return constraints.get();
  }

  @Override
  public String getFormalName() {
    return ModelUtil.resolveToString(getMetaschemaFieldAnnotation().formalName());
  }

  @Override
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getMetaschemaFieldAnnotation().description());
  }

  @Override
  public @Nullable MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getMetaschemaFieldAnnotation().description());
  }

  @Override
  public String getName() {
    return getMetaschemaFieldAnnotation().name();
  }

  @Override
  public Object getDefaultValue() {
    return getFieldValueInstance().getDefaultValue();
  }

  /**
   * Collect all fields that are part of the model for this class.
   *
   * @param clazz
   *          the class
   * @return the field value instances if found or {@code null} otherwise
   */
  protected java.lang.reflect.Field getFieldValueField(Class<?> clazz) {
    java.lang.reflect.Field[] fields = clazz.getDeclaredFields();

    java.lang.reflect.Field retval = null;

    Class<?> superClass = clazz.getSuperclass();
    if (superClass != null) {
      // get instances from superclass
      retval = getFieldValueField(superClass);
    }

    if (retval == null) {
      for (java.lang.reflect.Field field : fields) {
        if (!field.isAnnotationPresent(MetaschemaFieldValue.class)) {
          // skip fields that aren't a field or assembly instance
          continue;
        }

        if (field.isAnnotationPresent(Ignore.class)) {
          // skip this field, since it is ignored
          continue;
        }
        retval = field;
      }
    }
    return retval;
  }

  /**
   * Initialize the flag instances for this class.
   *
   * @return the field value instance
   */
  protected IBoundFieldValueInstance initalizeFieldValueInstance() {
    synchronized (this) {
      if (this.fieldValue == null) {
        java.lang.reflect.Field field = getFieldValueField(getBoundClass());
        if (field == null) {
          throw new IllegalArgumentException(
              String.format("Class '%s' is missing the '%s' annotation on one of its fields.",
                  getBoundClass().getName(),
                  MetaschemaFieldValue.class.getName()));
        }

        this.fieldValue = new DefaultFieldValueProperty(this, field);
      }
      return this.fieldValue;
    }
  }

  @Override
  public boolean isInline() {
    return false;
  }

  @Override
  public IBoundFieldInstance getInlineInstance() {
    return null;
  }

  @SuppressWarnings("null")
  @Override
  public IBoundFieldValueInstance getFieldValueInstance() {
    return initalizeFieldValueInstance();
  }

  @Override
  public Object getFieldValue(@NonNull Object item) {
    return ObjectUtils.requireNonNull(getFieldValueInstance().getValue(item));
  }

  @Override
  protected void initializeFlagInstance(IBoundFlagInstance instance) {
    super.initializeFlagInstance(instance);

    if (instance.isJsonValueKey()) {
      this.jsonValueKeyFlagInstance = instance;
    }
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "access is restricted using interface")
  public IBoundFlagInstance getJsonValueKeyFlagInstance() {
    initalizeFlagInstances();
    return jsonValueKeyFlagInstance;
  }

  @Override
  public String getJsonValueKeyName() {
    return getFieldValueInstance().getJsonValueKeyName();
  }

  // @Override
  // public Map<String, ? extends IBoundNamedInstance> getNamedInstances(Predicate<IBoundFlagInstance>
  // flagFilter)
  // {
  //// IBoundFieldValueInstance fieldValue = getFieldValue();
  //// String valuePropertyName = fieldValue.getJsonName();
  // Map<String, ? extends IBoundNamedInstance> retval;
  // if (valuePropertyName != null) {
  //// retval = Stream.concat(super.getNamedInstances(flagFilter).values().stream(),
  // Stream.of(fieldValue))
  //// .collect(Collectors.toMap(IBoundNamedInstance::getJsonName, FunctionCall.identity()));
  // retval = super.getNamedInstances(flagFilter).values().stream()
  // .collect(Collectors.toMap(IBoundNamedInstance::getJsonName, FunctionCall.identity()));
  // } else {
  // retval = super.getNamedInstances(flagFilter);
  // }
  // return retval;
  // }

  @Override
  public boolean isCollapsible() {
    return getMetaschemaFieldAnnotation().isCollapsible();
  }

  @SuppressWarnings("resource") // not owned
  @Override
  public List<Object> readItem(Object parentInstance, boolean requiresJsonKey, IJsonParsingContext context)
      throws IOException {
    JsonParser jsonParser = context.getReader(); // NOPMD - intentional

    if (requiresJsonKey) {
      // the start object has already been parsed, the next field name is the JSON key
      JsonUtil.assertCurrent(jsonParser, JsonToken.FIELD_NAME);
    } else {
      // JsonUtil.assertAndAdvance(jsonParser, JsonToken.START_OBJECT);
      // This could be an empty assembly signified by a END_OBJECT, or a series of properties signified by
      // a FIELD_NAME
      JsonUtil.assertCurrent(jsonParser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
    }

    List<Object> retval;
    if (isCollapsible()) {
      retval = readCollapsed(parentInstance, requiresJsonKey, context);
    } else {
      retval = CollectionUtil.singletonList(readNormal(parentInstance, requiresJsonKey, context));
    }

    // if (!requiresJsonKey) {
    // JsonUtil.assertAndAdvance(jsonParser, JsonToken.END_OBJECT);
    // }
    return retval;
  }

  @SuppressWarnings("resource") // not owned
  @NonNull
  private Object readNormal(@Nullable Object parentInstance, boolean requiresJsonKey,
      @NonNull IJsonParsingContext context)
      throws IOException {
    Predicate<IBoundFlagInstance> flagFilter = null;

    IBoundFlagInstance jsonKey = null;
    if (requiresJsonKey) {
      IBoundFlagInstance instance = getJsonKeyFlagInstance();
      if (instance == null) {
        throw new IOException("This property is configured to use a JSON key, but no JSON key was found");
      }

      flagFilter = (flag) -> {
        return !instance.equals(flag);
      };
      jsonKey = instance;
    }

    IBoundFlagInstance jsonValueKey = getJsonValueKeyFlagInstance();
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

    Map<String, ? extends IBoundNamedInstance> properties = getNamedInstances(flagFilter);

    try {
      Object instance = newInstance();

      callBeforeDeserialize(instance, parentInstance);

      JsonParser jsonParser = context.getReader(); // NOPMD - intentional

      if (jsonKey != null) {
        // if there is a json key, the first field will be the key
        String key = jsonParser.currentName();
        assert key != null;
        jsonKey.setValue(instance, jsonKey.readValueFromString(key));

        // advance past the field name
        if (properties.isEmpty()) {
          // the value will be a standard value
          // advance past the field name
          JsonUtil.assertAndAdvance(jsonParser, JsonToken.FIELD_NAME);
        } else {
          // the value will be a start object
          JsonUtil.advanceAndAssert(jsonParser, JsonToken.START_OBJECT);
          // advance past the start object
          jsonParser.nextToken();
        }
      }

      Set<String> handledProperties = new HashSet<>();
      if (properties.isEmpty()) {
        // this may be a value key value, an unrecognized flag, or the field value
        IBoundFieldValueInstance fieldValue = getFieldValueInstance();
        Object value = fieldValue.readValue(context);
        if (value != null) {
          fieldValue.setValue(instance, value);
        }
        handledProperties.add(fieldValue.getJsonValueKeyName());
      } else {
        // This could be an empty assembly signified by a END_OBJECT, or a series of properties signified by
        // a FIELD_NAME
        JsonUtil.assertCurrent(jsonParser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);

        boolean parsedValueKey = false;
        // now parse each property until the end object is reached
        while (!jsonParser.hasTokenId(JsonToken.END_OBJECT.id())) {
          String propertyName = jsonParser.getCurrentName();
          assert propertyName != null;
          // JsonUtil.assertAndAdvance(jsonParser, JsonToken.FIELD_NAME);

          IBoundNamedInstance namedProperty = properties.get(propertyName);

          boolean handled = false;
          if (namedProperty != null) {
            // this is a recognized flag

            if (namedProperty.equals(jsonValueKey)) {
              throw new IOException(
                  String.format("JSON value key configured, but found standard flag for the value key '%s'",
                      namedProperty.toCoordinates()));
            }

            // Now parse
            Object value = namedProperty.read(context);
            if (value != null) {
              namedProperty.setValue(instance, value);
              handled = true;
            }
          }

          if (namedProperty == null && !parsedValueKey) {
            // this may be a value key value, an unrecognized flag, or the field value
            parsedValueKey = getFieldValueInstance().read(instance, context);

            if (parsedValueKey) {
              handled = true;
            } else {
              if (context.getProblemHandler().canHandleUnknownProperty(this, propertyName, context)) {
                handled = context.getProblemHandler().handleUnknownProperty(this, propertyName, context);
              }
            }
          }

          if (handled) {
            handledProperties.add(propertyName);
          } else {
            if (LOGGER.isWarnEnabled()) {
              LOGGER.warn("Unrecognized property named '{}' at '{}'", propertyName,
                  JsonUtil.toString(ObjectUtils.notNull(jsonParser.getCurrentLocation())));
            }
            JsonUtil.skipNextValue(jsonParser);
          }
        }
      }

      // set undefined properties
      for (Map.Entry<String, ? extends IBoundNamedInstance> entry : properties.entrySet()) {
        if (!handledProperties.contains(entry.getKey())) {
          IBoundNamedInstance property = ObjectUtils.notNull(entry.getValue());
          // use the default value of the collector
          property.setValue(instance, property.newPropertyCollector().getValue());
        }

      }

      if (jsonKey != null && !properties.isEmpty()) {
        // read the END_OBJECT for the JSON key value
        JsonUtil.assertAndAdvance(jsonParser, JsonToken.END_OBJECT);
      }

      if (properties.isEmpty()) {
        // this is the next field or the end of the containing object of this field
        JsonUtil.assertCurrent(jsonParser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
      } else {
        // this is the current end element, but we are not responsible for parsing it.
        JsonUtil.assertCurrent(jsonParser, JsonToken.END_OBJECT);
      }

      callAfterDeserialize(instance, parentInstance);
      return instance;
    } catch (BindingException ex) {
      throw new IOException(ex);
    }
  }

  @SuppressWarnings("resource") // not owned
  @NonNull
  private List<Object> readCollapsed(@Nullable Object parentInstance, boolean requiresJsonKey,
      IJsonParsingContext context) throws IOException {

    Predicate<IBoundFlagInstance> flagFilter = null;

    IBoundFlagInstance jsonKey = null;
    if (requiresJsonKey) {
      IBoundFlagInstance instance = getJsonKeyFlagInstance();
      if (instance == null) {
        throw new IOException("This property is configured to use a JSON key, but no JSON key was found");
      }

      flagFilter = (flag) -> {
        return !instance.equals(flag);
      };
      jsonKey = instance;
    }

    IBoundFlagInstance jsonValueKey = getJsonValueKeyFlagInstance();
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

    Map<String, ? extends IBoundNamedInstance> properties = getNamedInstances(flagFilter);

    Map<IBoundNamedInstance, Supplier<? extends Object>> parsedProperties = new HashMap<>(); // NOPMD - intentional

    JsonParser jsonParser = context.getReader(); // NOPMD - intentional
    // This could be an empty assembly signified by a END_OBJECT, or a series of properties signified by
    // a FIELD_NAME
    JsonUtil.assertCurrent(jsonParser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);

    if (jsonKey != null) {
      // if there is a json key, the first field will be the key
      String key = jsonParser.currentName();
      assert key != null;
      Supplier<? extends Object> jsonKeySupplier = jsonKey.readValueAndSupply(key);
      parsedProperties.put(jsonKey, jsonKeySupplier);

      // next the value will be a start object
      JsonUtil.advanceAndAssert(jsonParser, JsonToken.START_OBJECT);
      // advance to the next field name
      jsonParser.nextToken();
    }

    List<? extends Object> values = null;
    Set<String> handledProperties = new HashSet<>();
    // now parse each property until the end object is reached
    while (!jsonParser.hasTokenId(JsonToken.END_OBJECT.id())) {
      String propertyName = jsonParser.getCurrentName();
      // // advance past the field name
      // jsonParser.nextToken();

      IBoundNamedInstance namedProperty = properties.get(propertyName);

      boolean handled = false;
      if (namedProperty != null) {
        // this is a recognized flag

        if (namedProperty.equals(jsonValueKey)) {
          throw new IOException(String.format(
              "JSON value key configured, but found standard flag for the value key '%s'",
              namedProperty.toCoordinates()));
        }

        // Now parse
        Supplier<? extends Object> supplier = ((IBoundFlagInstance) namedProperty).readValueAndSupply(context);
        parsedProperties.put(namedProperty, supplier);
        handled = true;
      } else {
        // this may be a value key value, an unrecognized flag, or the field value
        IBoundFieldValueInstance fieldValue = getFieldValueInstance();
        if (jsonValueKey != null) {
          // treat this as the value key
          String key = jsonParser.nextFieldName();
          assert key != null;
          Supplier<? extends Object> supplier = jsonValueKey.readValueAndSupply(key);
          parsedProperties.put(jsonValueKey, supplier);

          values = handleCollapsedValues(context);
          handled = true;
        } else {
          String valueKeyName = fieldValue.getJsonValueKeyName();
          if (propertyName.equals(valueKeyName)) {
            // advance past the field
            jsonParser.nextToken();
            // treat this as the field value
            values = handleCollapsedValues(context);
            handled = true;
          }
        }
      }

      if (handled) {
        handledProperties.add(propertyName);
      } else {
        if (LOGGER.isWarnEnabled()) {
          LOGGER.warn("Unrecognized property named '{}' at '{}'", propertyName,
              JsonUtil.toString(ObjectUtils.notNull(jsonParser.getCurrentLocation())));
        }
        JsonUtil.skipNextValue(jsonParser);
      }
    }

    // set undefined properties
    for (Map.Entry<String, ? extends IBoundNamedInstance> entry : properties.entrySet()) {
      if (!handledProperties.contains(entry.getKey())) {
        // use the default value of the collector
        IBoundNamedInstance property = ObjectUtils.notNull(entry.getValue());
        parsedProperties.put(property, () -> {
          return property.newPropertyCollector().getValue();
        });
      }
    }

    if (jsonKey != null) {
      // read the END_OBJECT for the JSON key value
      JsonUtil.assertAndAdvance(jsonParser, JsonToken.END_OBJECT);
    }

    // this is the current end element, but we are not responsible for parsing it.
    JsonUtil.assertCurrent(jsonParser, JsonToken.END_OBJECT);

    // now we need to clone one item per value
    // TODO: handle the case where there are no values
    List<Object> retval;
    if (values == null) {
      try {
        Object item = newInstance();

        callBeforeDeserialize(item, parentInstance);

        for (Map.Entry<IBoundNamedInstance, Supplier<? extends Object>> entry : parsedProperties.entrySet()) {
          IBoundNamedInstance property = entry.getKey();
          Supplier<? extends Object> supplier = entry.getValue();

          property.setValue(item, supplier.get());
        }

        callAfterDeserialize(item, parentInstance);

        retval = CollectionUtil.singletonList(item);
      } catch (BindingException ex) {
        throw new IOException(ex);
      }
    } else {
      List<Object> items = new ArrayList<>(values.size());
      for (Object value : values) {
        try {
          Object item = newInstance();

          callBeforeDeserialize(item, parentInstance);

          initalizeFieldValueInstance().setValue(item, value);

          for (Map.Entry<IBoundNamedInstance, Supplier<? extends Object>> entry : parsedProperties.entrySet()) {
            IBoundNamedInstance property = entry.getKey();
            Supplier<? extends Object> supplier = entry.getValue();

            property.setValue(item, supplier.get());
          }

          callAfterDeserialize(item, parentInstance);
          items.add(item);
        } catch (BindingException ex) {
          throw new IOException(ex);
        }
      }
      retval = items;
    }
    return retval;
  }

  @SuppressWarnings("resource") // not owned
  @NonNull
  private List<? extends Object> handleCollapsedValues(@NonNull IJsonParsingContext context)
      throws IOException {
    IBoundFieldValueInstance fieldValue = getFieldValueInstance();

    JsonParser jsonParser = context.getReader(); // NOPMD - intentional

    ListPropertyCollector collector = new ListPropertyCollector();
    if (jsonParser.hasToken(JsonToken.START_ARRAY)) {
      JsonUtil.assertAndAdvance(jsonParser, JsonToken.START_ARRAY);
      while (!jsonParser.hasToken(JsonToken.END_ARRAY)) {
        Object value = fieldValue.readValue(context);
        if (value != null) {
          collector.add(value);
        }
      }
      JsonUtil.assertAndAdvance(jsonParser, JsonToken.END_ARRAY);
    } else {
      Object value = fieldValue.readValue(context);
      if (value != null) {
        collector.add(value);
      }
    }

    return collector.getValue();
  }

  @Override
  protected void writeBody(Object instance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    getFieldValueInstance().write(instance, parentName, context);
  }

  @Override
  public void writeItems(Collection<? extends Object> items, boolean writeObjectWrapper, IJsonWritingContext context)
      throws IOException {
    if (isCollapsible()) {
      writeCollapsed(items, writeObjectWrapper, context);
    } else {
      writeNormal(items, writeObjectWrapper, context);
    }
  }

  private void writeCollapsed(@NonNull Collection<? extends Object> items, boolean writeObjectWrapper,
      @NonNull IJsonWritingContext context) throws IOException {
    CollapseKeyBuilder builder = new CollapseKeyBuilder(this);
    builder.addAll(items);

    builder.write(writeObjectWrapper, context);
  }

  @SuppressWarnings("resource") // not owned
  private void writeNormal(Collection<? extends Object> items, boolean writeObjectWrapper,
      IJsonWritingContext context)
      throws IOException {
    if (items.isEmpty()) {
      return;
    }

    Predicate<IBoundFlagInstance> flagFilter = null;

    IBoundFlagInstance jsonKey = getJsonKeyFlagInstance();
    if (jsonKey != null) {
      flagFilter = (flag) -> {
        return !jsonKey.equals(flag);
      };
    }

    IBoundFlagInstance jsonValueKey = getJsonValueKeyFlagInstance();
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

    Map<String, ? extends IBoundNamedInstance> properties = getNamedInstances(flagFilter);

    JsonGenerator writer = context.getWriter(); // NOPMD - intentional

    for (Object item : items) {
      assert item != null;
      if (writeObjectWrapper) {
        writer.writeStartObject();
      }

      if (jsonKey != null) {
        // if there is a json key, the first field will be the key
        Object flagValue = jsonKey.getValue(item);
        String key = jsonKey.getValueAsString(flagValue);
        if (key == null) {
          throw new IOException(new NullPointerException("Null key value")); // NOPMD - intentional
        }
        writer.writeFieldName(key);

        // next the value will be a start object
        writer.writeStartObject();
      }

      for (IBoundNamedInstance property : properties.values()) {
        ObjectUtils.notNull(property).write(item, context);
      }

      Object fieldValue = getFieldValueInstance().getValue(item);
      if (fieldValue != null) {
        String valueKeyName;
        if (jsonValueKey != null) {
          valueKeyName = jsonValueKey.getValueAsString(jsonValueKey.getValue(item));
        } else {
          valueKeyName = getFieldValueInstance().getJsonValueKeyName();
        }
        writer.writeFieldName(valueKeyName);
        getFieldValueInstance().writeValue(fieldValue, context);
      }

      if (jsonKey != null) {
        writer.writeEndObject();
      }

      if (writeObjectWrapper) {
        writer.writeEndObject();
      }
    }
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return getFieldValueInstance().getJavaTypeAdapter();
  }

  @Override
  protected void copyBoundObjectInternal(@NonNull Object fromInstance, @NonNull Object toInstance)
      throws BindingException {
    super.copyBoundObjectInternal(fromInstance, toInstance);

    getFieldValueInstance().copyBoundObject(fromInstance, toInstance);
  }

  @Override
  protected Class<? extends IMetaschema> getMetaschemaClass() {
    return getMetaschemaFieldAnnotation().metaschema();
  }
}
