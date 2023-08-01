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

package gov.nist.secauto.metaschema.databind.model;

import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;

import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

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
  private Lazy<IModelPropertyInfo> propertyInfo;

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
    this.propertyInfo = Lazy.lazy(() -> newPropertyInfo(() -> newDataTypeHandler()));
  }

  protected abstract IDataTypeHandler newDataTypeHandler();

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
    return propertyInfo.get();
  }

  @Override
  public Collection<? extends Object> getItemValues(Object value) {
    return getPropertyInfo().getItemsFromValue(value);
  }

  @Override
  public IPropertyCollector newPropertyCollector() {
    return getPropertyInfo().newPropertyCollector();
  }

  @Override
  public boolean write(Object parentInstance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    Object value = getValue(parentInstance);
    if (value == null) {
      return false; // NOPMD - intentional
    }

    IModelPropertyInfo propertyInfo = getPropertyInfo();

    if (propertyInfo.getProperty().getMinOccurs() > 0 || propertyInfo.getItemCount(value) > 0) {
      // only write a property if the wrapper is required or if it has contents
      QName currentStart = parentName;
      XMLStreamWriter2 writer = context.getWriter();
      QName groupQName = getXmlGroupAsQName();
      if (groupQName != null) {
        // write the grouping element
        writer.writeStartElement(groupQName.getNamespaceURI(), groupQName.getLocalPart());
        currentStart = groupQName;
      }

      // There are one or more named values based on cardinality
      propertyInfo.writeValue(value, currentStart, context);

      if (groupQName != null) {
        writer.writeEndElement();
      }
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
    return getPropertyInfo().getDataTypeHandler().copyItem(fromItem, toInstance);
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
