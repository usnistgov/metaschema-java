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

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.AbstractParser;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValueInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundNamedInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.IFieldClassBinding;
import gov.nist.secauto.metaschema.databind.model.IModelPropertyInfo;
import gov.nist.secauto.metaschema.databind.model.IPropertyCollector;
import gov.nist.secauto.metaschema.databind.model.IRootAssemblyClassBinding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class MetaschemaJsonParser
    extends AbstractParser
    implements IJsonParsingContext {
  private static final Logger LOGGER = LogManager.getLogger(MetaschemaJsonParser.class);

  @NonNull
  private final JsonParser parser;
  @NonNull
  private final IJsonProblemHandler problemHandler;

  public MetaschemaJsonParser(
      @NonNull JsonParser parser) {
    this(parser, new DefaultJsonProblemHandler());
  }

  public MetaschemaJsonParser(
      @NonNull JsonParser parser,
      @NonNull IJsonProblemHandler problemHandler) {
    this.parser = parser;
    this.problemHandler = problemHandler;
  }

  @Override
  public JsonParser getReader() {
    return parser;
  }

  @Override
  public IJsonProblemHandler getProblemHandler() {
    return problemHandler;
  }

  /**
   * Parses JSON into a bound object. This assembly must be a root assembly for which a call to
   * {@link IAssemblyClassBinding#isRoot()} will return {@code true}.
   * <p>
   * This method expects the parser's current token to be:
   * <ul>
   * <li>{@code null} indicating that the parser has not yet parsed a JSON node;</li>
   * <li>a {@link JsonToken#START_OBJECT} which represents the object wrapper containing the root
   * field,</li>
   * <li>a {@link JsonToken#FIELD_NAME} representing the root field to parse, or</li>
   * <li>a peer field to the root field that will be handled by the
   * {@link IJsonProblemHandler#handleUnknownRootProperty(IAssemblyClassBinding, String, JsonParser)}
   * method.</li>
   * </ul>
   * <p>
   * After parsing the current token will be:
   * <ul>
   * <li>the next token after the {@link JsonToken#END_OBJECT} corresponding to the initial
   * {@link JsonToken#START_OBJECT} parsed by this method;</li>
   * <li>the next token after the {@link JsonToken#END_OBJECT} for the root field's value; or</li>
   * <li>the next token after all fields and associated values have been parsed looking for the root
   * field. This next token will be the {@link JsonToken#END_OBJECT} for the object containing the
   * fields. In this case the method will throw an {@link IOException} indicating the root was not
   * found.</li>
   * </ul>
   *
   * @param definition
   *          the root definition to parse
   * @return the bound object instance representing the JSON object
   * @throws IOException
   *           if an error occurred while reading the JSON
   */
  @Nullable
  public Object read(@NonNull IRootAssemblyClassBinding definition) throws IOException {
    boolean objectWrapper = false;
    if (parser.currentToken() == null) {
      parser.nextToken();
    }

    if (JsonToken.START_OBJECT.equals(parser.currentToken())) {
      // advance past the start object to the field name
      JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);
      objectWrapper = true;
    }

    String rootFieldName = definition.getRootJsonName();
    JsonToken token;
    Object instance = null;
    while (!(JsonToken.END_OBJECT.equals(token = parser.currentToken()) || token == null)) {
      if (!JsonToken.FIELD_NAME.equals(token)) {
        throw new IOException(String.format("Expected FIELD_NAME token, found '%s'", token.toString()));
      }

      String fieldName = parser.currentName();
      if (fieldName.equals(rootFieldName)) {
        // process the object value, bound to the requested class
        JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);
        JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);

        instance = readAssemblyDefinitionValue(
            definition.getRootDefinition(),
            null);

        // advance past the end object
        JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);

        // stop now, since we found the root field
        break;
      }

      if (!getProblemHandler().handleUnknownRootProperty(definition, fieldName, parser)) {
        LOGGER.warn("Skipping unhandled top-level JSON field '{}'.", fieldName);
        JsonUtil.skipNextValue(parser);
      }
    }

    if (instance == null) {
      throw new IOException(String.format("Failed to find root field '%s'.", rootFieldName));
    }

    if (objectWrapper) {
      // advance past the end object
      JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
    }

    return instance;
  }

  @Override
  public Object readDefinitionValue(IClassBinding targetDefinition, Object targetObject, boolean requiresJsonKey)
      throws IOException {
    Object retval;
    if (targetDefinition instanceof IAssemblyClassBinding) {
      retval = readAssemblyDefinitionValue((IAssemblyClassBinding) targetDefinition, targetObject);
    } else if (targetDefinition instanceof IFieldClassBinding) {
      retval = readFieldDefinitionValue((IFieldClassBinding) targetDefinition, targetObject, requiresJsonKey);
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported class binding type: %s", targetDefinition.getClass().getName()));
    }
    return retval;
  }

  /**
   * Reads a JSON/YAML object storing the associated data in the Java object {@code parentInstance}.
   * <p>
   * When called the current {@link JsonToken} of the {@link JsonParser} is expected to be a
   * {@link JsonToken#START_OBJECT}.
   * <p>
   * After returning the current {@link JsonToken} of the {@link JsonParser} is expected to be the
   * next token after the {@link JsonToken#END_OBJECT} for this class.
   *
   * @param targetDefinition
   *          the definition to parse
   * @param targetObject
   *          the parent Java object to store the data in, which can be {@code null} if there is no
   *          parent
   * @return the instance
   * @throws IOException
   *           if an error occurred while reading the parsed content
   */
  @NonNull
  protected Object readAssemblyDefinitionValue(
      @NonNull IAssemblyClassBinding targetDefinition,
      @Nullable Object targetObject) throws IOException {
    JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);

    try {
      Object instance = targetDefinition.newInstance();

      readAssemblyDefinitionContents(targetDefinition, instance, targetObject);

      return instance;
    } catch (BindingException ex) {
      throw new IOException(
          String.format("Failed to parse JSON object for '%s'", targetDefinition.getBoundClass().getName()), ex);

    }
  }

  /**
   * Parses the JSON field contents related to the bound assembly.
   * <p>
   * This method expects the parser's current token to be at the first field name to parse.
   * <p>
   * After parsing the current token will be the token at the end object immediately after all the
   * fields and values.
   *
   * @param targetDefinition
   *          the Metaschema definition for the target object being read
   * @param instance
   *          the bound object to read data into
   * @param parentInstance
   *          the parent object used for deserialization callbacks
   * @throws IOException
   *           if an error occurred while reading the JSON
   */
  protected void readAssemblyDefinitionContents(
      @NonNull IAssemblyClassBinding targetDefinition,
      @NonNull Object instance,
      @Nullable Object parentInstance) throws IOException {
    JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);

    try {
      targetDefinition.callBeforeDeserialize(instance, parentInstance);
    } catch (BindingException ex) {
      throw new IOException("an error occured calling the beforeDeserialize() method", ex);
    }

    IBoundFlagInstance jsonKey = targetDefinition.getJsonKeyFlagInstance();
    Map<String, ? extends IBoundNamedInstance> properties;
    if (jsonKey == null) {
      properties = targetDefinition.getNamedInstances(null);
    } else {
      properties = targetDefinition.getNamedInstances((flag) -> !jsonKey.equals(flag));

      // if there is a json key, the first field will be the key
      String key = ObjectUtils.notNull(parser.getCurrentName());

      Object value = jsonKey.getDefinition().getJavaTypeAdapter().parse(key);
      jsonKey.setValue(instance, value.toString());

      // advance past the FIELD_NAME
      // next the value will be a start object
      JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);
      //
      // JsonUtil.assertAndAdvance(jsonParser, JsonToken.START_OBJECT);
    }

    Set<String> handledProperties = new HashSet<>();
    while (!JsonToken.END_OBJECT.equals(parser.currentToken())) {
      String propertyName = parser.getCurrentName();
      IBoundNamedInstance property = properties.get(propertyName);

      boolean handled = false;
      if (property != null) {
        handled = readInstanceValues(property, instance);
      }

      if (handled) {
        handledProperties.add(propertyName);
      } else {
        if (LOGGER.isWarnEnabled()) {
          LOGGER.warn("Unrecognized property named '{}' at '{}'", propertyName,
              JsonUtil.toString(ObjectUtils.notNull(parser.getCurrentLocation())));
        }
        JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);
        JsonUtil.skipNextValue(parser);
      }
    }

    // set undefined properties
    // TODO: re-implement this by removing the parsed properties from the properties map to speed up
    for (Map.Entry<String, ? extends IBoundNamedInstance> entry : properties.entrySet()) {
      if (!handledProperties.contains(entry.getKey())) {
        // use the default value of the collector
        IBoundNamedInstance property = ObjectUtils.notNull(entry.getValue());
        property.setValue(instance, property.newPropertyCollector().getValue());
      }
    }

    if (jsonKey != null) {
      // read the END_OBJECT for the JSON key value
      JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
    }

    try {
      targetDefinition.callAfterDeserialize(instance, parentInstance);
    } catch (BindingException ex) {
      throw new IOException("an error occured calling the afterDeserialize() method", ex);
    }

    JsonUtil.assertCurrent(parser, JsonToken.END_OBJECT);
  }

  /**
   * Reads a JSON/YAML object storing the associated data in the Java object {@code parentInstance}.
   * <p>
   * When called the current {@link JsonToken} of the {@link JsonParser} is expected to be a
   * {@link JsonToken#START_OBJECT}.
   * <p>
   * After returning the current {@link JsonToken} of the {@link JsonParser} is expected to be the
   * next token after the {@link JsonToken#END_OBJECT} for this class.
   *
   * @param targetDefinition
   *          the definition to parse
   * @param targetObject
   *          the parent Java object to store the data in, which can be {@code null} if there is no
   *          parent
   * @param requiresJsonKey
   *          when {@code true} indicates that the item will have a JSON key
   * @return the instance
   * @throws IOException
   *           if an error occurred while reading the parsed content
   */
  @NonNull
  protected Object readFieldDefinitionValue(
      @NonNull IFieldClassBinding targetDefinition,
      @NonNull Object targetObject,
      boolean requiresJsonKey) throws IOException {
    if (requiresJsonKey) {
      // the start object has already been parsed, the next field name is the JSON key
      JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME);
    } else {
      // JsonUtil.assertAndAdvance(jsonParser, JsonToken.START_OBJECT);
      // This could be an empty assembly signified by a END_OBJECT, or a series of properties signified by
      // a FIELD_NAME
      JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
    }

    return readNormal(targetDefinition, targetObject, requiresJsonKey);
  }

  @NonNull
  private Object readNormal(
      @NonNull IFieldClassBinding targetDefinition,
      @NonNull Object parentInstance,
      boolean requiresJsonKey)
      throws IOException {
    Predicate<IBoundFlagInstance> flagFilter = null;

    IBoundFlagInstance jsonKey = null;
    if (requiresJsonKey) {
      IBoundFlagInstance instance = targetDefinition.getJsonKeyFlagInstance();
      if (instance == null) {
        throw new IOException("This property is configured to use a JSON key, but no JSON key was found");
      }

      flagFilter = (flag) -> {
        return !instance.equals(flag);
      };
      jsonKey = instance;
    }

    IBoundFlagInstance jsonValueKey = targetDefinition.getJsonValueKeyFlagInstance();
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

    Map<String, ? extends IBoundNamedInstance> properties = targetDefinition.getNamedInstances(flagFilter);

    try {
      Object instance = targetDefinition.newInstance();

      targetDefinition.callBeforeDeserialize(instance, parentInstance);

      if (jsonKey != null) {
        // if there is a json key, the first field will be the key
        String key = parser.currentName();
        assert key != null;
        jsonKey.setValue(instance, jsonKey.getDefinition().getJavaTypeAdapter().parse(key));

        // advance past the field name
        if (properties.isEmpty()) {
          // the value will be a standard value
          // advance past the field name
          JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);
        } else {
          // the value will be a start object
          JsonUtil.advanceAndAssert(parser, JsonToken.START_OBJECT);
          // advance past the start object
          parser.nextToken();
        }
      }

      Set<String> handledProperties = new HashSet<>();
      if (properties.isEmpty()) {
        // this may be a value key value, an unrecognized flag, or the field value
        IBoundFieldValueInstance fieldValue = targetDefinition.getFieldValueInstance();
        // parse the value
        Object value = fieldValue.getJavaTypeAdapter().parse(parser);
        fieldValue.setValue(instance, value);
        handledProperties.add(fieldValue.getJsonValueKeyName());
      } else {
        // This could be an empty assembly signified by a END_OBJECT, or a series of properties signified by
        // a FIELD_NAME
        JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);

        boolean parsedValueKey = false;
        // now parse each property until the end object is reached
        while (!parser.hasTokenId(JsonToken.END_OBJECT.id())) {
          String propertyName = parser.getCurrentName();
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
            handled = readInstanceValues(namedProperty, instance);
          }

          if (namedProperty == null && !parsedValueKey) {
            // this may be a value key value, an unrecognized flag, or the field value
            IBoundFieldValueInstance fieldValueInstance = targetDefinition.getFieldValueInstance();
            parsedValueKey = readFieldValueInstanceValue(fieldValueInstance, instance);

            if (parsedValueKey) {
              handled = true;
            } else {
              if (getProblemHandler().canHandleUnknownProperty(targetDefinition, propertyName, parser)) {
                handled = getProblemHandler().handleUnknownProperty(targetDefinition, propertyName, parser);
              }
            }
          }

          if (handled) {
            handledProperties.add(propertyName);
          } else {
            if (LOGGER.isWarnEnabled()) {
              LOGGER.warn("Unrecognized property named '{}' at '{}'", propertyName,
                  JsonUtil.toString(ObjectUtils.notNull(parser.getCurrentLocation())));
            }
            JsonUtil.skipNextValue(parser);
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
        JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
      }

      if (properties.isEmpty()) {
        // this is the next field or the end of the containing object of this field
        JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
      } else {
        // this is the current end element, but we are not responsible for parsing it.
        JsonUtil.assertCurrent(parser, JsonToken.END_OBJECT);
      }

      targetDefinition.callAfterDeserialize(instance, parentInstance);
      return instance;
    } catch (BindingException ex) {
      throw new IOException(ex);
    }
  }

  /**
   * Read JSON data associated with this property and apply it to the provided {@code objectInstance}
   * on which this property exists.
   * <p>
   * The parser's current token is expected to be the {@link JsonToken#FIELD_NAME} for the field value
   * being parsed.
   * <p>
   * After parsing the parser's current token will be the next token after the field's value.
   *
   * @param instance
   *          the instance to parse data for
   * @param objectInstance
   *          an instance of the class on which this property exists
   * @return {@code true} if the property was parsed, or {@code false} if the data did not contain
   *         information for this property
   * @throws IOException
   *           if there was an error when reading JSON data
   */
  public boolean readInstanceValues(
      @NonNull IBoundNamedInstance instance,
      @NonNull Object objectInstance) throws IOException {
    // the parser's current token should be the JSON field name
    JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME);

    String propertyName = parser.currentName();
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("reading property {}", propertyName);
    }

    boolean handled = instance.getJsonName().equals(propertyName);
    if (handled) {
      Object value;
      if (instance instanceof IBoundFlagInstance) {
        value = readFlagInstanceValue((IBoundFlagInstance) instance);
      } else if (instance instanceof IBoundNamedModelInstance) {
        value = readModelInstanceValue((IBoundNamedModelInstance) instance, objectInstance);
      } else {
        throw new UnsupportedOperationException(
            String.format("Unsupported instance type: %s", instance.getClass().getName()));
      }
      instance.setValue(objectInstance, value);
    }

    JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
    return handled;
  }

  @NonNull
  protected Object readFlagInstanceValue(
      @NonNull IBoundFlagInstance instance) throws IOException {
    // advance past the property name
    parser.nextFieldName();

    // parse the value
    return instance.getDefinition().getJavaTypeAdapter().parse(parser);
  }

  @Nullable
  protected Object readModelInstanceValue(
      @NonNull IBoundNamedModelInstance instance,
      @NonNull Object parentInstance) throws IOException {
    // the parser's current token should be the JSON field name
    // advance past the property name
    JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);

    IModelPropertyInfo info = instance.getPropertyInfo();
    IPropertyCollector collector = info.newPropertyCollector();

    // parse the value
    info.readValue(collector, parentInstance, this);

    JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);

    return collector.getValue();
  }

  /**
   * Read JSON data associated with this property and apply it to the provided {@code objectInstance}
   * on which this property exists.
   * <p>
   * The parser's current token is expected to be the {@link JsonToken#FIELD_NAME} for the field value
   * being parsed.
   * <p>
   * After parsing the parser's current token will be the next token after the field's value.
   *
   * @param instance
   *          the instance to parse data for
   * @param objectInstance
   *          an instance of the class on which this property exists
   * @return {@code true} if the property was parsed, or {@code false} if the data did not contain
   *         information for this property
   * @throws IOException
   *           if there was an error when reading JSON data
   */
  public boolean readFieldValueInstanceValue(
      @NonNull IBoundFieldValueInstance instance,
      @NonNull Object objectInstance) throws IOException {
    // the parser's current token should be the JSON field name
    JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME);

    boolean handled;
    IBoundFlagInstance jsonValueKey = instance.getParentClassBinding().getJsonValueKeyFlagInstance();
    if (jsonValueKey != null) {
      // assume this is the JSON value key case
      handled = true;
    } else {
      handled = instance.getJsonValueKeyName().equals(parser.currentName());
    }

    if (handled) {
      // There are two modes:
      // 1) use of a JSON value key, or
      // 2) a simple value named "value"
      if (jsonValueKey != null) {
        // this is the JSON value key case
        String fieldName = ObjectUtils.notNull(parser.currentName());
        jsonValueKey.setValue(objectInstance, jsonValueKey.getDefinition().getJavaTypeAdapter().parse(fieldName));
      } else {
        String valueKeyName = instance.getJsonValueKeyName();
        String fieldName = parser.getCurrentName();
        if (!fieldName.equals(valueKeyName)) {
          throw new IOException(
              String.format("Expecteded to parse the value property named '%s', but found a property named '%s'.",
                  valueKeyName, fieldName));
        }
      }
      // advance past the property name
      parser.nextToken();

      // parse the value
      Object retval = instance.getJavaTypeAdapter().parse(parser);
      instance.setValue(objectInstance, retval);
    }
    return handled;
  }

}
