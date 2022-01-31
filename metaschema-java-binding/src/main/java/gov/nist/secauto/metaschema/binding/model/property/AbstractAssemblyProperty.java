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
import gov.nist.secauto.metaschema.binding.io.context.ParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.metapath.type.NodeItemFactory;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.datatypes.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.datatypes.util.XmlEventUtil;
import gov.nist.secauto.metaschema.model.common.metapath.format.IAssemblyPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public abstract class AbstractAssemblyProperty
    extends AbstractNamedModelProperty
    implements AssemblyProperty {

  public AbstractAssemblyProperty(AssemblyClassBinding parentClassBinding, Field field) {
    super(parentClassBinding, field);
  }

  @Override
  protected JavaTypeAdapter<?> getJavaTypeAdapter() {
    // an assembly property is always associated with a bound class, so there will never be a class
    // binding
    return null;
  }

  @Override
  public Object readItem(Object parentInstance, StartElement start,
      XmlParsingContext context) throws BindingException, XMLStreamException, IOException {
    XMLEventReader2 eventReader = context.getReader();

    // consume extra whitespace between elements
    XmlEventUtil.skipWhitespace(eventReader);

    Object retval = null;
    XMLEvent event = eventReader.peek();
    if (event.isStartElement() && getXmlQName().equals(event.asStartElement().getName())) {
      // Consume the start element
      event = eventReader.nextEvent();
      StartElement propertyStartElement = event.asStartElement();

      // consume the value
      retval = getDataTypeHandler().get(parentInstance, propertyStartElement, context);

      // consume the end element
      XmlEventUtil.consumeAndAssert(eventReader, XMLEvent.END_ELEMENT, propertyStartElement.getName());
    }
    return retval;
  }

  @Override
  public boolean writeItem(Object item, QName parentName, XmlWritingContext context)
      throws XMLStreamException, IOException {
    XMLStreamWriter2 writer = context.getWriter();

    QName currentParentName = getXmlQName();

    // write the start element
    writer.writeStartElement(currentParentName.getNamespaceURI(), currentParentName.getLocalPart());

    // write the value
    getDataTypeHandler().accept(item, currentParentName, context);

    // write the end element
    writer.writeEndElement();

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

  @Override
  public void validateValue(Object value, ParsingContext<?, ?> context) {
    context.getConstraintValidator().validateValue(this, value, context);
  }


  @Override
  public void validateItem(Object value, ParsingContext<?, ?> context) {
    context.getConstraintValidator().validateItem(this, context.getPathBuilder().getContextPathSegment(), value, context);
  }

  @Override
  public IAssemblyNodeItem newNodeItem(IAssemblyPathSegment segment, Object value, IAssemblyNodeItem parent) {
    // TODO: migrate implementation here?
    return NodeItemFactory.newNodeItem(segment, value, parent);
  }

  @Override
  public Stream<IAssemblyNodeItem> getNodeItemsFromParentInstance(IAssemblyNodeItem parentItem) {
    return getNodeItemsForValue(parentItem, getValue(parentItem.getValue()));
  }

  @Override
  public Stream<IAssemblyNodeItem> getNodeItemsForValue(IAssemblyNodeItem parentItem, Object value) {
    IAssemblyPathSegment parentPathSegment = parentItem.getPathSegment();
    return getNodeItemsForValue(parentPathSegment, value, parentItem);
  }

  @Override
  public Stream<IAssemblyNodeItem> getNodeItemsForValue(IAssemblyPathSegment parentPathSegment, Object value) {
    return getNodeItemsForValue(parentPathSegment, value, null);
  }

  protected Stream<IAssemblyNodeItem> getNodeItemsForValue(IAssemblyPathSegment parentPathSegment, Object value, IAssemblyNodeItem parentItem) {
    AtomicInteger index = new AtomicInteger();
    return getPropertyInfo().getItemsFromValue(value).map(item -> {
      IAssemblyPathSegment segment = newPathSegment(parentPathSegment, index.incrementAndGet());
      return newNodeItem(segment, item, parentItem);
    });
  }
}
