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

package gov.nist.secauto.metaschema.databind.model.info;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.util.JsonUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValueInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundJavaProperty;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.IFieldClassBinding;

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
  @NonNull
  IClassBinding getClassBinding();

  @Override
  default boolean isUnwrappedValueAllowedInXml() {
    // never for a complex item
    return false;
  }

  @Override
  default Object deepCopyItem(Object item, Object parentInstance) throws BindingException {
    return getClassBinding().deepCopyItem(item, parentInstance);
  }

  /**
   * Generates a mapping of property names to associated Module instances.
   * <p>
   * If {@code requiresJsonKey} is {@code true} then the instance used as the JSON
   * key is not included in the mapping.
   * <p>
   * If the {@code targetDefinition} is an instance of {@link IFieldDefinition}
   * and a JSON value key property is configured, then the value key flag and
   * value are also omitted from the mapping. Otherwise, the value is included in
   * the mapping.
   *
   * @param jsonKey
   *          the flag instance used as the JSON key, or {@code null} otherwise
   * @return a mapping of JSON property to related Module instance
   */
  @NonNull
  default Map<String, ? extends IBoundJavaProperty> getJsonInstanceMap(@Nullable IBoundFlagInstance jsonKey) {
    IClassBinding targetDefinition = getClassBinding();

    Collection<? extends IBoundFlagInstance> flags = targetDefinition.getFlagInstances();
    int flagCount = flags.size() - (jsonKey == null ? 0 : 1);

    @SuppressWarnings("resource") Stream<? extends IBoundJavaProperty> instanceStream;
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

    if (jsonKey != null) {
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
            IBoundJavaProperty::getJsonName,
            Function.identity())));
  }

  @Nullable
  IBoundFlagInstance getJsonKey();

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

    IClassBinding definition = getClassBinding();
    Object targetObject;
    try {
      targetObject = definition.newInstance();
      definition.callBeforeDeserialize(targetObject, parent);
    } catch (BindingException ex) {
      throw new IOException(ex);
    }

    IBoundFlagInstance jsonKey = getJsonKey();
    boolean keyObjectWrapper = false;
    if (jsonKey != null) {
      // the field will be the JSON key
      String key = ObjectUtils.notNull(parser.getCurrentName());

      Object value = jsonKey.getDefinition().getJavaTypeAdapter().parse(key);
      jsonKey.setValue(targetObject, value.toString());

      // advance past the FIELD_NAME
      JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);

      keyObjectWrapper = JsonToken.START_OBJECT.equals(parser.currentToken());
      if (keyObjectWrapper) {
        JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);
      }
    }

    if (keyObjectWrapper || JsonToken.FIELD_NAME.equals(parser.currentToken())) {
      context.readDefinitionValue(definition, targetObject, getJsonInstanceMap(jsonKey));
    } else if (parser.currentToken().isScalarValue()) {
      // REFACTOR: need to figure out why this special case exists
      // this is just a value
      IFieldClassBinding fieldDefinition = (IFieldClassBinding) definition;
      Object fieldValue = fieldDefinition.getJavaTypeAdapter().parse(parser);
      fieldDefinition.getFieldValueInstance().setValue(targetObject, fieldValue);
    }

    try {
      definition.callAfterDeserialize(targetObject, parent);
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
    return context.readDefinitionValue(getClassBinding(), parent, start);
  }

  @Override
  default void writeItem(Object item, QName currentParentName, IXmlWritingContext context)
      throws IOException, XMLStreamException {
    context.writeDefinitionValue(getClassBinding(), item, currentParentName);
  }

  @SuppressWarnings("resource") // not owned
  @Override
  default void writeItem(Object item, IJsonWritingContext context, IBoundFlagInstance jsonKey) throws IOException {
    JsonGenerator writer = context.getWriter();

    writer.writeStartObject();

    IClassBinding definition = getClassBinding();
    if (jsonKey != null) {
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
        definition,
        item,
        getJsonInstanceMap(jsonKey));

    if (jsonKey != null) {
      writer.writeEndObject();
    }

    writer.writeEndObject();
  }
}
