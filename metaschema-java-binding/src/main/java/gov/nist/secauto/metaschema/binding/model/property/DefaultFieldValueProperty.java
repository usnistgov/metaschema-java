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

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
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

  protected String getJsonValueKeyName() {
    String name;
    if (getParentClassBinding().getJsonValueKeyFlag() != null) {
      name = null;
    } else {
      name = getFieldValueAnnotation().name();
      if (name == null || "##none".equals(name)) {
        name = getJavaTypeAdapter().getDefaultJsonFieldName();
      }
    }
    return name;
  }

  public JavaTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  public QName getXmlQName() {
    // there is no XML QName, since the value data is represented as child text/elements of the
    // containing field's element.
    return null;
  }

  @Override
  public String getJsonPropertyName() {
    return getJsonValueKeyName();
  }

  @Override
  public boolean read(Object parentInstance, JsonParsingContext context) throws IOException, BindingException {
    boolean handled = false;
    JsonParser parser = context.getReader();
    // There are two modes:
    // 1) use of a JSON value key, or
    // 2) a simple value named "value"
    FlagProperty jsonValueKey = getParentClassBinding().getJsonValueKeyFlag();
    if (jsonValueKey != null) {
      // this is the JSON value key case
      jsonValueKey.setValue(parentInstance, jsonValueKey.readValueFromString(parser.nextFieldName()));
      handled = true;
    } else {
      if (getJsonValueKeyName().equals(parser.currentName())) {
        // this is the simple value case
        // advance past the property name
        parser.nextFieldName();
        handled = true;
      }
    }

    if (handled) {
      // parse the value
      PropertyCollector collector = newPropertyCollector();
      readValue(collector, parentInstance, context);
      setValue(parentInstance, collector.getValue());
    }

    return handled;
  }

  // TODO: add parallel readValue to JSON implementation
  @Override
  public boolean read(Object parentInstance, StartElement start, XmlParsingContext context)
      throws IOException, XMLStreamException {

    Object value = getJavaTypeAdapter().parse(context.getReader());
    setValue(parentInstance, value);
    return true;
  }

  @Override
  public PropertyCollector newPropertyCollector() {
    return new SingletonPropertyCollector();
  }

  @Override
  public boolean readValue(PropertyCollector collector, Object parentInstance, JsonParsingContext context)
      throws IOException, BindingException {
    Object value = getJavaTypeAdapter().parse(context.getReader());

    boolean retval = false;
    if (value != null) {
      collector.add(value);
      retval = true;
    }
    return retval;
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
      context.getWriter().writeFieldName(getJsonPropertyName());
      logger.info("FIELD: {}", getJsonPropertyName());
      writeValue(value, context);
    }
  }

  @Override
  public void writeValue(Object value, JsonWritingContext context) throws IOException {
    getJavaTypeAdapter().writeJsonValue(value, context.getWriter());
  }

}
