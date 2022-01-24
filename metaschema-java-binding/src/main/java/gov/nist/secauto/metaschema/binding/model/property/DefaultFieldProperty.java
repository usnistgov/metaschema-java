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

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.FieldDefinition;
import gov.nist.secauto.metaschema.binding.model.ModelUtil;
import gov.nist.secauto.metaschema.binding.model.annotations.Field;
import gov.nist.secauto.metaschema.binding.model.annotations.NullJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.constraint.ValueConstraintSupport;
import gov.nist.secauto.metaschema.binding.model.property.info.DataTypeHandler;
import gov.nist.secauto.metaschema.binding.model.property.info.XmlBindingSupplier;
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
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.instance.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.instance.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.util.XmlEventUtil;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class DefaultFieldProperty
    extends AbstractFieldProperty {

  public static DefaultFieldProperty createInstance(AssemblyClassBinding parentClassBinding,
      java.lang.reflect.Field field) {
    DefaultFieldProperty retval = new DefaultFieldProperty(parentClassBinding, field);
    return retval;
  }

  private final Field field;
  private final IJavaTypeAdapter<?> javaTypeAdapter;
  private FieldDefinition definition;
  private IValueConstraintSupport constraints;

  public DefaultFieldProperty(AssemblyClassBinding parentClassBinding, java.lang.reflect.Field field) {
    super(parentClassBinding, field);
    this.field = field.getAnnotation(Field.class);
    if (this.field == null) {
      throw new IllegalArgumentException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
          field.getName(), parentClassBinding.getBoundClass().getName(), Field.class.getName()));
    }

    Class<? extends IJavaTypeAdapter<?>> adapterClass = getFieldAnnotation().typeAdapter();
    if (NullJavaTypeAdapter.class.equals(adapterClass)) {
      javaTypeAdapter = null;
    } else {
      javaTypeAdapter = getParentClassBinding().getBindingContext().getJavaTypeAdapterInstance(adapterClass);
    }
  }

  public Field getFieldAnnotation() {
    return field;
  }

  @Override
  protected IJavaTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  public FieldDefinition getDefinition() {
    synchronized (this) {
      if (definition == null) {
        DataTypeHandler handler = getDataTypeHandler();
        ClassBinding classBinding = handler.getClassBinding();
        if (classBinding == null) {
          definition = new ScalarFieldDefinition();
        } else {
          definition = (FieldClassBinding) classBinding;
        }
      }
    }
    return definition;
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveLocalName(getFieldAnnotation().useName(), getJavaPropertyName());
  }

  @Override
  public String getXmlNamespace() {
    return ModelUtil.resolveNamespace(getFieldAnnotation().namespace(), getParentClassBinding(), false);
  }

  @Override
  public boolean isInXmlWrapped() {
    return getFieldAnnotation().inXmlWrapped();
  }

  @Override
  public int getMinOccurs() {
    return getFieldAnnotation().minOccurs();
  }

  @Override
  public int getMaxOccurs() {
    return getFieldAnnotation().maxOccurs();
  }

  @Override
  public String getGroupAsName() {
    return ModelUtil.resolveLocalName(getFieldAnnotation().groupName(), null);
  }

  @Override
  public String getGroupAsXmlNamespace() {
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


  /**
   * Used to generate the instances for the constraints in a lazy fashion when the constraints are
   * first accessed.
   */
  protected synchronized void checkModelConstraints() {
    if (constraints == null) {
      constraints = new ValueConstraintSupport(this);
    }
  }

  //
  // @Override
  // public IPathSegment newPathSegment(int position) {
  // return FormatterFactory.instance().newFieldPathSegment(this, position);
  // }

  private class ScalarFieldDefinition implements FieldDefinition {
    @Override
    public IJavaTypeAdapter<?> getDatatype() {
      return getJavaTypeAdapter();
    }

    @Override
    public String getName() {
      return DefaultFieldProperty.this.getName();
    }

    @Override
    public String getUseName() {
      return null;
    }

    @Override
    public String getXmlNamespace() {
      return DefaultFieldProperty.this.getXmlNamespace();
    }

    @Override
    public MarkupMultiline getRemarks() {
      return DefaultFieldProperty.this.getRemarks();
    }

    @Override
    public String toCoordinates() {
      return DefaultFieldProperty.this.toCoordinates();
    }

    @Override
    public Map<String, ? extends FlagProperty> getFlagInstanceMap() {
      return Collections.emptyMap();
    }

    @Override
    public boolean hasJsonKey() {
      return false;
    }

    @Override
    public IFlagInstance getJsonKeyFlagInstance() {
      return null;
    }

    // @Override
    // public boolean hasJsonValueKey() {
    // return false;
    // }

    @Override
    public IFlagInstance getJsonValueKeyFlagInstance() {
      return null;
    }

    @Override
    public String getJsonValueKeyName() {
      return null;
    }

    @Override
    public boolean isCollapsible() {
      return false;
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
    public BindingContext getBindingContext() {
      return getContainingDefinition().getBindingContext();
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
    public boolean isGlobal() {
      return false;
    }

    @Override
    public IMetaschema getContainingMetaschema() {
      // TODO: implement
      return null;
    }
  }
}
