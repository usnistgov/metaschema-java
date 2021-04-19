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

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.ModelUtil;
import gov.nist.secauto.metaschema.binding.model.annotations.Flag;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueKey;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonKey;
import gov.nist.secauto.metaschema.binding.model.property.info.PropertyCollector;
import gov.nist.secauto.metaschema.binding.model.property.info.SingletonPropertyCollector;
import gov.nist.secauto.metaschema.datatypes.adapter.JavaTypeAdapter;

import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.function.Supplier;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

public class DefaultFlagProperty
    extends AbstractNamedProperty<ClassBinding>
    implements FlagProperty {
  //  private static final Logger logger = LogManager.getLogger(DefaultFlagProperty.class);

  private final Flag flag;
  private final JavaTypeAdapter<?> javaTypeAdapter;

  public DefaultFlagProperty(ClassBinding parentClassBinding, Field field, BindingContext bindingContext) {
    super(field, parentClassBinding);
    this.flag = field.getAnnotation(Flag.class);
    this.javaTypeAdapter = bindingContext.getJavaTypeAdapterInstance(this.flag.typeAdapter());
  }

  protected Flag getFlagAnnotation() {
    return flag;
  }

  @Override
  public boolean isJsonKey() {
    return getField().isAnnotationPresent(JsonKey.class);
  }

  @Override
  public boolean isJsonValueKey() {
    return getField().isAnnotationPresent(JsonFieldValueKey.class);
  }

  public JavaTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  protected String getXmlLocalName() {
    return ModelUtil.resolveLocalName(getFlagAnnotation().name(), getJavaPropertyName());
  }

  @Override
  protected String getXmlNamespace() {
    return ModelUtil.resolveNamespace(getFlagAnnotation().namespace(), getParentClassBinding(), true);
  }

  @Override
  public boolean read(Object parentInstance, StartElement parent, XmlParsingContext context) throws IOException {
    // when reading an attribute:
    // - "parent" will contain the attributes to read
    // - the event reader "peek" will be on the end element or the next start element
    boolean handled = false;
    if (parent != null) {
      Attribute attribute = parent.getAttributeByName(getXmlQName());
      if (attribute != null) {
        // get the attribute value
        Object value = getJavaTypeAdapter().parse(attribute.getValue());
        // apply the value to the parentObject
        setValue(parentInstance, value);
        handled = true;
      }
    }
    return handled;
  }

  @Override
  public PropertyCollector newPropertyCollector() {
    return new SingletonPropertyCollector();
  }

  @Override
  public boolean readValue(PropertyCollector collector, Object parentInstance, JsonParsingContext context)
      throws IOException, BindingException {
    JavaTypeAdapter<?> adapter = getJavaTypeAdapter();

    if (adapter == null) {
      throw new BindingException(
          String.format("Unable to parse type '%s', which is not a known bound class or data type", getItemType()));
    }

    Object value = adapter.parse(context.getReader());

    boolean retval = false;
    if (value != null) {
      collector.add(value);
      retval = true;
    }
    return retval;
  }

  // TODO: implement collector?
  @Override
  public Object readValueFromString(String value) throws IOException {
    return getJavaTypeAdapter().parse(value);
  }

  @Override
  public Supplier<?> readValueAndSupply(String value) throws IOException {
    return getJavaTypeAdapter().parseAndSupply(value);
  }

  @Override
  public Supplier<?> readValueAndSupply(JsonParsingContext context) throws IOException {
    return getJavaTypeAdapter().parseAndSupply(context.getReader());
  }

  @Override
  public boolean write(Object instance, QName parentName, XmlWritingContext context)
      throws XMLStreamException, IOException {
    QName name = getXmlQName();
    String value;

    Object objectValue = getValue(instance);
    if (objectValue != null) {
      value = objectValue.toString();
    } else {
      value = null;
    }

    if (value != null) {
      XMLStreamWriter2 writer = context.getWriter();
      if (name.getNamespaceURI().isEmpty()) {
        writer.writeAttribute(name.getLocalPart(), value);
      } else {
        writer.writeAttribute(name.getNamespaceURI(), name.getLocalPart(), value);
      }
    }
    return true;
  }

  @Override
  public void write(Object instance, JsonWritingContext context) throws IOException {
    JsonGenerator writer = context.getWriter();

    Object value = getValue(instance);
    if (value != null) {
      // write the field name
      writer.writeFieldName(getJsonPropertyName());

      // write the value
      writeValue(value, context);
    }
  }

  @Override
  public String getValueAsString(Object value) throws IOException {
    return getJavaTypeAdapter().asString(getValue(value));
  }

  @Override
  public void writeValue(Object value, JsonWritingContext context) throws IOException {
    getJavaTypeAdapter().writeJsonValue(value, context.getWriter());
  }
}
