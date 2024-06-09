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

package gov.nist.secauto.metaschema.databind.io.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.core.model.util.JsonUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldScalar;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedField;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedNamed;
import gov.nist.secauto.metaschema.databind.model.IBoundProperty;
import gov.nist.secauto.metaschema.databind.model.info.AbstractModelInstanceReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureScalarItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IModelInstanceCollectionInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.talsmasoftware.lazy4j.Lazy;

public class MetaschemaJsonReader
    implements IJsonParsingContext {
  private static final Logger LOGGER = LogManager.getLogger(MetaschemaJsonReader.class);

  @NonNull
  private final Deque<JsonParser> parserStack = new LinkedList<>();
  @NonNull
  private final InstanceReader instanceReader = new InstanceReader();
  @NonNull
  private final ItemReader itemReader = new ItemReader();

  @NonNull
  private final IJsonProblemHandler problemHandler;
  @NonNull
  private final Lazy<ObjectMapper> objectMapper;

  /**
   * Construct a new Module-aware JSON parser using the default problem handler.
   *
   * @param parser
   *          the JSON parser to parse with
   * @throws IOException
   *           if an error occurred while reading the JSON
   * @see DefaultJsonProblemHandler
   */
  @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
  public MetaschemaJsonReader(
      @NonNull JsonParser parser) throws IOException {
    this(parser, new DefaultJsonProblemHandler());
  }

  /**
   * Construct a new Module-aware JSON parser.
   *
   * @param parser
   *          the JSON parser to parse with
   * @param problemHandler
   *          the problem handler implementation to use
   * @throws IOException
   *           if an error occurred while reading the JSON
   */
  @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
  public MetaschemaJsonReader(
      @NonNull JsonParser parser,
      @NonNull IJsonProblemHandler problemHandler) throws IOException {
    this.problemHandler = problemHandler;
    this.objectMapper = ObjectUtils.notNull(Lazy.lazy(ObjectMapper::new));
    push(parser);
  }

  @SuppressWarnings("resource")
  @Override
  public JsonParser getReader() {
    return ObjectUtils.notNull(parserStack.peek());
  }

  @NonNull
  protected InstanceReader getInstanceReader() {
    return instanceReader;
  }

  @NonNull
  protected ItemReader getItemReader() {
    return itemReader;
  }

  // protected void analyzeParserStack(@NonNull String action) throws IOException
  // {
  // StringBuilder builder = new StringBuilder()
  // .append("------\n");
  //
  // for (JsonParser parser : parserStack) {
  // JsonToken token = parser.getCurrentToken();
  // if (token == null) {
  // LOGGER.info(String.format("Advancing parser: %s", parser.hashCode()));
  // token = parser.nextToken();
  // }
  //
  // String name = parser.currentName();
  // builder.append(String.format("%s: %d: %s(%s)%s\n",
  // action,
  // parser.hashCode(),
  // token.name(),
  // name == null ? "" : name,
  // JsonUtil.generateLocationMessage(parser)));
  // }
  // LOGGER.info(builder.toString());
  // }

  @SuppressWarnings("resource")
  public final void push(JsonParser parser) throws IOException {
    assert !parser.equals(parserStack.peek());
    if (parser.getCurrentToken() == null) {
      parser.nextToken();
    }
    parserStack.push(parser);
  }

  @SuppressWarnings("resource")
  @NonNull
  public final JsonParser pop(@NonNull JsonParser parser) {
    JsonParser old = parserStack.pop();
    assert parser.equals(old);
    return ObjectUtils.notNull(parserStack.peek());
  }

  @Override
  public IJsonProblemHandler getProblemHandler() {
    return problemHandler;
  }

  @NonNull
  protected ObjectMapper getObjectMapper() {
    return ObjectUtils.notNull(objectMapper.get());
  }

  public class InstanceReader implements IJsonParsingContext.IInstanceReader {

    @Override
    public JsonParser getJsonParser() {
      return getReader();
    }

    @Override
    public Object readItemFlag(Object parentItem, IBoundInstanceFlag instance) throws IOException {
      return readInstance(instance, parentItem);
    }

    @Override
    public Object readItemField(Object parentItem, IBoundInstanceModelFieldScalar instance) throws IOException {
      return readModelInstance(instance, parentItem);
    }

    @Override
    public Object readItemField(Object parentItem, IBoundInstanceModelFieldComplex instance) throws IOException {
      return readModelInstance(instance, parentItem);
    }

    @Override
    public Object readItemField(Object parentItem, IBoundInstanceModelGroupedField instance) throws IOException {
      throw new UnsupportedOperationException("not needed");
    }

    @Override
    public Object readItemField(Object item, IBoundDefinitionModelFieldComplex definition) throws IOException {
      return definition.readItem(item, getItemReader());
    }

    @Override
    public Object readItemFieldValue(Object parentItem, IBoundFieldValue fieldValue) throws IOException {
      return readInstance(fieldValue, parentItem);
    }

    @Override
    public Object readItemAssembly(Object parentItem, IBoundInstanceModelAssembly instance) throws IOException {
      return readModelInstance(instance, parentItem);
    }

    @Override
    public Object readItemAssembly(Object parentItem, IBoundInstanceModelGroupedAssembly instance) throws IOException {
      throw new UnsupportedOperationException("not needed");
    }

    @Override
    public Object readItemAssembly(Object item, IBoundDefinitionModelAssembly definition) throws IOException {
      return getItemReader().readItemAssembly(item, definition);
    }

    @Override
    public Object readChoiceGroupItem(Object parentItem, IBoundInstanceModelChoiceGroup instance) throws IOException {
      return readModelInstance(instance, parentItem);
    }

    @NonNull
    private Object readInstance(
        @NonNull IBoundProperty instance,
        @NonNull Object parentItem) throws IOException {
      return instance.readItem(parentItem, getItemReader());
    }

    @NonNull
    private Object readModelInstance(
        @NonNull IBoundInstanceModel instance,
        @NonNull Object parentItem) throws IOException {
      IModelInstanceCollectionInfo collectionInfo = instance.getCollectionInfo();
      return collectionInfo.readItems(new ModelInstanceReadHandler(instance, parentItem, getItemReader()));
    }
  }

  private final class ItemReader implements IJsonParsingContext.ItemReader {

    @Override
    public Object readItemFlag(Object parentItem, IBoundInstanceFlag instance) throws IOException {
      return readScalarItem(instance);
    }

    @Override
    public Object readItemField(Object parentItem, IBoundInstanceModelFieldScalar instance) throws IOException {
      return readScalarItem(instance);
    }

    @Override
    public Object readItemField(Object parentItem, IBoundInstanceModelFieldComplex instance) throws IOException {
      return readFieldObject(
          parentItem,
          instance.getDefinition(),
          instance.getJsonProperties(),
          instance.getEffectiveJsonKey(),
          getProblemHandler());
    }

    @Override
    public Object readItemField(Object parentItem, IBoundInstanceModelGroupedField instance) throws IOException {
      IJsonProblemHandler problemHandler = new GroupedInstanceProblemHandler(instance, getProblemHandler());
      IBoundDefinitionModelFieldComplex definition = instance.getDefinition();
      IBoundInstanceFlag jsonValueKeyFlag = definition.getJsonValueKeyFlagInstance();

      IJsonProblemHandler actualProblemHandler = jsonValueKeyFlag == null
          ? problemHandler
          : new JsomValueKeyProblemHandler(problemHandler, jsonValueKeyFlag);

      return readComplexDefinitionObject(
          parentItem,
          definition,
          instance.getEffectiveJsonKey(),
          new PropertyBodyHandler(instance.getJsonProperties()),
          actualProblemHandler);
    }

    @Override
    public Object readItemField(Object parentItem, IBoundDefinitionModelFieldComplex definition) throws IOException {
      return readFieldObject(
          parentItem,
          definition,
          definition.getJsonProperties(),
          null,
          getProblemHandler());
    }

    @Override
    public Object readItemFieldValue(Object parentItem, IBoundFieldValue fieldValue) throws IOException {
      // read the field value's value
      return readScalarItem(fieldValue);
    }

    @Override
    public Object readItemAssembly(Object parentItem, IBoundInstanceModelAssembly instance) throws IOException {
      IBoundInstanceFlag jsonKey = instance.getJsonKey();
      IBoundDefinitionModelComplex definition = instance.getDefinition();
      return readComplexDefinitionObject(
          parentItem,
          definition,
          jsonKey,
          new PropertyBodyHandler(instance.getJsonProperties()),
          getProblemHandler());
    }

    @Override
    public Object readItemAssembly(Object parentItem, IBoundInstanceModelGroupedAssembly instance) throws IOException {
      return readComplexDefinitionObject(
          parentItem,
          instance.getDefinition(),
          instance.getEffectiveJsonKey(),
          new PropertyBodyHandler(instance.getJsonProperties()),
          new GroupedInstanceProblemHandler(instance, getProblemHandler()));
    }

    @Override
    public Object readItemAssembly(Object parentItem, IBoundDefinitionModelAssembly definition) throws IOException {
      return readComplexDefinitionObject(
          parentItem,
          definition,
          null,
          new PropertyBodyHandler(definition.getJsonProperties()),
          getProblemHandler());
    }

    @SuppressWarnings("resource")
    @NonNull
    private Object readScalarItem(@NonNull IFeatureScalarItemValueHandler handler)
        throws IOException {
      return handler.getJavaTypeAdapter().parse(getReader());
    }

    @NonNull
    private Object readFieldObject(
        @Nullable Object parentItem,
        @NonNull IBoundDefinitionModelFieldComplex definition,
        @NonNull Map<String, IBoundProperty> jsonProperties,
        @Nullable IBoundInstanceFlag jsonKey,
        @NonNull IJsonProblemHandler problemHandler) throws IOException {
      IBoundInstanceFlag jsonValueKey = definition.getJsonValueKeyFlagInstance();
      IJsonProblemHandler actualProblemHandler = jsonValueKey == null
          ? problemHandler
          : new JsomValueKeyProblemHandler(problemHandler, jsonValueKey);

      Object retval;
      if (jsonProperties.isEmpty() && jsonValueKey == null) {
        retval = readComplexDefinitionObject(
            parentItem,
            definition,
            jsonKey,
            (def, parent, problem) -> {
              IBoundFieldValue fieldValue = definition.getFieldValue();
              Object item = readItemFieldValue(parent, fieldValue);
              fieldValue.setValue(parent, item);
            },
            actualProblemHandler);

      } else {
        retval = readComplexDefinitionObject(
            parentItem,
            definition,
            jsonKey,
            new PropertyBodyHandler(jsonProperties),
            actualProblemHandler);
      }
      return retval;
    }

    @SuppressWarnings("resource")
    @Override
    public Object readChoiceGroupItem(Object parentItem, IBoundInstanceModelChoiceGroup instance) throws IOException {
      JsonParser parser = getReader();
      ObjectNode node = parser.readValueAsTree();

      String discriminatorProperty = instance.getJsonDiscriminatorProperty();
      JsonNode discriminatorNode = node.get(discriminatorProperty);
      if (discriminatorNode == null) {
        throw new IllegalArgumentException(String.format(
            "Unable to find discriminator property '%s' for object at '%s'.",
            discriminatorProperty,
            JsonUtil.toString(parser)));
      }
      String discriminator = ObjectUtils.requireNonNull(discriminatorNode.asText());

      IBoundInstanceModelGroupedNamed actualInstance = instance.getGroupedModelInstance(discriminator);
      assert actualInstance != null;

      Object retval;
      try (JsonParser newParser = node.traverse(parser.getCodec())) {
        push(newParser);

        // get initial token
        retval = actualInstance.readItem(parentItem, getItemReader());
        assert newParser.currentToken() == null;
        pop(newParser);
      }

      // advance the original parser to the next token
      parser.nextToken();

      return retval;
    }

    private final class JsonKeyBodyHandler implements DefinitionBodyHandler<IBoundDefinitionModelComplex> {
      @NonNull
      private final IBoundInstanceFlag jsonKey;
      @NonNull
      private final DefinitionBodyHandler<IBoundDefinitionModelComplex> bodyHandler;

      private JsonKeyBodyHandler(
          @NonNull IBoundInstanceFlag jsonKey,
          @NonNull DefinitionBodyHandler<IBoundDefinitionModelComplex> bodyHandler) {
        this.jsonKey = jsonKey;
        this.bodyHandler = bodyHandler;
      }

      @Override
      public void accept(IBoundDefinitionModelComplex definition, Object parentItem, IJsonProblemHandler problemHandler)
          throws IOException {
        @SuppressWarnings("resource") JsonParser parser = getReader();
        JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME);

        // the field will be the JSON key
        String key = ObjectUtils.notNull(parser.currentName());
        Object value = jsonKey.getDefinition().getJavaTypeAdapter().parse(key);
        jsonKey.setValue(parentItem, ObjectUtils.notNull(value.toString()));

        // skip to the next token
        parser.nextToken();
        // JsonUtil.assertCurrent(parser, JsonToken.START_OBJECT);

        // // advance past the JSON key's start object
        // JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);

        // read the property values
        bodyHandler.accept(definition, parentItem, problemHandler);

        // // advance past the JSON key's end object
        // JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
      }
    }

    private final class PropertyBodyHandler implements DefinitionBodyHandler<IBoundDefinitionModelComplex> {
      @NonNull
      private final Map<String, IBoundProperty> jsonProperties;

      private PropertyBodyHandler(@NonNull Map<String, IBoundProperty> jsonProperties) {
        this.jsonProperties = jsonProperties;
      }

      @Override
      public void accept(
          IBoundDefinitionModelComplex definition,
          Object parentItem,
          IJsonProblemHandler problemHandler)
          throws IOException {
        @SuppressWarnings("resource") JsonParser parser = getReader();

        // advance past the start object
        JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);

        // make a copy, since we use the remaining values to initialize default values
        Map<String, IBoundProperty> remainingInstances = new HashMap<>(jsonProperties); // NOPMD not concurrent

        // handle each property
        while (JsonToken.FIELD_NAME.equals(parser.currentToken())) {

          // the parser's current token should be the JSON field name
          String propertyName = ObjectUtils.notNull(parser.currentName());
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("reading property {}", propertyName);
          }

          IBoundProperty property = remainingInstances.get(propertyName);

          boolean handled = false;
          if (property != null) {
            // advance past the field name
            parser.nextToken();

            Object value = property.readItem(parentItem, getInstanceReader());
            property.setValue(parentItem, value);

            // mark handled
            remainingInstances.remove(propertyName);
            handled = true;
          }

          if (!handled && !problemHandler.handleUnknownProperty(
              definition,
              parentItem,
              propertyName,
              getInstanceReader())) {
            if (LOGGER.isWarnEnabled()) {
              LOGGER.warn("Skipping unhandled JSON field '{}' {}.", propertyName, JsonUtil.toString(parser));
            }
            JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);
            JsonUtil.skipNextValue(parser);
          }

          // the current token will be either the next instance field name or the end of
          // the parent object
          JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
        }

        problemHandler.handleMissingInstances(
            definition,
            parentItem,
            ObjectUtils.notNull(remainingInstances.values()));

        // advance past the end object
        JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
      }
    }

    private final class GroupedInstanceProblemHandler implements IJsonProblemHandler {
      @NonNull
      private final IBoundInstanceModelGroupedNamed instance;
      @NonNull
      private final IJsonProblemHandler delegate;

      private GroupedInstanceProblemHandler(
          @NonNull IBoundInstanceModelGroupedNamed instance,
          @NonNull IJsonProblemHandler delegate) {
        this.instance = instance;
        this.delegate = delegate;
      }

      @Override
      public void handleMissingInstances(IBoundDefinitionModel parentDefinition, Object targetObject,
          Collection<? extends IBoundProperty> unhandledInstances) throws IOException {
        delegate.handleMissingInstances(parentDefinition, targetObject, unhandledInstances);
      }

      @SuppressWarnings("resource")
      @Override
      public boolean handleUnknownProperty(
          IBoundDefinitionModelComplex definition,
          Object parentItem,
          String fieldName,
          IInstanceReader reader) throws IOException {
        JsonParser parser = reader.getJsonParser();
        boolean retval;
        if (instance.getParentContainer().getJsonDiscriminatorProperty().equals(fieldName)) {
          JsonUtil.skipNextValue(parser);
          retval = true;
        } else {
          retval = delegate.handleUnknownProperty(definition, parentItem, fieldName, reader);
        }
        return retval;
      }
    }

    private final class JsomValueKeyProblemHandler implements IJsonProblemHandler {
      @NonNull
      private final IJsonProblemHandler delegate;
      @NonNull
      private final IBoundInstanceFlag jsonValueKyeFlag;
      private boolean foundJsonValueKey; // false

      private JsomValueKeyProblemHandler(
          @NonNull IJsonProblemHandler delegate,
          @NonNull IBoundInstanceFlag jsonValueKyeFlag) {
        this.delegate = delegate;
        this.jsonValueKyeFlag = jsonValueKyeFlag;
      }

      @Override
      public void handleMissingInstances(IBoundDefinitionModel parentDefinition, Object targetObject,
          Collection<? extends IBoundProperty> unhandledInstances) throws IOException {
        delegate.handleMissingInstances(parentDefinition, targetObject, unhandledInstances);
      }

      @Override
      public boolean handleUnknownProperty(IBoundDefinitionModelComplex definition, Object parentItem, String fieldName,
          IInstanceReader reader) throws IOException {
        boolean retval;
        if (foundJsonValueKey) {
          retval = delegate.handleUnknownProperty(definition, parentItem, fieldName, reader);
        } else {
          @SuppressWarnings("resource") JsonParser parser = getReader();
          // handle JSON value key
          String key = ObjectUtils.notNull(parser.getCurrentName());
          Object keyValue = jsonValueKyeFlag.getJavaTypeAdapter().parse(key);
          jsonValueKyeFlag.setValue(ObjectUtils.notNull(parentItem), keyValue);

          // advance past the field name
          JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);

          IBoundFieldValue fieldValue = ((IBoundDefinitionModelFieldComplex) definition).getFieldValue();
          Object value = getItemReader().readItemFieldValue(
              ObjectUtils.notNull(parentItem),
              fieldValue);
          fieldValue.setValue(ObjectUtils.notNull(parentItem), value);

          retval = foundJsonValueKey = true;
        }
        return retval;
      }

    }

    @NonNull
    private Object readComplexDefinitionObject(
        @Nullable Object parentItem,
        @NonNull IBoundDefinitionModelComplex definition,
        @Nullable IBoundInstanceFlag jsonKey,
        @NonNull DefinitionBodyHandler<IBoundDefinitionModelComplex> bodyHandler,
        @NonNull IJsonProblemHandler problemHandler) throws IOException {
      DefinitionBodyHandler<IBoundDefinitionModelComplex> actualBodyHandler = jsonKey == null
          ? bodyHandler
          : new JsonKeyBodyHandler(jsonKey, bodyHandler);

      // construct the item
      Object item = definition.newInstance();

      try {
        // call pre-parse initialization hook
        definition.callBeforeDeserialize(item, parentItem);

        // read the property values
        actualBodyHandler.accept(definition, item, problemHandler);

        // call post-parse initialization hook
        definition.callAfterDeserialize(item, parentItem);
      } catch (BindingException ex) {
        throw new IOException(ex);
      }

      return item;
    }
  }

  private class ModelInstanceReadHandler
      extends AbstractModelInstanceReadHandler {

    protected ModelInstanceReadHandler(
        @NonNull IBoundInstanceModel instance,
        @NonNull Object parentItem,
        @SuppressWarnings("unused") @NonNull IJsonParsingContext.ItemReader itemReader) {
      super(instance, parentItem);
    }

    @SuppressWarnings("resource") // no need to close parser
    @Override
    public List<?> readList() throws IOException {
      JsonParser parser = getReader();

      List<Object> items = new LinkedList<>();
      switch (parser.currentToken()) {
      case START_ARRAY:
        // this is an array, we need to parse the array wrapper then each item
        JsonUtil.assertAndAdvance(parser, JsonToken.START_ARRAY);

        // parse items
        while (!JsonToken.END_ARRAY.equals(parser.currentToken())) {
          items.add(readItem());
        }

        // this is the other side of the array wrapper, advance past it
        JsonUtil.assertAndAdvance(parser, JsonToken.END_ARRAY);
        break;
      case VALUE_NULL:
        JsonUtil.assertAndAdvance(parser, JsonToken.VALUE_NULL);
        break;
      default:
        // this is a singleton, just parse the value as a single item
        items.add(readItem());
        break;
      }
      return items;
    }

    @SuppressWarnings("resource") // no need to close parser
    @Override
    public Map<String, ?> readMap() throws IOException {
      JsonParser parser = getReader();

      IBoundInstanceModel instance = getCollectionInfo().getInstance();

      @SuppressWarnings("PMD.UseConcurrentHashMap") Map<String, Object> items = new LinkedHashMap<>();

      // A map value is always wrapped in a START_OBJECT, since fields are used for
      // the keys
      JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);

      // process all map items
      while (!JsonToken.END_OBJECT.equals(parser.currentToken())) {

        // a map item will always start with a FIELD_NAME, since this represents the key
        JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME);

        Object item = readItem();

        // lookup the key
        IBoundInstanceFlag jsonKey = instance.getItemJsonKey(item);
        assert jsonKey != null;

        Object keyValue = jsonKey.getValue(item);
        if (keyValue == null) {
          throw new IOException(String.format("Null value for json-key for definition '%s'",
              jsonKey.getContainingDefinition().toCoordinates()));
        }
        String key = jsonKey.getJavaTypeAdapter().asString(keyValue);
        items.put(key, item);

        // the next item will be a FIELD_NAME, or we will encounter an END_OBJECT if all
        // items have been
        // read
        JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
      }

      // A map value will always end with an end object, which needs to be consumed
      JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);

      return items;
    }

    @Override
    @NonNull
    public Object readItem() throws IOException {
      IBoundInstanceModel instance = getCollectionInfo().getInstance();
      return instance.readItem(getParentObject(), getItemReader());
    }
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public <T> T readObject(@NonNull IBoundDefinitionModelComplex definition) throws IOException {
    return (T) definition.readItem(null, getInstanceReader());
  }

  @SuppressWarnings({ "unchecked", "resource" })
  @NonNull
  public <T> T readProperty(
      @NonNull IBoundDefinitionModelComplex definition,
      @NonNull String expectedFieldName) throws IOException {
    JsonParser parser = getReader();

    boolean hasStartObject = JsonToken.START_OBJECT.equals(parser.currentToken());
    if (hasStartObject) {
      // advance past the start object
      JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);
    }

    T retval = null;
    JsonToken token;
    while (!JsonToken.END_OBJECT.equals(token = parser.currentToken()) && token != null) {
      if (!JsonToken.FIELD_NAME.equals(token)) {
        throw new IOException(String.format("Expected FIELD_NAME token, found '%s'", token.toString()));
      }

      String propertyName = ObjectUtils.notNull(parser.currentName());
      if (expectedFieldName.equals(propertyName)) {
        // process the object value, bound to the requested class
        JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);

        // stop now, since we found the field
        retval = (T) definition.readItem(null, getInstanceReader());
        break;
      }

      if (!getProblemHandler().handleUnknownProperty(
          definition,
          null,
          propertyName,
          getInstanceReader())) {
        if (LOGGER.isWarnEnabled()) {
          LOGGER.warn("Skipping unhandled JSON field '{}'{}.", propertyName, JsonUtil.toString(parser));
        }
        JsonUtil.skipNextValue(parser);
      }
    }

    if (hasStartObject) {
      // advance past the end object
      JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
    }

    if (retval == null) {
      throw new IOException(String.format("Failed to find property with name '%s'%s.",
          expectedFieldName,
          JsonUtil.generateLocationMessage(parser)));
    }
    return retval;
  }

  @FunctionalInterface
  private interface DefinitionBodyHandler<DEF extends IBoundDefinitionModelComplex> {
    void accept(
        @NonNull DEF definition,
        @NonNull Object item,
        @NonNull IJsonProblemHandler problemHandler) throws IOException;
  }
}
