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

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.info.DataTypeHandler;
import gov.nist.secauto.metaschema.binding.model.property.info.XmlBindingSupplier;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.XmlEventUtil;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public abstract class AbstractFieldProperty
    extends AbstractNamedModelProperty
    implements FieldProperty {

  public AbstractFieldProperty(AssemblyClassBinding parentClassBinding, java.lang.reflect.Field field) {
    super(parentClassBinding, field);
  }

  @Override
  public boolean isNextProperty(XmlParsingContext context) throws XMLStreamException {
    boolean retval = super.isNextProperty(context);
    if (!retval) {
      XMLEventReader2 eventReader = context.getReader();
      XMLEvent event = eventReader.peek();
      if (event.isStartElement()) {
        QName qname = event.asStartElement().getName();
        IJavaTypeAdapter<?> adapter = getJavaTypeAdapter();
        retval = !isInXmlWrapped() && adapter.isUnrappedValueAllowedInXml() && adapter.canHandleQName(qname);
      }
    }
    return retval;
  }

  @Override
  public Object readItem(Object parentInstance, StartElement start,
      XmlParsingContext context) throws BindingException, XMLStreamException, IOException {
    // figure out how to parse the item
    XmlBindingSupplier supplier = getDataTypeHandler();

    // figure out if we need to parse the wrapper or not
    IJavaTypeAdapter<?> adapter = getJavaTypeAdapter();
    boolean parseWrapper = true;
    if (adapter != null && !isInXmlWrapped() && adapter.isUnrappedValueAllowedInXml()) {
      parseWrapper = false;
    }

    XMLEventReader2 eventReader = context.getReader();

    Object retval = null;
    StartElement currentStart = start;
    boolean parse = true; // determines if parsing happened
    if (parseWrapper) {
      // TODO: not sure this is needed, since there is a peek just before this
      // parse any whitespace before the element
      XmlEventUtil.skipWhitespace(eventReader);

      XMLEvent event = eventReader.peek();
      if (event.isStartElement() && getXmlQName().equals(event.asStartElement().getName())) {
        // Consume the start element
        currentStart
            = XmlEventUtil.consumeAndAssert(eventReader, XMLEvent.START_ELEMENT, getXmlQName()).asStartElement();
      } else {
        parse = false;
      }
    }

    if (parse) {
      // consume the value
      retval = supplier.get(parentInstance, currentStart, context);

      if (parseWrapper) {
        // consume the end element
        XmlEventUtil.consumeAndAssert(context.getReader(), XMLEvent.END_ELEMENT, currentStart.getName());
      }
    }

    return retval;
  }

  @Override
  public boolean writeItem(Object item, QName parentName, XmlWritingContext context)
      throws XMLStreamException, IOException {
    // figure out how to parse the item
    DataTypeHandler handler = getDataTypeHandler();

    // figure out if we need to parse the wrapper or not
    boolean writeWrapper = isInXmlWrapped() || !handler.isUnrappedValueAllowedInXml();

    XMLStreamWriter2 writer = context.getWriter();

    QName currentParentName;
    if (writeWrapper) {
      currentParentName = getXmlQName();
      writer.writeStartElement(currentParentName.getNamespaceURI(), currentParentName.getLocalPart());
    } else {
      currentParentName = parentName;
    }

    // write the value
    handler.accept(item, currentParentName, context);

    if (writeWrapper) {
      writer.writeEndElement();
    }
    return true;
  }

  @Override
  public AssemblyClassBinding getContainingDefinition() {
    return getParentClassBinding();
  }

  @Override
  public String toCoordinates() {
    return String.format("%s Instance(%s): %s:%s", getModelType().name().toLowerCase(), getName(),
        getParentClassBinding().getBoundClass().getName(), getField().getName());
  }

  @Override
  public MarkupMultiline getRemarks() {
    throw new UnsupportedOperationException();
  }
}