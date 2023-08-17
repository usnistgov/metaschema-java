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

import gov.nist.secauto.metaschema.core.model.util.JsonUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValueInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundNamedInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.IFieldClassBinding;
import gov.nist.secauto.metaschema.databind.model.info.IModelPropertyInfo;
import gov.nist.secauto.metaschema.databind.model.info.IPropertyCollector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class MetaschemaJsonReader
    implements IJsonParsingContext {
  private static final Logger LOGGER = LogManager.getLogger(MetaschemaJsonReader.class);

  @NonNull
  private final JsonParser parser;
  @NonNull
  private final IJsonProblemHandler problemHandler;

  /**
   * Construct a new Metaschema-aware JSON parser using the default problem
   * handler.
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
   * Construct a new Metaschema-aware JSON parser.
   *
   * @param parser
   *          the JSON parser to parse with
   * @param problemHandler
   *          the problem handler implementation to use
   */
  public MetaschemaJsonReader(
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
   * Parses JSON into a bound object. This assembly must be a root assembly for
   * which a call to {@link IAssemblyClassBinding#isRoot()} will return
   * {@code true}.
   * <p>
   * This method expects the parser's current token to be:
   * <ul>
   * <li>{@code null} indicating that the parser has not yet parsed a JSON
   * node;</li>
   * <li>a {@link JsonToken#START_OBJECT} which represents the object wrapper
   * containing the root field,</li>
   * <li>a {@link JsonToken#FIELD_NAME} representing the root field to parse,
   * or</li>
   * <li>a peer field to the root field that will be handled by the
   * {@link IJsonProblemHandler#handleUnknownProperty(IClassBinding, Object, String, IJsonParsingContext)}
   * method.</li>
   * </ul>
   * <p>
   * After parsing the current token will be:
   * <ul>
   * <li>the next token after the {@link JsonToken#END_OBJECT} corresponding to
   * the initial {@link JsonToken#START_OBJECT} parsed by this method;</li>
   * <li>the next token after the {@link JsonToken#END_OBJECT} for the root
   * field's value; or</li>
   * <li>the next token after all fields and associated values have been parsed
   * looking for the root field. This next token will be the
   * {@link JsonToken#END_OBJECT} for the object containing the fields. In this
   * case the method will throw an {@link IOException} indicating the root was not
   * found.</li>
   * </ul>
   *
   * @param <T>
   *          the Java type of the resulting bound instance
   * @param definition
   *          the root definition to parse
   * @return the bound object instance representing the JSON object
   * @throws IOException
   *           if an error occurred while parsing the JSON
   */
  @SuppressWarnings({
      "PMD.CyclomaticComplexity" // acceptable
  })
  @Nullable
  public <T> T read(@NonNull IAssemblyClassBinding definition) throws IOException {
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

        instance = readDefinitionValue(
            definition,
            null,
            false);

        // advance past the end object
        JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);

        // stop now, since we found the root field
        break;
      }

      if (!getProblemHandler().handleUnknownProperty(definition, instance, fieldName, this)) {
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

    return ObjectUtils.asType(instance);
  }

  /**
   * Read the data associated with the {@code instance} and apply it to the
   * provided {@code parentObject}.
   * <p>
   * Consumes the field if the field's name matches. If it matches, then
   * {@code true} is returned after parsing the value. Otherwise, {@code false} is
   * returned to indicate the property was not parsed.
   *
   * @param instance
   *          the instance to parse data for
   * @param parentObject
   *          the Java object that data parsed by this method will be stored in
   * @return {@code true} if the instance was parsed, or {@code false} if the data
   *         did not contain information for this instance
   * @throws IOException
   *           if an error occurred while parsing the input
   */
  protected boolean readInstance(
      @NonNull IBoundNamedInstance instance,
      @NonNull Object parentObject) throws IOException {
    // the parser's current token should be the JSON field name
    JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME);

    String propertyName = parser.currentName();
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("reading property {}", propertyName);
    }

    boolean handled = instance.getJsonName().equals(propertyName);
    if (handled) {
      // advance past the field name
      parser.nextToken();

      Object value = readInstanceValue(instance, parentObject);

      if (value != null) {
        instance.setValue(parentObject, value);
      }
    }

    // the current token will be either the next instance field name or the end of
    // the parent object
    JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
    return handled;
  }

  /**
   * Read the data associated with the {@code instance}.
   *
   * @param instance
   *          the instance that describes the syntax of the data to read
   * @param parentObject
   *          the Java object that data parsed by this method will be stored in
   * @return the parsed value(s)
   * @throws IOException
   *           if an error occurred while parsing the input
   */
  protected Object readInstanceValue(
      @NonNull IBoundNamedInstance instance,
      @NonNull Object parentObject) throws IOException {
    Object value;
    if (instance instanceof IBoundNamedModelInstance) {

      // Deal with the collection or value type
      IModelPropertyInfo info = ((IBoundNamedModelInstance) instance).getPropertyInfo();
      IPropertyCollector collector = info.newPropertyCollector();

      // let the property info parse the value
      info.readValue(collector, parentObject, this);

      // get the underlying value
      value = collector.getValue();
    } else if (instance instanceof IBoundFlagInstance) {
      // just read the value directly
      value = ((IBoundFlagInstance) instance).getDefinition().getJavaTypeAdapter().parse(parser);
    } else if (instance instanceof IBoundFieldValueInstance) {
      // just read the value directly
      value = ((IBoundFieldValueInstance) instance).getJavaTypeAdapter().parse(parser);
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported instance type: %s", instance.getClass().getName()));
    }
    return value;
  }

  @NonNull
  private static Map<String, ? extends IBoundNamedInstance> getInstancesToParse(
      @NonNull IClassBinding targetDefinition,
      boolean requiresJsonKey) {
    Collection<? extends IBoundFlagInstance> flags = targetDefinition.getFlagInstances();
    int flagCount = flags.size() - (requiresJsonKey ? 1 : 0);

    @SuppressWarnings("resource") Stream<? extends IBoundNamedInstance> instanceStream;
    if (targetDefinition instanceof IAssemblyClassBinding) {
      instanceStream = ((IAssemblyClassBinding) targetDefinition).getModelInstances().stream();
      // .flatMap((instance) -> {
      // return instance instanceof IChoiceInstance ?
      // ((IChoiceInstance)instance).getNamedModelInstances().stream()
      // });
    } else if (targetDefinition instanceof IFieldClassBinding) {
      IFieldClassBinding targetFieldDefinition = (IFieldClassBinding) targetDefinition;

      IBoundFlagInstance jsonValueKeyFlag = targetFieldDefinition.getJsonValueKeyFlagInstance();
      if (jsonValueKeyFlag == null && flagCount > 0) {
        // the field value is handled as named field
        IBoundFieldValueInstance fieldValue = targetFieldDefinition.getFieldValueInstance();
        instanceStream = Stream.of(fieldValue);
      } else {
        // only the value, with no flags or a JSON value key flag
        instanceStream = Stream.empty();
      }
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported class binding type: %s", targetDefinition.getClass().getName()));
    }

    if (requiresJsonKey) {
      IBoundFlagInstance jsonKey = targetDefinition.getJsonKeyFlagInstance();
      assert jsonKey != null;
      instanceStream = Stream.concat(
          flags.stream().filter((flag) -> !jsonKey.equals(flag)),
          instanceStream);
    } else {
      instanceStream = Stream.concat(
          flags.stream(),
          instanceStream);
    }
    return ObjectUtils.notNull(instanceStream.collect(
        Collectors.toMap(
            IBoundNamedInstance::getJsonName,
            Function.identity())));
  }

  @Override
  public <T> T readDefinitionValue(IClassBinding targetDefinition, Object parentObject, boolean requiresJsonKey)
      throws IOException {
    Object targetObject;
    try {
      targetObject = targetDefinition.newInstance();
      targetDefinition.callBeforeDeserialize(targetObject, parentObject);
    } catch (BindingException ex) {
      throw new IOException(ex);
    }

    readDefinitionValueContents(targetDefinition, targetObject, requiresJsonKey);

    try {
      targetDefinition.callAfterDeserialize(targetObject, parentObject);
    } catch (BindingException ex) {
      throw new IOException(ex);
    }
    return ObjectUtils.asType(targetObject);
  }

  @SuppressFBWarnings(value = "UC_USELESS_CONDITION", justification = "false positive")
  private void readDefinitionValueContents(
      @NonNull IClassBinding targetDefinition,
      @NonNull Object targetObject,
      boolean requiresJsonKey) throws IOException {
    boolean keyObjectWrapper = false;
    if (requiresJsonKey) {
      readDefinitionJsonKey(targetDefinition, targetObject);

      keyObjectWrapper = JsonToken.START_OBJECT.equals(parser.currentToken());
      if (keyObjectWrapper) {
        JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);
      }
    }

    if (keyObjectWrapper || JsonToken.FIELD_NAME.equals(parser.currentToken())) {
      Map<String, ? extends IBoundNamedInstance> properties = getInstancesToParse(targetDefinition, requiresJsonKey);
      readDefinitionContents(targetDefinition, targetObject, properties);
    } else if (parser.currentToken().isScalarValue()) {
      // this is a value
      IFieldClassBinding fieldDefinition = (IFieldClassBinding) targetDefinition;
      Object fieldValue = fieldDefinition.getJavaTypeAdapter().parse(parser);
      fieldDefinition.getFieldValueInstance().setValue(targetObject, fieldValue);
    }

    if (keyObjectWrapper) {
      // advance past the END_OBJECT for the JSON key
      JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
    }
  }

  private void readDefinitionJsonKey(
      @NonNull IClassBinding targetDefinition,
      @NonNull Object targetObject) throws IOException {
    IBoundFlagInstance jsonKey = targetDefinition.getJsonKeyFlagInstance();
    if (jsonKey == null) {
      throw new IOException(String.format("JSON key not defined for object '%s'%s",
          targetDefinition.toCoordinates(), JsonUtil.generateLocationMessage(parser)));
    }

    // the field will be the JSON key
    String key = ObjectUtils.notNull(parser.getCurrentName());

    Object value = jsonKey.getDefinition().getJavaTypeAdapter().parse(key);
    jsonKey.setValue(targetObject, value.toString());

    // advance past the FIELD_NAME
    JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);
  }

  @SuppressWarnings({
      "PMD.NullAssignment", // readability
      "PMD.CyclomaticComplexity", // acceptable
      "PMD.CognitiveComplexity" // acceptable
  })
  private void readDefinitionContents(
      @NonNull IClassBinding targetDefinition,
      @NonNull Object targetObject,
      @NonNull Map<String, ? extends IBoundNamedInstance> instances) throws IOException {

    IBoundFlagInstance valueKeyFlag = null;
    if (targetDefinition instanceof IFieldClassBinding) {
      IFieldClassBinding targetFieldDefinition = (IFieldClassBinding) targetDefinition;
      valueKeyFlag = targetFieldDefinition.getJsonValueKeyFlagInstance();
    }

    while (!JsonToken.END_OBJECT.equals(parser.currentToken())) {

      boolean handled = false;
      String propertyName = parser.getCurrentName();
      assert propertyName != null;

      if (JsonToken.FIELD_NAME.equals(parser.currentToken())) {
        IBoundNamedInstance property = instances.get(propertyName);
        if (property != null) {
          handled = readInstance(property, targetObject);
          instances.remove(propertyName);
        }
      } else {
        throw new IOException(
            String.format("Unexpected token: " + JsonUtil.toString(parser)));
      }

      if (!handled) {
        // LOGGER.atInfo().log("Current token: " + JsonUtil.toString(parser));
        if (valueKeyFlag != null) {
          // Handle JSON value key flag case
          IFieldClassBinding targetFieldDefinition = (IFieldClassBinding) targetDefinition;
          valueKeyFlag.setValue(targetObject,
              valueKeyFlag.getDefinition().getJavaTypeAdapter().parse(propertyName));

          // advance past the FIELD_NAME to get the value
          JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);

          IBoundFieldValueInstance fieldValue = targetFieldDefinition.getFieldValueInstance();
          fieldValue.setValue(
              targetObject,
              fieldValue.getJavaTypeAdapter().parse(parser));
          valueKeyFlag = null;
        } else if (!getProblemHandler().handleUnknownProperty(
            targetDefinition,
            targetObject,
            propertyName,
            this)) {
          if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Unrecognized property named '{}' at '{}'", propertyName,
                JsonUtil.toString(ObjectUtils.notNull(parser.getCurrentLocation())));
          }
          JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);
          JsonUtil.skipNextValue(parser);
        }
      }
    }

    if (!instances.isEmpty()) {
      getProblemHandler().handleMissingInstances(
          targetDefinition,
          targetObject,
          ObjectUtils.notNull(instances.values()));
    }
  }
}
