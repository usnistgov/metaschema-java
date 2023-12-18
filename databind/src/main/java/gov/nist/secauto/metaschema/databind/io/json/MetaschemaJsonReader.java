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
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public class MetaschemaJsonReader
    implements IJsonParsingContext {
  private static final Logger LOGGER = LogManager.getLogger(MetaschemaJsonReader.class);

  @NonNull
  private final JsonParser rootParser;
  @NonNull
  private final IJsonProblemHandler problemHandler;
  @NonNull
  private final Lazy<ObjectMapper> objectMapper;

  /**
   * Construct a new Module-aware JSON parser using the default problem handler.
   *
   * @param parser
   *          the JSON parser to parse with
   * @see DefaultJsonProblemHandler
   */
  public MetaschemaJsonReader(
      @NonNull JsonParser parser) {
    this(parser, new DefaultJsonProblemHandler());
  }

  /**
   * Construct a new Module-aware JSON parser.
   *
   * @param parser
   *          the JSON parser to parse with
   * @param problemHandler
   *          the problem handler implementation to use
   */
  public MetaschemaJsonReader(
      @NonNull JsonParser parser,
      @NonNull IJsonProblemHandler problemHandler) {
    this.rootParser = parser;
    this.problemHandler = problemHandler;
    this.objectMapper = ObjectUtils.notNull(Lazy.lazy(() -> new ObjectMapper()));
  }

  @Override
  public JsonParser getReader() {
    return rootParser;
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
    @NonNull
    private final ItemReader itemReader;

    protected InstanceReader(@NonNull ItemReader itemReader) {
      this.itemReader = itemReader;
    }

    /**
     * @return the startElement
     */
    @Override
    @NonNull
    public JsonParser getJsonParser() {
      return getItemReader().getJsonParser();
    }

    @Override
    @NonNull
    public IJsonParsingContext.ItemReader getItemReader() {
      return itemReader;
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
    public Object readItemField(Object item, IBoundDefinitionFieldComplex definition) throws IOException {
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
    public Object readItemAssembly(Object item, IBoundDefinitionAssembly definition) throws IOException {
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

  private class ItemReader implements IJsonParsingContext.ItemReader {
    @NonNull
    private final JsonParser itemParser;

    protected ItemReader(@NonNull JsonParser parser) {
      this.itemParser = parser;
    }

    /**
     * @return the startElement
     */
    @Override
    @NonNull
    public JsonParser getJsonParser() {
      return itemParser;
    }

    @Override
    public Object readItemFlag(Object parentItem, IBoundInstanceFlag instance) throws IOException {
      return readScalarItem(instance);
    }

    @Override
    public Object readItemField(Object parentItem, IBoundInstanceModelFieldScalar instance) throws IOException {
      return readScalarItem(instance);
    }

    @NonNull
    private Object readFieldObject(
        @Nullable Object parentItem,
        @NonNull IBoundDefinitionFieldComplex definition,
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

    @Override
    public Object readItemField(Object parentItem, IBoundInstanceModelFieldComplex instance) throws IOException {
      return readFieldObject(
          parentItem,
          instance.getDefinition(),
          instance.getJsonProperties(),
          instance.getJsonKey(),
          getProblemHandler());
    }

    @Override
    public Object readItemField(Object parentItem, IBoundInstanceModelGroupedField instance) throws IOException {
      IJsonProblemHandler problemHandler = new GroupedInstanceProblemHandler(instance, getProblemHandler());
      IBoundDefinitionFieldComplex definition = instance.getDefinition();
      IBoundInstanceFlag jsonValueKeyFlag = definition.getJsonValueKeyFlagInstance();

      IJsonProblemHandler actualProblemHandler = jsonValueKeyFlag == null
          ? problemHandler
          : new JsomValueKeyProblemHandler(problemHandler, jsonValueKeyFlag);

      return readComplexDefinitionObject(
          parentItem,
          definition,
          instance.getJsonKey(),
          new PropertyBodyHandler(instance.getJsonProperties()),
          actualProblemHandler);
    }

    @Override
    public Object readItemField(Object parentItem, IBoundDefinitionFieldComplex definition) throws IOException {
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
          instance.getJsonKey(),
          new PropertyBodyHandler(instance.getJsonProperties()),
          new GroupedInstanceProblemHandler(instance, getProblemHandler()));
    }

    @Override
    public Object readItemAssembly(Object parentItem, IBoundDefinitionAssembly definition) throws IOException {
      return readComplexDefinitionObject(
          parentItem,
          definition,
          null,
          new PropertyBodyHandler(definition.getJsonProperties()),
          getProblemHandler());
    }

    @NonNull
    private Object readScalarItem(@NonNull IFeatureScalarItemValueHandler handler)
        throws IOException {
      return handler.getJavaTypeAdapter().parse(itemParser);
    }

    @Override
    public Object readChoiceGroupItem(Object parentItem, IBoundInstanceModelChoiceGroup instance) throws IOException {
      ObjectNode node = itemParser.readValueAsTree();

      JsonNode descriminatorNode = node.get(instance.getJsonDiscriminatorProperty());
      String discriminator = descriminatorNode.asText();

      IBoundInstanceModelGroupedNamed actualInstance = instance.getGroupedModelInstance(discriminator);
      assert actualInstance != null;
      try (JsonParser newParser = node.traverse(itemParser.getCodec())) {
        // get initial token
        newParser.nextToken();
        Object retval = actualInstance.readItem(parentItem, new ItemReader(newParser));
        assert newParser.currentToken() == null;
        return retval;
      }
    }

    private class JsonKeyBodyHandler implements DefinitionBodyHandler<IBoundDefinitionModelComplex> {
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
        JsonUtil.assertCurrent(itemParser, JsonToken.FIELD_NAME);

        // the field will be the JSON key
        String key = ObjectUtils.notNull(itemParser.currentName());
        Object value = jsonKey.getDefinition().getJavaTypeAdapter().parse(key);
        jsonKey.setValue(parentItem, ObjectUtils.notNull(value.toString()));

        // skip to the next token
        itemParser.nextToken();
        // JsonUtil.assertCurrent(parser, JsonToken.START_OBJECT);

        // // advance past the JSON key's start object
        // JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);

        // read the property values
        bodyHandler.accept(definition, parentItem, problemHandler);

        // // advance past the JSON key's end object
        // JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
      }
    }

    private class PropertyBodyHandler implements DefinitionBodyHandler<IBoundDefinitionModelComplex> {
      @NonNull
      private final Map<String, IBoundProperty> jsonProperties;

      private PropertyBodyHandler(@NonNull Map<String, IBoundProperty> jsonProperties) {
        this.jsonProperties = jsonProperties;
      }

      @Override
      public void accept(IBoundDefinitionModelComplex definition, Object parentItem, IJsonProblemHandler problemHandler)
          throws IOException {
        // advance past the start object
        JsonUtil.assertAndAdvance(itemParser, JsonToken.START_OBJECT);

        // make a copy, since we use the remaining values to initialize default values
        Map<String, IBoundProperty> remainingInstances = new HashMap<>(jsonProperties); // NOPMD not concurrent

        // handle each property
        while (JsonToken.FIELD_NAME.equals(itemParser.currentToken())) {

          // the parser's current token should be the JSON field name
          String propertyName = ObjectUtils.notNull(itemParser.currentName());
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("reading property {}", propertyName);
          }

          IBoundProperty property = remainingInstances.get(propertyName);

          boolean handled = false;
          if (property != null) {
            // advance past the field name
            itemParser.nextToken();

            Object value = property.readItem(parentItem, new InstanceReader(ItemReader.this));
            property.setValue(parentItem, value);

            // mark handled
            remainingInstances.remove(propertyName);
            handled = true;
          }

          if (!handled) {
            if (!problemHandler.handleUnknownProperty(
                definition,
                parentItem,
                propertyName,
                new InstanceReader(ItemReader.this))) {
              LOGGER.warn("Skipping unhandled JSON field '{}'.", propertyName);
              JsonUtil.assertAndAdvance(itemParser, JsonToken.FIELD_NAME);
              JsonUtil.skipNextValue(itemParser);
            }
          }

          // the current token will be either the next instance field name or the end of
          // the parent object
          JsonUtil.assertCurrent(itemParser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
        }

        problemHandler.handleMissingInstances(
            definition,
            parentItem,
            ObjectUtils.notNull(remainingInstances.values()));

        // advance past the end object
        JsonUtil.assertAndAdvance(itemParser, JsonToken.END_OBJECT);
      }
    }

    private class GroupedInstanceProblemHandler implements IJsonProblemHandler {
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

      @Override
      public boolean handleUnknownProperty(
          IBoundDefinitionModelComplex definition,
          Object parentItem,
          String fieldName,
          IInstanceReader reader) throws IOException {
        boolean retval;
        if (instance.getParentContainer().getJsonDiscriminatorProperty().equals(fieldName)) {
          JsonUtil.skipNextValue(reader.getJsonParser());
          retval = true;
        } else {
          retval = delegate.handleUnknownProperty(definition, parentItem, fieldName, reader);
        }
        return retval;
      }
    }

    private class JsomValueKeyProblemHandler implements IJsonProblemHandler {
      @NonNull
      private final IJsonProblemHandler delegate;
      @NonNull
      private final IBoundInstanceFlag jsonValueKyeFlag;
      private boolean foundJsonValueKey = false;

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
          // handle JSON value key
          String key = ObjectUtils.notNull(reader.getJsonParser().getCurrentName());
          Object keyValue = jsonValueKyeFlag.getJavaTypeAdapter().parse(key);
          jsonValueKyeFlag.setValue(ObjectUtils.notNull(parentItem), keyValue);

          // advance past the field name
          JsonUtil.assertAndAdvance(reader.getJsonParser(), JsonToken.FIELD_NAME);

          IBoundFieldValue fieldValue = ((IBoundDefinitionFieldComplex) definition).getFieldValue();
          Object value = reader.getItemReader().readItemFieldValue(
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
    @NonNull
    private final IJsonParsingContext.ItemReader itemReader;

    protected ModelInstanceReadHandler(
        @NonNull IBoundInstanceModel instance,
        @NonNull Object parentItem,
        @NonNull IJsonParsingContext.ItemReader itemReader) {
      super(instance, parentItem);
      this.itemReader = itemReader;
    }

    @NonNull
    protected IJsonParsingContext.ItemReader getItemReader() {
      return itemReader;
    }

    @SuppressWarnings("resource") // no need to close parser
    @Override
    public List<?> readList() throws IOException {
      JsonParser itemParser = getItemReader().getJsonParser();

      List<Object> items = new LinkedList<>();
      switch (itemParser.currentToken()) {
      case START_ARRAY: {
        // this is an array, we need to parse the array wrapper then each item
        JsonUtil.assertAndAdvance(itemParser, JsonToken.START_ARRAY);

        // parse items
        JsonToken token;
        while (!JsonToken.END_ARRAY.equals(token = itemParser.currentToken())) {
          items.add(readItem());
        }

        // this is the other side of the array wrapper, advance past it
        JsonUtil.assertAndAdvance(itemParser, JsonToken.END_ARRAY);
        break;
      }
      case VALUE_NULL: {
        JsonUtil.assertAndAdvance(itemParser, JsonToken.VALUE_NULL);
        break;
      }
      default:
        // this is a singleton, just parse the value as a single item
        items.add(readItem());
      }
      return items;
    }

    @SuppressWarnings("resource") // no need to close parser
    @Override
    public Map<String, ?> readMap() throws IOException {
      JsonParser itemParser = getItemReader().getJsonParser();

      IBoundInstanceModel instance = getCollectionInfo().getInstance();

      Map<String, Object> items = new LinkedHashMap<>();

      // A map value is always wrapped in a START_OBJECT, since fields are used for
      // the keys
      JsonUtil.assertAndAdvance(itemParser, JsonToken.START_OBJECT);

      // process all map items
      JsonToken token;
      while (!JsonToken.END_OBJECT.equals(token = itemParser.currentToken())) {

        // a map item will always start with a FIELD_NAME, since this represents the key
        JsonUtil.assertCurrent(itemParser, JsonToken.FIELD_NAME);

        Object item = readItem();

        // lookup the key
        IBoundInstanceFlag jsonKey = instance.getItemJsonKey(item);
        assert jsonKey != null;

        String key = ObjectUtils.requireNonNull(jsonKey.getValue(item)).toString();
        items.put(key, item);

        // the next item will be a FIELD_NAME, or we will encounter an END_OBJECT if all
        // items have been
        // read
        JsonUtil.assertCurrent(itemParser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
      }

      // A map value will always end with an end object, which needs to be consumed
      JsonUtil.assertAndAdvance(itemParser, JsonToken.END_OBJECT);

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
    return (T) definition.readItem(null, new InstanceReader(new ItemReader(rootParser)));
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public <T> T readField(
      @NonNull IBoundDefinitionModelComplex definition,
      @NonNull String expectedFieldName) throws IOException {
    // check if at the start of parsing a document
    if (rootParser.currentToken() == null) {
      rootParser.nextToken();
    }

    boolean hasStartObject = JsonToken.START_OBJECT.equals(rootParser.currentToken());
    if (hasStartObject) {
      // advance past the start object
      JsonUtil.assertAndAdvance(rootParser, JsonToken.START_OBJECT);
    }

    T retval = null;
    JsonToken token;
    while (!(JsonToken.END_OBJECT.equals(token = rootParser.currentToken()) || token == null)) {
      if (!JsonToken.FIELD_NAME.equals(token)) {
        throw new IOException(String.format("Expected FIELD_NAME token, found '%s'", token.toString()));
      }

      String fieldName = rootParser.currentName();
      if (expectedFieldName.equals(fieldName)) {
        // process the object value, bound to the requested class
        JsonUtil.assertAndAdvance(rootParser, JsonToken.FIELD_NAME);

        // stop now, since we found the field
        retval = (T) definition.readItem(null, new InstanceReader(new ItemReader(rootParser)));
        break;
      }

      if (!getProblemHandler().handleUnknownProperty(
          definition,
          null,
          fieldName,
          new InstanceReader(new ItemReader(rootParser)))) {
        LOGGER.warn("Skipping unhandled JSON field '{}'.", fieldName);
        JsonUtil.skipNextValue(rootParser);
      }
    }
    if (hasStartObject) {
      // advance past the end object
      JsonUtil.assertAndAdvance(rootParser, JsonToken.END_OBJECT);
    }

    if (retval == null) {
      throw new IOException(String.format("Failed to find property with name '%s'%s.",
          expectedFieldName,
          JsonUtil.generateLocationMessage(rootParser)));
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
