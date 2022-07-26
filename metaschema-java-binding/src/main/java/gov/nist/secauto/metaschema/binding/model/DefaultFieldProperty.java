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

import gov.nist.secauto.metaschema.binding.model.annotations.BoundField;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.binding.model.annotations.NullJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.common.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.InternalModelSource;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IValueConstraintSupport;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

class DefaultFieldProperty
    extends AbstractFieldProperty
    implements IBoundJavaCollectionField {

  @NonNull
  private final Field field;
  @NonNull
  private final BoundField fieldAnnotation;
  @NonNull
  private final IDataTypeAdapter<?> javaTypeAdapter;
  private IBoundFieldDefinition definition;
  private IValueConstraintSupport constraints;

  public static DefaultFieldProperty createInstance(@NonNull IAssemblyClassBinding parentClassBinding,
      @NonNull Field field) {
    return new DefaultFieldProperty(parentClassBinding, field);
  }

  public DefaultFieldProperty(@NonNull IAssemblyClassBinding parentClassBinding, @NonNull Field field) {
    super(parentClassBinding);
    this.field = ObjectUtils.requireNonNull(field, "field");

    if (field.isAnnotationPresent(BoundField.class)) {
      this.fieldAnnotation = ObjectUtils.notNull(field.getAnnotation(BoundField.class));
    } else {
      throw new IllegalArgumentException(String.format("BoundField '%s' on class '%s' is missing the '%s' annotation.",
          field.getName(), parentClassBinding.getBoundClass().getName(), BoundField.class.getName()));
    }

    Class<? extends IDataTypeAdapter<?>> adapterClass = ObjectUtils.notNull(getFieldAnnotation().typeAdapter());
    if (NullJavaTypeAdapter.class.equals(adapterClass)) {
      javaTypeAdapter = MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
    } else {
      javaTypeAdapter = ObjectUtils.requireNonNull(
          parentClassBinding.getBindingContext().getJavaTypeAdapterInstance(adapterClass));
    }

    Class<?> itemType = getItemType();
    // the item type must either match the type adapter or be a bound field
    if (!itemType.isAnnotationPresent(MetaschemaField.class) && !itemType.equals(javaTypeAdapter.getJavaClass())) {
      throw new IllegalStateException(
          String.format("Property '%s' on class '%s' has the '%s' type adapter configured," +
              " but the field's item type '%s' does not match the adapter's type '%s'.",
              getName(),
              getContainingDefinition().getBoundClass().getName(),
              javaTypeAdapter.getClass().getName(),
              itemType.getName(),
              javaTypeAdapter.getJavaClass().getName()));
    }
  }

  @Override
  public Field getField() {
    return field;
  }

  @NonNull
  public BoundField getFieldAnnotation() {
    return fieldAnnotation;
  }

  @NonNull
  protected IDataTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  public IBoundFieldDefinition getDefinition() {
    synchronized (this) {
      if (definition == null) {
        IDataTypeHandler handler = getDataTypeHandler();
        IClassBinding classBinding = handler.getClassBinding();
        if (classBinding == null) {
          definition = new ScalarFieldDefinition();
        } else {
          definition = (IFieldClassBinding) classBinding;
        }
      }
      return ObjectUtils.notNull(definition);
    }
  }

  @Override
  public String getFormalName() {
    return ModelUtil.resolveToString(getFieldAnnotation().formalName());
  }

  @Override
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getFieldAnnotation().description());
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveLocalName(getFieldAnnotation().useName(), getJavaPropertyName());
  }

  @Override
  public String getXmlNamespace() {
    return ModelUtil.resolveNamespace(getFieldAnnotation().namespace(), getParentClassBinding());
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
    return ModelUtil.resolveNamespace(getFieldAnnotation().groupNamespace(), getParentClassBinding());
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
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getFieldAnnotation().remarks());
  }

  /**
   * Used to generate the instances for the constraints in a lazy fashion when the constraints are
   * first accessed.
   */
  protected void checkModelConstraints() {
    synchronized (this) {
      if (constraints == null) {
        constraints = new ValueConstraintSupport(getFieldAnnotation(), InternalModelSource.instance());
      }
    }
  }

  private class ScalarFieldDefinition implements IBoundFieldDefinition {

    @Override
    public @NonNull Object getFieldValue(@NonNull Object item) {
      return item;
    }

    @Override
    public IDataTypeAdapter<?> getJavaTypeAdapter() {
      return ObjectUtils.notNull(DefaultFieldProperty.this.getJavaTypeAdapter());
    }

    @Override
    public boolean isInline() {
      return true;
    }

    @Override
    public IBoundFieldInstance getInlineInstance() {
      return DefaultFieldProperty.this;
    }

    @Override
    public String getFormalName() {
      return DefaultFieldProperty.this.getFormalName();
    }

    @Override
    public MarkupLine getDescription() {
      return DefaultFieldProperty.this.getDescription();
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
    public MarkupMultiline getRemarks() {
      return DefaultFieldProperty.this.getRemarks();
    }

    @Override
    public String toCoordinates() {
      return DefaultFieldProperty.this.toCoordinates();
    }

    @Override
    public IBoundFlagInstance getFlagInstanceByName(String name) {
      // scalar fields do not have flags
      return null;
    }

    @Override
    public Collection<? extends IBoundFlagInstance> getFlagInstances() {
      return CollectionUtil.emptyList();
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
      // this will never be used
      return getJavaTypeAdapter().getDefaultJsonValueKey();
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
    public List<? extends IAllowedValuesConstraint> getAllowedValuesConstraints() {
      checkModelConstraints();
      return constraints.getAllowedValuesConstraints();
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
    public void addConstraint(@NonNull IAllowedValuesConstraint constraint) {
      checkModelConstraints();
      constraints.addConstraint(constraint);
    }

    @Override
    public void addConstraint(@NonNull IMatchesConstraint constraint) {
      checkModelConstraints();
      constraints.addConstraint(constraint);
    }

    @Override
    public void addConstraint(@NonNull IIndexHasKeyConstraint constraint) {
      checkModelConstraints();
      constraints.addConstraint(constraint);
    }

    @Override
    public void addConstraint(@NonNull IExpectConstraint constraint) {
      checkModelConstraints();
      constraints.addConstraint(constraint);
    }

    @Override
    public @NonNull ModuleScopeEnum getModuleScope() {
      // TODO: is this the right value?
      return ModuleScopeEnum.INHERITED;
    }

    @Override
    public IMetaschema getContainingMetaschema() {
      return DefaultFieldProperty.this.getContainingMetaschema();
    }
  }
}
