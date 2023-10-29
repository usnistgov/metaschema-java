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

package gov.nist.secauto.metaschema.databind.strategy.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.model.util.JsonUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.info.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.info.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundFieldValueInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundNamedInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IFieldClassBinding;
import gov.nist.secauto.metaschema.databind.strategy.IClassBindingStrategy;
import gov.nist.secauto.metaschema.databind.strategy.IFlagInstanceBindingStrategy;
import gov.nist.secauto.metaschema.databind.strategy.IItemValueHandler;
import gov.nist.secauto.metaschema.databind.strategy.IPropertyBindingStrategy;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IFeatureComplexItemValueHandler extends IItemValueHandler {
  @Override
  default boolean isUnwrappedValueAllowedInXml() {
    // complex data is always wrapped
    return false;
  }

  @NonNull
  IClassBindingStrategy<? extends IFlagContainer> getBindingStrategy();

  /**
   * Generates a mapping of property names to associated Module instances.
   * <p>
   * If {@code requiresJsonKey} is {@code true} then the instance used as the JSON
   * key is not included in the mapping.
   * <p>
   * If the {@code targetDefinition} is an instance of {@link IFieldDefinition}
   * and a JSON value key property is configured, then the value key flag and
   * value are also ommitted from the mapping. Otherwise, the value is included in
   * the mapping.
   *
   * @param targetDefinition
   *          the Module bound definition to generate the instance map for
   * @param requiresJsonKey
   *          if {@code true} then the instance used as the JSON key is not
   *          included in the mapping, or {@code false} otherwise
   * @return a mapping of JSON property to related Module instance
   */
  @NonNull
  private static Map<String, ? extends IBoundNamedInstance> getInstancesToParse(
      @NonNull IClassBinding targetDefinition,
      boolean requiresJsonKey) {
    Collection<? extends IBoundFlagInstance> flags = targetDefinition.getFlagInstances();
    int flagCount = flags.size() - (requiresJsonKey ? 1 : 0);

    @SuppressWarnings("resource") Stream<? extends IBoundNamedInstance> instanceStream;
    if (targetDefinition instanceof IAssemblyClassBinding) {
      // use all child instances
      instanceStream = ((IAssemblyClassBinding) targetDefinition).getModelInstances().stream();
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
        Collectors.toUnmodifiableMap(
            IBoundNamedInstance::getJsonName,
            Function.identity())));
  }

  ClassDataTypeHandler(
      @Nullable IBoundNamedModelInstance targetInstance,
      @NonNull IClassBinding classBinding) {
    this.classBinding = classBinding;

    this.jsonKeyRequired = targetInstance != null && targetInstance.getPropertyInfo().isJsonKeyRequired();
    this.propertyMap = ObjectUtils.notNull(Lazy.lazy(() -> getInstancesToParse(
        classBinding,
        this.jsonKeyRequired)));
  }

  @NonNull
  protected Map<String, ? extends IPropertyBindingStrategy> getJsonInstanceMap() {
    return ObjectUtils.notNull(propertyMap.get());
  }

  @SuppressWarnings("resource") // not owned
  private boolean readItemJsonKey(
      @NonNull Object item,
      IJsonParsingContext context) throws IOException {
    JsonParser parser = context.getReader(); // NOPMD - intentional

    IFlagInstanceBindingStrategy jsonKey = getBindingStrategy().getJsonKey(item);
    if (jsonKey == null) {
      throw new IOException(String.format("JSON key not defined for object '%s'%s",
          getBindingStrategy().toCoordinates(), JsonUtil.generateLocationMessage(parser)));
    }

    // the field will be the JSON key
    String key = ObjectUtils.notNull(parser.getCurrentName());

    Object value = jsonKey.getValueFromString(key);
    jsonKey.setValue(item, value);

    // advance past the FIELD_NAME
    JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);

    boolean keyObjectWrapper = JsonToken.START_OBJECT.equals(parser.currentToken());
    if (keyObjectWrapper) {
      JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);
    }

    return keyObjectWrapper;
  }

  @SuppressWarnings({
      "resource", // not owned
      "PMD.NPathComplexity", "PMD.CyclomaticComplexity" // ok
  })
  @Override
  default Object readItem(Object parent, IJsonParsingContext context)
      throws IOException {
    JsonParser parser = context.getReader(); // NOPMD - intentional
    boolean objectWrapper = JsonToken.START_OBJECT.equals(parser.currentToken());
    if (objectWrapper) {
      JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);
    }

    Object targetObject;
    try {
      targetObject = getBindingStrategy().newInstance();
      getBindingStrategy().callBeforeDeserialize(targetObject, parent);
    } catch (BindingException ex) {
      throw new IOException(ex);
    }

    boolean keyObjectWrapper = false;
    if (isJsonKeyRequired()) {
      keyObjectWrapper = readItemJsonKey(targetObject, context);
    }

    IClassBindingStrategy<?> bindingStrategy = getBindingStrategy();
    if (keyObjectWrapper || JsonToken.FIELD_NAME.equals(parser.currentToken())) {
      context.readDefinitionValue(bindingStrategy, targetObject, getJsonInstanceMap());
    } else if (parser.currentToken().isScalarValue()) {
      // HELP: need to figure out why this special case exists
      // this is just a value
      throw new UnsupportedOperationException();
      // IFieldClassBinding fieldDefinition = (IFieldClassBinding) definition;
      // Object fieldValue = fieldDefinition.getJavaTypeAdapter().parse(parser);
      // fieldDefinition.getFieldValueInstance().setValue(targetObject, fieldValue);
    }

    try {
      getBindingStrategy().callAfterDeserialize(targetObject, parent);
    } catch (BindingException ex) {
      throw new IOException(ex);
    }

    if (keyObjectWrapper) {
      // advance past the END_OBJECT for the JSON key
      JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
    }

    if (objectWrapper) {
      JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
    }
    return ObjectUtils.asType(targetObject);
  }

  @Override
  default Object readItem(Object parent, StartElement start, IXmlParsingContext context)
      throws IOException, XMLStreamException {
    return context.readDefinitionValue(getBindingStrategy(), parent, start);
  }

  @Override
  default void writeItem(Object item, QName currentParentName, IXmlWritingContext context)
      throws IOException, XMLStreamException {
    context.writeDefinitionValue(getBindingStrategy(), item, currentParentName);
  }

  @SuppressWarnings("resource") // not owned
  @Override
  default void writeItem(Object item, IJsonWritingContext context) throws IOException {
    JsonGenerator writer = context.getWriter();

    writer.writeStartObject();

    boolean writeJsonKey = isJsonKeyRequired();
    if (writeJsonKey) {
      IFlagInstanceBindingStrategy jsonKey = getBindingStrategy().getJsonKey(item);
      assert jsonKey != null;

      // the field will be the JSON key
      String key = jsonKey.toStringFromItem(item);
      if (key == null) {
        throw new IOException(new NullPointerException("Null key value"));
      }
      writer.writeFieldName(key);

      // next the value will be a start object
      writer.writeStartObject();
    }

    context.writeDefinitionValue(
        getBindingStrategy(),
        item,
        getJsonInstanceMap());

    if (writeJsonKey) {
      writer.writeEndObject();
    }

    writer.writeEndObject();
  }
}
