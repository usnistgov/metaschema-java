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

package gov.nist.secauto.metaschema.binding.model.property;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonUtil;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.IFieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.BoundFieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.NullJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.property.info.IPropertyCollector;
import gov.nist.secauto.metaschema.binding.model.property.info.SingletonPropertyCollector;
import gov.nist.secauto.metaschema.model.common.ModelType;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance.IInstanceSet;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public class DefaultFieldValueProperty
    extends AbstractProperty<IFieldClassBinding>
    implements IBoundFieldValueInstance, IBoundJavaField {
  private static final Logger LOGGER = LogManager.getLogger(DefaultFieldValueProperty.class);

  @NotNull
  private final Field field;
  @NotNull
  private final BoundFieldValue fieldValue;
  @NotNull
  private final IJavaTypeAdapter<?> javaTypeAdapter;

  public DefaultFieldValueProperty(@NotNull IFieldClassBinding fieldClassBinding, @NotNull Field field) {
    super(fieldClassBinding);
    this.field = ObjectUtils.requireNonNull(field, "field");
    this.fieldValue = ObjectUtils.requireNonNull(field.getAnnotation(BoundFieldValue.class));
    
    Class<? extends IJavaTypeAdapter<?>> adapterClass = ObjectUtils.notNull(fieldValue.typeAdapter());
    if (NullJavaTypeAdapter.class.equals(adapterClass)) {
      this.javaTypeAdapter = MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
    } else {
      this.javaTypeAdapter = ObjectUtils.requireNonNull(
          fieldClassBinding.getBindingContext().getJavaTypeAdapterInstance(adapterClass));
    }
  }

  @Override
  public @NotNull Field getField() {
    return field;
  }

  protected BoundFieldValue getFieldValueAnnotation() {
    return fieldValue;
  }

  @Override
  public @NotNull IFieldClassBinding getDefinition() {
    return getParentClassBinding();
  }

  @Override
  public String getJsonValueKeyName() {
    String name = getFieldValueAnnotation().name();
    if (name == null || "##none".equals(name)) {
      name = getJavaTypeAdapter().getDefaultJsonValueKey();
    }
    return name;
  }

  @Override
  public IJavaTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  public String getUseName() {
    // TODO: implement?
    return null;
  }

  @Override
  public IPropertyCollector newPropertyCollector() {
    return new SingletonPropertyCollector();
  }

  @Override
  public Object read(IJsonParsingContext context) throws IOException {
    if (getParentClassBinding().hasJsonValueKeyFlagInstance()) {
      throw new UnsupportedOperationException("for a JSON value key, use the read(Object, IJsonParsingContext) method");
    }

    Object retval = null;
    if (isNextProperty(context)) {
      JsonParser parser = context.getReader(); // NOPMD - intentional
      // advance past the property name
      parser.nextFieldName();

      retval = readInternal(context);
    }
    return retval;
  }

  @Override
  public boolean read(Object objectInstance, IJsonParsingContext context) throws IOException {
    boolean handled = isNextProperty(context);
    if (handled) {
      JsonParser parser = context.getReader();// NOPMD - intentional
      // There are two modes:
      // 1) use of a JSON value key, or
      // 2) a simple value named "value"
      IBoundFlagInstance jsonValueKey = getParentClassBinding().getJsonValueKeyFlagInstance();
      if (jsonValueKey != null) {
        // this is the JSON value key case
        String fieldName = ObjectUtils.notNull(parser.currentName());
        jsonValueKey.setValue(objectInstance, jsonValueKey.readValueFromString(fieldName));
      } else {
        String valueKeyName = getJsonValueKeyName();
        String fieldName = parser.getCurrentName();
        if (!fieldName.equals(valueKeyName)) {
          throw new IOException(
              String.format("Expecteded to parse the value property named '%s', but found a property named '%s'.",
                  valueKeyName, fieldName));
        }
      }
      // advance past the property name
      parser.nextToken();

      Object retval = readInternal(context);
      setValue(objectInstance, retval);
    }
    return handled;
  }

  @Override
  public boolean read(Object parentInstance, StartElement start, IXmlParsingContext context)
      throws IOException, XMLStreamException {
    Object value = readInternal(context);
    setValue(parentInstance, value);
    return true;
  }

  @Override
  public Object readValue(Object parentInstance, IJsonParsingContext context) throws IOException {
    return readInternal(context);
  }

  public boolean isNextProperty(IJsonParsingContext context) throws IOException {
    JsonParser parser = context.getReader(); // NOPMD - intentional

    // the parser's current token should be the JSON field name
    JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME);

    boolean handled;
    IBoundFlagInstance jsonValueKey = getParentClassBinding().getJsonValueKeyFlagInstance();
    if (jsonValueKey != null) {
      // assume this is the JSON value key case
      handled = true;
    } else {
      handled = getJsonValueKeyName().equals(parser.currentName());
    }
    return handled;
  }

  protected Object readInternal(IJsonParsingContext context) throws IOException {
    // parse the value
    return getJavaTypeAdapter().parse(context.getReader());
  }

  protected Object readInternal(IXmlParsingContext context) throws IOException {
    // parse the value
    return getJavaTypeAdapter().parse(context.getReader());
  }

  @Override
  public boolean write(Object instance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    Object value = getValue(instance);
    if (value != null) {
      getJavaTypeAdapter().writeXmlCharacters(value, parentName, context.getWriter());
    }
    return true;
  }

  @Override
  public void write(Object instance, IJsonWritingContext context) throws IOException {
    Object value = getValue(instance);
    if (value != null) {
      // There are two modes:
      // 1) use of a JSON value key, or
      // 2) a simple value named "value"
      IBoundFlagInstance jsonValueKey = getParentClassBinding().getJsonValueKeyFlagInstance();

      String valueKeyName;
      if (jsonValueKey != null) {
        // this is the JSON value key case
        valueKeyName = jsonValueKey.getValue(instance).toString();
      } else {
        valueKeyName = getJsonValueKeyName();
      }
      context.getWriter().writeFieldName(valueKeyName);
      LOGGER.info("FIELD: {}", valueKeyName);
      writeValue(value, context);
    }
  }

  @Override
  public void writeValue(Object value, IJsonWritingContext context) throws IOException {
    if (value != null) {
      getJavaTypeAdapter().writeJsonValue(value, context.getWriter());
    }
  }

  @Override
  public @NotNull ModelType getModelType() {
    // TODO: is this right? Is there a way to not make this derived from a property?
    return ModelType.FIELD;
  }

  @Override
  public @Nullable MarkupMultiline getRemarks() {
    // TODO: implement?
    return null;
  }

  @Override
  public void copyBoundObject(Object fromInstance, Object toInstance) {
    Object value = getValue(fromInstance);
    IJavaTypeAdapter<?> adapter = getJavaTypeAdapter();
    setValue(toInstance, value == null ? null : adapter.copy(value));
  }

  @Override
  public @NotNull IInstanceSet evaluateMetapathInstances(@NotNull MetapathExpression expression) {
    // TODO implement
    throw new UnsupportedOperationException();
  }
}
