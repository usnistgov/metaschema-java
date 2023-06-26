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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonUtil;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.model.common.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.model.common.util.XmlEventUtil;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

abstract class AbstractNamedModelProperty // NOPMD - intentional
    extends AbstractNamedProperty<IAssemblyClassBinding>
    implements IBoundNamedModelInstance {
  // private static final Logger logger = LogManager.getLogger(AbstractNamedModelProperty.class);

  @NonNull
  private static final IGroupAs SINGLETON_GROUP_AS = new IGroupAs() {
    @Override
    public String getGroupAsName() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getGroupAsXmlNamespace() {
      throw new UnsupportedOperationException();
    }

    @Override
    public JsonGroupAsBehavior getJsonGroupAsBehavior() {
      return JsonGroupAsBehavior.NONE;
    }

    @Override
    public XmlGroupAsBehavior getXmlGroupAsBehavior() {
      return XmlGroupAsBehavior.UNGROUPED;
    }
  };

  @NonNull
  private final Field field;
  @NonNull
  private final IGroupAs groupAs;
  private IModelPropertyInfo propertyInfo;
  private IDataTypeHandler dataTypeHandler;

  /**
   * Construct a new bound model instance based on a Java property. The name of the property is bound
   * to the name of the instance.
   *
   * @param field
   *          the field instance associated with this property
   *
   * @param parentClassBinding
   *          the class binding for the field's containing class
   */
  protected AbstractNamedModelProperty(@NonNull Field field, @NonNull IAssemblyClassBinding parentClassBinding) {
    super(parentClassBinding);
    this.field = ObjectUtils.requireNonNull(field, "field");

    GroupAs annotation = field.getAnnotation(GroupAs.class);
    // if (annotation == null && (getMaxOccurs() == -1 || getMaxOccurs() > 1)) {
    // throw new IllegalStateException(String.format("Field '%s' on class '%s' is missing the '%s'
    // annotation.",
    // field.getName(), parentClassBinding.getBoundClass().getName(), GroupAs.class.getName()));
    // }
    this.groupAs = annotation == null ? SINGLETON_GROUP_AS : new SimpleGroupAs(annotation, parentClassBinding);
  }

  @Override
  public Field getField() {
    return field;
  }

  @Override
  public abstract int getMinOccurs();

  @Override
  public abstract int getMaxOccurs();

  @Override
  public String getGroupAsName() {
    return groupAs.getGroupAsName();
  }

  @Override
  public String getGroupAsXmlNamespace() {
    return groupAs.getGroupAsXmlNamespace();
  }

  @Override
  public JsonGroupAsBehavior getJsonGroupAsBehavior() {
    return groupAs.getJsonGroupAsBehavior();
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    return groupAs.getXmlGroupAsBehavior();
  }

  /**
   * Gets information about the bound property.
   *
   * @return the property information for the bound property
   */
  @SuppressWarnings("null")
  @Override
  @NonNull
  public IModelPropertyInfo getPropertyInfo() {
    synchronized (this) {
      if (propertyInfo == null) {
        propertyInfo = newPropertyInfo();
      }
      return propertyInfo;
    }
  }

  @Override
  public Collection<? extends Object> getItemValues(Object value) {
    return getPropertyInfo().getItemsFromValue(value);
  }

  protected abstract IDataTypeHandler newDataTypeHandler();

  @Override
  public IDataTypeHandler getDataTypeHandler() {
    synchronized (this) {
      if (dataTypeHandler == null) {
        dataTypeHandler = newDataTypeHandler();
      }
      return ObjectUtils.notNull(dataTypeHandler);
    }
  }

  public boolean isNextProperty(@NonNull IXmlParsingContext context) throws XMLStreamException {
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
  public boolean read(Object parentInstance, StartElement start, IXmlParsingContext context)
      throws IOException, XMLStreamException {
    boolean handled = isNextProperty(context);
    if (handled) {
      Object value = readInternal(parentInstance, start, context);
      setValue(parentInstance, value);
    }
    return handled;
  }

  @SuppressWarnings("resource")
  @Override
  protected Object readInternal(Object parentInstance, IJsonParsingContext context)
      throws IOException {
    JsonParser parser = context.getReader(); // NOPMD - intentional

    // the parser's current token should be the JSON field name
    // advance past the property name
    JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);

    // parse the value
    IPropertyCollector collector = newPropertyCollector();
    IModelPropertyInfo info = getPropertyInfo();
    info.readValue(collector, parentInstance, context);

    JsonUtil.assertCurrent(context.getReader(), JsonToken.FIELD_NAME, JsonToken.END_OBJECT);

    return collector.getValue();
  }

  protected Object readInternal(@Nullable Object parentInstance, @NonNull StartElement start,
      @NonNull IXmlParsingContext context)
      throws IOException, XMLStreamException {
    XMLEventReader2 eventReader = context.getReader();

    XmlEventUtil.skipWhitespace(eventReader);

    StartElement currentStart = start;

    QName groupQName = getXmlGroupAsQName();
    if (groupQName != null) {
      // we are to parse the grouping element, if the next token matches
      XMLEvent groupEvent = XmlEventUtil.consumeAndAssert(eventReader, XMLStreamConstants.START_ELEMENT, groupQName);
      currentStart = ObjectUtils.notNull(groupEvent.asStartElement());
    }

    IPropertyCollector collector = newPropertyCollector();
    // There are zero or more named values based on cardinality
    getPropertyInfo().readValue(collector, parentInstance, currentStart, context);

    Object value = collector.getValue();

    // consume extra whitespace between elements
    XmlEventUtil.skipWhitespace(eventReader);

    if (groupQName != null) {
      // consume the end of the group
      XmlEventUtil.consumeAndAssert(eventReader, XMLStreamConstants.END_ELEMENT, groupQName);
    }

    return value;
  }

  @Override
  public IPropertyCollector newPropertyCollector() {
    return getPropertyInfo().newPropertyCollector();
  }

  @Override
  public List<Object> readItem(Object parentInstance, boolean requiresJsonKey, IJsonParsingContext context)
      throws IOException {
    IDataTypeHandler supplier = getDataTypeHandler();
    return supplier.get(parentInstance, requiresJsonKey, context);
  }

  @Override
  public boolean write(Object parentInstance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    Object value = getValue(parentInstance);
    if (value == null) {
      return false; // NOPMD - intentional
    }

    QName currentStart = parentName;
    XMLStreamWriter2 writer = context.getWriter();
    QName groupQName = getXmlGroupAsQName();
    if (groupQName != null) {
      // write the grouping element
      writer.writeStartElement(groupQName.getNamespaceURI(), groupQName.getLocalPart());
      currentStart = groupQName;
    }

    // There are one or more named values based on cardinality
    getPropertyInfo().writeValue(value, currentStart, context);

    if (groupQName != null) {
      writer.writeEndElement();
    }
    return true;
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

  @SuppressWarnings("resource")
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
  public void copyBoundObject(@NonNull Object fromInstance, @NonNull Object toInstance) throws BindingException {
    Object value = getValue(fromInstance);
    if (value != null) {
      IModelPropertyInfo propertyInfo = getPropertyInfo();
      IPropertyCollector collector = newPropertyCollector();

      propertyInfo.copy(fromInstance, toInstance, collector);

      value = collector.getValue();
    }
    setValue(toInstance, value);
  }

  @Override
  public Object copyItem(Object fromItem, Object toInstance) throws BindingException {
    return getDataTypeHandler().copyItem(fromItem, toInstance);
  }

  /**
   * A data object to record the group as selections.
   */
  private interface IGroupAs {
    @NonNull
    String getGroupAsName();

    @Nullable
    String getGroupAsXmlNamespace();

    @NonNull
    JsonGroupAsBehavior getJsonGroupAsBehavior();

    @NonNull
    XmlGroupAsBehavior getXmlGroupAsBehavior();
  }

  private static final class SimpleGroupAs implements IGroupAs {
    @NonNull
    private final String name;
    @NonNull
    private final Lazy<String> namespace;
    @NonNull
    private final GroupAs annotation;

    private SimpleGroupAs(@NonNull GroupAs annotation, @NonNull IClassBinding parentDefinition) {
      this.annotation = annotation;
      {
        String value = ModelUtil.resolveLocalName(annotation.name(), null);
        if (value == null) {
          throw new IllegalStateException(
              String.format("The %s#groupName value '%s' resulted in an invalid null value",
                  GroupAs.class.getName(),
                  annotation.name()));
        }
        this.name = value;
      }
      this.namespace = ObjectUtils.notNull(
          Lazy.lazy(() -> ModelUtil.resolveNamespace(annotation.namespace(), parentDefinition)));
    }

    @Override
    public String getGroupAsName() {
      return name;
    }

    @Override
    public String getGroupAsXmlNamespace() {
      return namespace.get();
    }

    @Override
    public JsonGroupAsBehavior getJsonGroupAsBehavior() {
      return annotation.inJson();
    }

    @Override
    public XmlGroupAsBehavior getXmlGroupAsBehavior() {
      return annotation.inXml();
    }
  }

}
