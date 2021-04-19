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
import gov.nist.secauto.metaschema.binding.model.ModelUtil;
import gov.nist.secauto.metaschema.binding.model.annotations.Field;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.annotations.NullJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.property.info.DataTypeHandler;
import gov.nist.secauto.metaschema.binding.model.property.info.PropertyCollector;
import gov.nist.secauto.metaschema.binding.model.property.info.XmlBindingSupplier;
import gov.nist.secauto.metaschema.datatypes.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatypes.util.XmlEventUtil;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class DefaultFieldProperty
    extends AbstractModelProperty
    implements ModelProperty {

  public static DefaultFieldProperty createInstance(AssemblyClassBinding parentClassBinding,
      java.lang.reflect.Field field) {
    DefaultFieldProperty retval = new DefaultFieldProperty(parentClassBinding, field);
    return retval;
  }

  private final Field field;
  private final JavaTypeAdapter<?> javaTypeAdapter;

  public DefaultFieldProperty(AssemblyClassBinding parentClassBinding, java.lang.reflect.Field field) {
    super(parentClassBinding, field);
    this.field = field.getAnnotation(Field.class);
    if (this.field == null) {
      throw new IllegalArgumentException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
          field.getName(), parentClassBinding.getBoundClass().getName(), Field.class.getName()));
    }

    Class<? extends JavaTypeAdapter<?>> adapterClass = getFieldAnnotation().typeAdapter();
    if (NullJavaTypeAdapter.class.equals(adapterClass)) {
      javaTypeAdapter = null;
    } else {
      javaTypeAdapter = getParentClassBinding().getBindingContext().getJavaTypeAdapterInstance(adapterClass);
    }

  }

  protected Field getFieldAnnotation() {
    return field;
  }

  @Override
  protected JavaTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  public boolean isXmlWrapped() {
    return getFieldAnnotation().inXmlWrapped();
  }

  @Override
  public int getMinimumOccurance() {
    return getFieldAnnotation().minOccurs();
  }

  @Override
  public int getMaximumOccurance() {
    return getFieldAnnotation().maxOccurs();
  }

  @Override
  protected String getXmlGroupLocalName() {
    return ModelUtil.resolveLocalName(getFieldAnnotation().groupName(), null);
  }

  @Override
  protected String getXmlGroupNamespace() {
    return ModelUtil.resolveNamespace(getFieldAnnotation().groupNamespace(), getParentClassBinding(), false);
  }

  @Override
  public JsonGroupAsBehavior getJsonGroupAsBehavior() {
    return getFieldAnnotation().inJson();
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    return getFieldAnnotation().inXml();
  }

  @Override
  protected String getXmlLocalName() {
    return ModelUtil.resolveLocalName(getFieldAnnotation().name(), getJavaPropertyName());
  }

  @Override
  protected String getXmlNamespace() {
    return ModelUtil.resolveNamespace(getFieldAnnotation().namespace(), getParentClassBinding(), false);
  }

  @Override
  public boolean readItem(PropertyCollector collector, Object parentInstance, StartElement start,
      XmlParsingContext context)
      throws BindingException, XMLStreamException, IOException {
    // figure out how to parse the item
    XmlBindingSupplier supplier = getBindingSupplier();

    // figure out if we need to parse the wrapper or not
    JavaTypeAdapter<?> adapter = getJavaTypeAdapter();
    boolean parseWrapper = true;
    if (adapter != null && !isXmlWrapped() && adapter.isUnrappedValueAllowedInXml()) {
      parseWrapper = false;
    }

    XMLEventReader2 eventReader = context.getReader();

    boolean handled = false;
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
      handled = supplier.get(collector, parentInstance, currentStart, context);

      if (parseWrapper) {
        // consume the end element
        XmlEventUtil.consumeAndAssert(context.getReader(), XMLEvent.END_ELEMENT, currentStart.getName());
      }
    }

    return handled;
  }

  @Override
  public boolean writeItem(Object item, QName parentName, XmlWritingContext context)
      throws XMLStreamException, IOException {
    // figure out how to parse the item
    DataTypeHandler handler = getBindingSupplier();

    // figure out if we need to parse the wrapper or not
    boolean writeWrapper = isXmlWrapped() || !handler.isUnrappedValueAllowedInXml();

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
}
