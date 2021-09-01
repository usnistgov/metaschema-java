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

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.context.ParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonUtil;
import gov.nist.secauto.metaschema.binding.io.json.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.model.property.info.PropertyCollector;
import gov.nist.secauto.metaschema.binding.model.property.info.SingletonPropertyCollector;
import gov.nist.secauto.metaschema.datatypes.adapter.JavaTypeAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public class DefaultFieldValueProperty
    extends AbstractProperty<FieldClassBinding>
    implements FieldValueProperty {
  private static final Logger logger = LogManager.getLogger(DefaultFieldValueProperty.class);

  private final FieldValue fieldValue;
  private final JavaTypeAdapter<?> javaTypeAdapter;

  public DefaultFieldValueProperty(FieldClassBinding fieldClassBinding, Field field) {
    super(field, fieldClassBinding);
    this.fieldValue = field.getAnnotation(FieldValue.class);
    this.javaTypeAdapter = fieldClassBinding.getBindingContext().getJavaTypeAdapterInstance(fieldValue.typeAdapter());
  }


  protected FieldValue getFieldValueAnnotation() {
    return fieldValue;
  }

  @Override
  public String getJsonValueKeyName() {
    String name = getFieldValueAnnotation().name();
    if ("##none".equals(name)) {
      name = getJavaTypeAdapter().getDefaultJsonValueKey();
    }
    return name;
  }

  @Override
  public JavaTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  public Object read(XmlParsingContext context) throws IOException, BindingException, XMLStreamException {
    return readInternal(context);
  }

  @Override
  public boolean read(Object parentInstance, StartElement start, XmlParsingContext context)
      throws IOException, XMLStreamException, BindingException {
    Object value = readInternal(context);
    setValue(parentInstance, value);
    return true;
  }

  protected Object readInternal(XmlParsingContext context) throws IOException, BindingException {
    // parse the value
    Object retval = getJavaTypeAdapter().parse(context.getReader());

    // validate the flag value
    if (context.isValidating()) {
      validateValue(retval, context);
    }
    return retval;
  }

  public boolean isNextProperty(JsonParsingContext context) throws IOException {
    JsonParser parser = context.getReader();

    // the parser's current token should be the JSON field name
    JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME);

    boolean handled = false;
    FlagProperty jsonValueKey = getParentClassBinding().getJsonValueKeyFlagInstance();
    if (jsonValueKey != null) {
      // assume this is the JSON value key case
      handled = true;
    } else {
      handled = getJsonValueKeyName().equals(parser.currentName());
    }
    return handled;
  }

  @Override
  public Object read(JsonParsingContext context) throws IOException, BindingException {
    if (getParentClassBinding().hasJsonValueKeyFlagInstance()) {
      throw new UnsupportedOperationException("for a JSON value key, use the read(Object, JsonParsingContext) method");
    }

    Object retval = null;
    if (isNextProperty(context)) {
      JsonParser parser = context.getReader();
      // advance past the property name
      parser.nextFieldName();

      retval = readInternal(context);
    }
    return retval;
  }

  protected Object readInternal(JsonParsingContext context) throws IOException, BindingException {
    // parse the value
    return getJavaTypeAdapter().parse(context.getReader());
  }

  @Override
  public boolean read(Object parentInstance, JsonParsingContext context) throws IOException, BindingException {
    boolean handled = isNextProperty(context);
    if (handled) {
      JsonParser parser = context.getReader();
      // There are two modes:
      // 1) use of a JSON value key, or
      // 2) a simple value named "value"
      FlagProperty jsonValueKey = getParentClassBinding().getJsonValueKeyFlagInstance();
      if (jsonValueKey != null) {
        // this is the JSON value key case
        jsonValueKey.setValue(parentInstance, jsonValueKey.readValueFromString(parser.nextFieldName()));
      } else {
        // advance past the property name
        parser.nextFieldName();
      }

      Object retval = readInternal(context);
      setValue(parentInstance, retval);
    }
    return handled;
  }

  @Override
  public PropertyCollector newPropertyCollector() {
    return new SingletonPropertyCollector();
  }

  @Override
  public Object readValue(Object parentInstance, JsonParsingContext context) throws IOException, BindingException {
    return readInternal(context);
  }

  @Override
  public boolean write(Object instance, QName parentName, XmlWritingContext context)
      throws XMLStreamException, IOException {
    Object value = getValue(instance);
    if (value != null) {
      getJavaTypeAdapter().writeXmlCharacters(value, parentName, context.getWriter());
    }
    return true;
  }

  @Override
  public void write(Object instance, JsonWritingContext context) throws IOException {
    Object value = getValue(instance);
    if (value != null) {
      // There are two modes:
      // 1) use of a JSON value key, or
      // 2) a simple value named "value"
      FlagProperty jsonValueKey = getParentClassBinding().getJsonValueKeyFlagInstance();

      String valueKeyName;
      if (jsonValueKey != null) {
        // this is the JSON value key case
        valueKeyName = jsonValueKey.getValue(instance).toString();
      } else {
        valueKeyName = getJsonValueKeyName();
      }
      context.getWriter().writeFieldName(valueKeyName);
      logger.info("FIELD: {}", valueKeyName);
      writeValue(value, context);
    }
  }

  @Override
  public void writeValue(Object value, JsonWritingContext context) throws IOException {
    getJavaTypeAdapter().writeJsonValue(value, context.getWriter());
  }

  @Override
  public void validateValue(Object value, ParsingContext<?, ?> context) {
    // TODO Auto-generated method stub

  }

  @Override
  public FieldClassBinding getContainingDefinition() {
    return getParentClassBinding();
  }
}
