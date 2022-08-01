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

package gov.nist.secauto.metaschema.binding.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Supplier;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import edu.umd.cs.findbugs.annotations.NonNull;

abstract class AbstractFlagProperty
    extends AbstractNamedProperty<IClassBinding>
    implements IBoundFlagInstance {

  public AbstractFlagProperty(@NonNull IClassBinding parentClassBinding) {
    super(parentClassBinding);
  }

  @Override
  public IPropertyCollector newPropertyCollector() {
    return new SingletonPropertyCollector();
  }

  @Override
  public void copyBoundObject(Object fromInstance, Object toInstance) {
    Object value = getValue(fromInstance);
    IDataTypeAdapter<?> adapter = getDefinition().getJavaTypeAdapter();
    setValue(toInstance, value == null ? null : adapter.copy(value));
  }

  @Override
  public boolean read(Object parentInstance, StartElement parent, IXmlParsingContext context) throws IOException {

    // when reading an attribute:
    // - "parent" will contain the attributes to read
    // - the event reader "peek" will be on the end element or the next start element
    boolean handled = false;
    Attribute attribute = parent.getAttributeByName(getXmlQName());
    if (attribute != null) {
      // get the attribute value
      Object value = getDefinition().getJavaTypeAdapter().parse(ObjectUtils.notNull(attribute.getValue()));
      // apply the value to the parentObject
      setValue(parentInstance, value);

      handled = true;
    }
    return handled;
  }

  @Override
  protected Object readInternal(Object parentInstance, IJsonParsingContext context) throws IOException {
    JsonParser parser = context.getReader();// NOPMD - intentional

    // advance past the property name
    parser.nextFieldName();

    // parse the value
    return readValueAndSupply(context).get();
  }

  // TODO: implement collector?
  @Override
  public Object readValueFromString(String value) throws IOException {
    return getDefinition().getJavaTypeAdapter().parse(value);
  }

  @Override
  public Supplier<?> readValueAndSupply(String value) throws IOException {
    return getDefinition().getJavaTypeAdapter().parseAndSupply(value);
  }

  @Override
  public Supplier<?> readValueAndSupply(IJsonParsingContext context) throws IOException {
    return getDefinition().getJavaTypeAdapter().parseAndSupply(context.getReader());
  }

  @Override
  public boolean write(Object instance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    Object objectValue = getValue(instance);
    String value = objectValue == null ? null : getDefinition().getJavaTypeAdapter().asString(objectValue);

    if (value != null) {
      QName name = getXmlQName();
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
  public void write(Object instance, IJsonWritingContext context) throws IOException {
    JsonGenerator writer = context.getWriter(); // NOPMD - intentional

    Object value = getValue(instance);
    if (value != null) {
      // write the field name
      writer.writeFieldName(getJsonName());

      // write the value
      writeValue(value, context);
    }
  }

  @Override
  public String getValueAsString(Object value) {
    return value == null ? null : getDefinition().getJavaTypeAdapter().asString(value);
  }

  @Override
  public void writeValue(@NonNull Object value, IJsonWritingContext context) throws IOException {
    getDefinition().getJavaTypeAdapter().writeJsonValue(value, context.getWriter());
  }

  @SuppressWarnings("null")
  @Override
  public String toCoordinates() {
    return String.format("%s Instance(%s): %s",
        getModelType().name().toLowerCase(Locale.ROOT),
        getParentClassBinding().getBoundClass().getName(),
        getName());
  }
}
