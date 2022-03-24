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
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.IBoundFlagDefinition;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.ModelUtil;
import gov.nist.secauto.metaschema.binding.model.ValueConstraintSupport;
import gov.nist.secauto.metaschema.binding.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.binding.model.property.info.IPropertyCollector;
import gov.nist.secauto.metaschema.binding.model.property.info.SingletonPropertyCollector;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IValueConstraintSupport;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.codehaus.stax2.XMLStreamWriter2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

public class DefaultFlagProperty
    extends AbstractNamedProperty<IClassBinding>
    implements IBoundFlagInstance {
  // private static final Logger logger = LogManager.getLogger(DefaultFlagProperty.class);
  @NotNull
  private final BoundFlag flag;
  @NotNull
  private final IJavaTypeAdapter<?> javaTypeAdapter;
  private InternalFlagDefinition definition;
  private IValueConstraintSupport constraints;

  /**
   * Construct a new bound flag instance based on a Java property. The name of the property is bound
   * to the name of the instance.
   * 
   * @param field
   *          the Java field to bind to
   * @param parentClassBinding
   *          the class binding for the field's containing class
   * @param bindingContext
   *          the binding context to use for resolving bound types
   */
  public DefaultFlagProperty(@NotNull Field field, @NotNull IClassBinding parentClassBinding,
      @NotNull IBindingContext bindingContext) {
    super(field, parentClassBinding);
    this.flag = ObjectUtils.requireNonNull(field.getAnnotation(BoundFlag.class));
    this.javaTypeAdapter = ObjectUtils.notNull(
        bindingContext.getJavaTypeAdapterInstance(ObjectUtils.notNull(this.flag.typeAdapter())));
  }

  @NotNull
  protected BoundFlag getFlagAnnotation() {
    return flag;
  }

  // @Override
  // public boolean isJsonKey() {
  // return getField().isAnnotationPresent(JsonKey.class);
  // }

  // @Override
  // public boolean isJsonValueKey() {
  // return getField().isAnnotationPresent(JsonFieldValueKeyFlag.class);
  // }

  @Override
  public boolean isRequired() {
    return getFlagAnnotation().required();
  }

  @NotNull
  public IJavaTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveLocalName(getFlagAnnotation().useName(), getJavaPropertyName());
  }

  @Override
  public String getXmlNamespace() {
    return ModelUtil.resolveOptionalNamespace(getFlagAnnotation().namespace(), getParentClassBinding());
  }

  /**
   * Used to generate the instances for the constraints in a lazy fashion when the constraints are
   * first accessed.
   */
  protected void checkModelConstraints() {
    synchronized (this) {
      if (constraints == null) {
        constraints = new ValueConstraintSupport(this.getFlagAnnotation());
      }
    }
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
      Object value = getJavaTypeAdapter().parse(ObjectUtils.notNull(attribute.getValue()));
      // apply the value to the parentObject
      setValue(parentInstance, value);

      handled = true;
    }
    return handled;
  }

  @Override
  public IPropertyCollector newPropertyCollector() {
    return new SingletonPropertyCollector();
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
    return getJavaTypeAdapter().parse(value);
  }

  @Override
  public Supplier<?> readValueAndSupply(String value) throws IOException {
    return getJavaTypeAdapter().parseAndSupply(value);
  }

  @Override
  public Supplier<?> readValueAndSupply(IJsonParsingContext context) throws IOException {
    return getJavaTypeAdapter().parseAndSupply(context.getReader());
  }

  @Override
  public boolean write(Object instance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    Object objectValue = getValue(instance);
    String value = objectValue == null ? null : getJavaTypeAdapter().asString(objectValue);

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
  public String getValueAsString(Object value) throws IOException {
    return value == null ? null : getJavaTypeAdapter().asString(value);
  }

  @Override
  public void writeValue(@NotNull Object value, IJsonWritingContext context) throws IOException {
    getJavaTypeAdapter().writeJsonValue(value, context.getWriter());
  }

  @Override
  public IClassBinding getContainingDefinition() {
    return getParentClassBinding();
  }

  @SuppressWarnings("null")
  @Override
  public String toCoordinates() {
    return String.format("%s Instance(%s): %s:%s",
        getModelType().name().toLowerCase(Locale.ROOT),
        getName(),
        getParentClassBinding().getBoundClass().getName(),
        getField().getName());
  }

  @Override
  public MarkupMultiline getRemarks() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("null")
  @Override
  public IBoundFlagDefinition getDefinition() {
    synchronized (this) {
      if (definition == null) {
        definition = new InternalFlagDefinition();
      }
    }
    return definition;
  }

  @Override
  public IMetaschema getContainingMetaschema() {
    // TODO: implement
    return null;
  }

  @Override
  public void copyBoundObject(Object fromInstance, Object toInstance) {
    Object value = getValue(fromInstance);
    IJavaTypeAdapter<?> adapter = getJavaTypeAdapter();
    setValue(toInstance, value == null ? null : adapter.copy(value));
  }

  private class InternalFlagDefinition implements IBoundFlagDefinition {
    @Override
    public IJavaTypeAdapter<?> getDatatype() {
      return getJavaTypeAdapter();
    }

    @Override
    public boolean isInline() {
      return true;
    }

    @Override
    public IBoundFlagInstance getInlineInstance() {
      return DefaultFlagProperty.this;
    }

    @Override
    public String getName() {
      return DefaultFlagProperty.this.getName();
    }

    @Override
    public String getUseName() {
      return null;
    }

    @Override
    public MarkupMultiline getRemarks() {
      return DefaultFlagProperty.this.getRemarks();
    }

    @Override
    public String toCoordinates() {
      return DefaultFlagProperty.this.toCoordinates();
    }

    @Override
    public List<? extends IConstraint> getConstraints() {
      checkModelConstraints();
      return constraints.getConstraints();
    }

    @Override
    public List<? extends IAllowedValuesConstraint> getAllowedValuesContraints() {
      checkModelConstraints();
      return constraints.getAllowedValuesContraints();
    }

    @Override
    public List<? extends IMatchesConstraint> getMatchesConstraints() {
      checkModelConstraints();
      return constraints.getMatchesConstraints();
    }

    @Override
    public List<? extends IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
      checkModelConstraints();
      return constraints.getIndexHasKeyConstraints();
    }

    @Override
    public List<? extends IExpectConstraint> getExpectConstraints() {
      checkModelConstraints();
      return constraints.getExpectConstraints();
    }

    @Override
    public IBindingContext getBindingContext() {
      return getParentClassBinding().getBindingContext();
    }

    @Override
    public String getFormalName() {
      // TODO: implement
      return null;
    }

    @Override
    public MarkupLine getDescription() {
      // TODO: implement
      return null;
    }

    @Override
    public @NotNull ModuleScopeEnum getModuleScope() {
      // TODO: is this the right value?
      return ModuleScopeEnum.INHERITED;
    }

    @Override
    public IMetaschema getContainingMetaschema() {
      // TODO: implement
      return null;
    }
  }

}
