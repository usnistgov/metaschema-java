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
import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.info.ClassDataTypeHandler;
import gov.nist.secauto.metaschema.binding.model.property.info.IDataTypeHandler;
import gov.nist.secauto.metaschema.binding.model.property.info.IModelPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.info.IPropertyCollector;
import gov.nist.secauto.metaschema.binding.model.property.info.JavaTypeAdapterDataTypeHandler;
import gov.nist.secauto.metaschema.binding.model.property.info.ListPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.info.MapPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.info.SingletonPropertyInfo;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.instance.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.util.XmlEventUtil;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public abstract class AbstractNamedModelProperty
    extends AbstractNamedProperty<IAssemblyClassBinding>
    implements IBoundNamedModelInstance {
  // private static final Logger logger = LogManager.getLogger(AbstractNamedModelProperty.class);

  private IModelPropertyInfo propertyInfo;
  private IDataTypeHandler dataTypeHandler;

  protected AbstractNamedModelProperty(IAssemblyClassBinding parentClassBinding, Field field) {
    super(field, parentClassBinding);
  }

  @SuppressWarnings("PMD")
  @Override
  public IMetaschema getContainingMetaschema() {
    return null;
  }

  protected abstract IJavaTypeAdapter<?> getJavaTypeAdapter();

  @Override
  public Class<?> getItemType() {
    return getPropertyInfo().getItemType();
  }

  protected IModelPropertyInfo newPropertyInfo() {
    // create the property info
    Type type = getField().getGenericType();

    IModelPropertyInfo retval;
    if (type instanceof ParameterizedType) {
      if (getMaxOccurs() == -1 || getMaxOccurs() > 1) {
        if (JsonGroupAsBehavior.KEYED.equals(getJsonGroupAsBehavior())) {
          retval = new MapPropertyInfo(this);
        } else {
          retval = new ListPropertyInfo(this);
        }
      } else {
        throw new IllegalStateException(String.format(
            "The field '%s' on class '%s' has a data parmeterized type of '%s',"
                + " but the occurance is not multi-valued.",
            getField().getName(), getParentClassBinding().getBoundClass().getName(), getField().getType().getName()));
      }
    } else {
      if (getMaxOccurs() == -1 || getMaxOccurs() > 1) {
        switch (getJsonGroupAsBehavior()) {
        case KEYED:
          throw new IllegalStateException(
              String.format("The field '%s' on class '%s' has data type of '%s'," + " but should have a type of '%s'.",
                  getField().getName(), getParentClassBinding().getBoundClass().getName(),
                  getField().getType().getName(), Map.class.getName()));
        case LIST:
        case SINGLETON_OR_LIST:
          throw new IllegalStateException(
              String.format("The field '%s' on class '%s' has data type of '%s'," + " but should have a type of '%s'.",
                  getField().getName(), getParentClassBinding().getBoundClass().getName(),
                  getField().getType().getName(), List.class.getName()));
        default:
          // this should not occur
          throw new IllegalStateException(getJsonGroupAsBehavior().name());
        }
      }
      retval = new SingletonPropertyInfo(this);
    }
    return retval;
  }
  //
  // @Override
  // public Stream<INodeItem> newNodeItems(Object value, List<IPathSegment> precedingPath) {
  // AtomicInteger index = new AtomicInteger();
  // return getItemsFromValue(value).map(item -> {
  // // build a positional index of the values
  // final Integer position = index.incrementAndGet();
  // return new TerminalNodeItem(item, new this.newPathSegment(position), precedingPath);
  // });
  // }

  // @Override
  // public INodeItem newNodeItem(Object item, List<IPathSegment> precedingPath) {
  // return new TerminalNodeItem(item, this.newPathSegment(1), precedingPath);
  // }

  // @Override
  // public Stream<? extends INodeItem> getNodeItemsFromParentInstance(IAssemblyNodeItem parentItem,
  // Object parentValue) {
  // return newNodeItems(parentItem, getPropertyInfo().getItemsFromParentInstance(parentValue));
  // }

  /**
   * Gets information about the bound property.
   * 
   * @return the property information for the bound property
   */
  protected synchronized IModelPropertyInfo getPropertyInfo() {
    if (propertyInfo == null) {
      propertyInfo = newPropertyInfo();
      assert this.propertyInfo != null;
    }
    return propertyInfo;
  }

  @Override
  public Collection<? extends Object> getItemValues(Object value) {
    return getPropertyInfo().getItemsFromValue(value);
  }

  protected IDataTypeHandler newDataTypeHandler() {
    IDataTypeHandler retval;
    // get the binding supplier
    IJavaTypeAdapter<?> adapter = getJavaTypeAdapter();
    if (adapter == null) {
      IClassBinding classBinding
          = getParentClassBinding().getBindingContext().getClassBinding(getPropertyInfo().getItemType());
      if (classBinding != null) {
        retval = new ClassDataTypeHandler(classBinding, this);
      } else {
        throw new IllegalStateException(
            String.format("Unable to parse type '%s', which is not a known bound class or data type",
                getPropertyInfo().getItemType()));
      }
    } else {
      retval = new JavaTypeAdapterDataTypeHandler(adapter, this);
    }
    return retval;
  }

  @Override
  public synchronized IDataTypeHandler getDataTypeHandler() {
    if (dataTypeHandler == null) {
      dataTypeHandler = newDataTypeHandler();
    }
    return dataTypeHandler;
  }

  public boolean isNextProperty(IXmlParsingContext context) throws XMLStreamException {
    XMLEventReader2 eventReader = context.getReader();

    XmlEventUtil.skipWhitespace(eventReader);

    boolean handled = false;
    QName groupQName = getXmlGroupAsQName();
    if (groupQName != null) {
      // we are to parse the grouping element, if the next token matches
      XMLEvent event = eventReader.peek();
      if (event.isStartElement() && groupQName.equals(event.asStartElement().getName())) {
        handled = true;
      }
    }

    if (!handled) {
      XMLEvent event = eventReader.peek();
      QName xmlQName = getXmlQName();
      if (xmlQName != null && event.isStartElement() && xmlQName.equals(event.asStartElement().getName())) {
        handled = true;
      }
    }
    return handled;
  }

  @Override
  public Object read(IXmlParsingContext context) throws IOException, XMLStreamException {
    Object retval = null;
    if (isNextProperty(context)) {
      retval = readInternal(null, null, context);
    }
    return retval;
  }

  @Override
  public boolean read(Object parentInstance, StartElement start, IXmlParsingContext context)
      throws IOException, XMLStreamException {
    boolean handled = isNextProperty(context);
    if (handled) {
      Object value = readInternal(parentInstance, start, context);
      setValue(parentInstance, value);
    }
    return handled;
  }

  @Override
  protected Object readInternal(Object parentInstance, IJsonParsingContext context)
      throws IOException {
    JsonParser parser = context.getReader();

    // advance past the property name
    parser.nextFieldName();

    // parse the value
    IPropertyCollector collector = newPropertyCollector();
    IModelPropertyInfo info = getPropertyInfo();
    info.readValue(collector, parentInstance, context);

    return collector.getValue();
  }

  protected Object readInternal(Object parentInstance, StartElement start, IXmlParsingContext context)
      throws IOException, XMLStreamException {
    XMLEventReader2 eventReader = context.getReader();

    XmlEventUtil.skipWhitespace(eventReader);

    StartElement currentStart = start;

    QName groupQName = getXmlGroupAsQName();
    if (groupQName != null) {
      // we are to parse the grouping element, if the next token matches
      XMLEvent groupEvent = XmlEventUtil.consumeAndAssert(eventReader, XMLEvent.START_ELEMENT, groupQName);
      currentStart = groupEvent.asStartElement();
    }

    IPropertyCollector collector = newPropertyCollector();
    // There are zero or more named values based on cardinality
    getPropertyInfo().readValue(collector, parentInstance, currentStart, context);

    Object value = collector.getValue();

    // consume extra whitespace between elements
    XmlEventUtil.skipWhitespace(eventReader);

    if (groupQName != null) {
      // consume the end of the group
      XmlEventUtil.consumeAndAssert(eventReader, XMLEvent.END_ELEMENT, groupQName);
    }

    return value;
  }

  @Override
  public IPropertyCollector newPropertyCollector() {
    return getPropertyInfo().newPropertyCollector();
  }

  @Override
  public List<Object> readItem(Object parentInstance, IJsonParsingContext context) throws IOException {
    IDataTypeHandler supplier = getDataTypeHandler();
    return supplier.get(parentInstance, context);
  }

  @Override
  public boolean write(Object parentInstance, QName parentName, IXmlWritingContext context) throws XMLStreamException, IOException {
    Object value = getValue(parentInstance);
    if (value == null) {
      return false;
    }

    boolean handled = false;
    QName currentStart = parentName;
    XMLStreamWriter2 writer = context.getWriter();
    QName groupQName = getXmlGroupAsQName();
    if (groupQName != null) {
      // write the grouping element
      writer.writeStartElement(groupQName.getNamespaceURI(), groupQName.getLocalPart());
      currentStart = groupQName;
      handled = true;
    }

    // There are zero or more named values based on cardinality
    if (getPropertyInfo().writeValue(parentInstance, currentStart, context)) {
      handled = true;
    }

    if (groupQName != null) {
      writer.writeEndElement();
    }
    return handled;
  }
  //
  // @Override
  // public void writeItem(Object parentInstance, IJsonParsingContext context) {
  // IDataTypeHandler supplier = getBindingSupplier();
  // return supplier.write(parentInstance, context);
  // }
  //
  // @Override
  // public void writeValue(Object parentInstance, IJsonParsingContext context) {
  // IModelPropertyInfo info = getPropertyInfo();
  // return info.writeValue(parentInstance, context);
  // }

  @Override
  public void write(Object parentInstance, IJsonWritingContext context) throws IOException {
    if (getPropertyInfo().isValueSet(parentInstance)) {
      // write the field name
      context.getWriter().writeFieldName(getJsonName());

      // dispatch to the property info implementation to address cardinality
      getPropertyInfo().writeValue(parentInstance, context);
    }
  }

  @Override
  public void copyBoundObject(@NotNull Object fromInstance, @NotNull Object toInstance) throws BindingException {
    Object value = getValue(fromInstance);
    if (value != null) {
      IModelPropertyInfo propertyInfo = getPropertyInfo();
      IPropertyCollector collector = newPropertyCollector();

      propertyInfo.copy(fromInstance, toInstance, collector);

      value = collector.getValue();
      setValue(toInstance, value);
    }
  }

  @Override
  public Object copyItem(@NotNull Object fromItem, @NotNull Object toInstance) throws BindingException {
    return getDataTypeHandler().copyItem(fromItem, toInstance);
  }

}
