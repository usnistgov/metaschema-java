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

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IMetaschema;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.InternalModelSource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.xml.IFlagContainerSupport;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import java.lang.reflect.Field;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

class SimpleFieldProperty
    extends AbstractFieldProperty {
  @NonNull
  private final IDataTypeAdapter<?> javaTypeAdapter;
  @Nullable
  private final Object defaultValue;
  @NonNull
  private final Lazy<ScalarFieldDefinition> definition;

  /**
   * Construct a new bound flag instance based on a Java property. The name of the
   * property is bound to the name of the instance.
   *
   * @param field
   *          the Java field to bind to
   * @param parentClassBinding
   *          the class binding for the field's containing class
   */
  public SimpleFieldProperty(
      @NonNull Field field,
      @NonNull IAssemblyClassBinding parentClassBinding) {
    super(field, parentClassBinding);

    BoundFieldValue boundFieldValue = field.getAnnotation(BoundFieldValue.class);
    if (boundFieldValue == null) {
      this.javaTypeAdapter = MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
      this.defaultValue = null; // NOPMD readability
    } else {
      this.javaTypeAdapter = ModelUtil.getDataTypeAdapter(
          boundFieldValue.typeAdapter(),
          parentClassBinding.getBindingContext());
      this.defaultValue = ModelUtil.resolveDefaultValue(boundFieldValue.defaultValue(), this.javaTypeAdapter);
    }

    Class<?> itemType = getItemType();
    if (!itemType.equals(javaTypeAdapter.getJavaClass())) {
      throw new IllegalStateException(
          String.format("Field '%s' on class '%s' has the '%s' type adapter configured," +
              " but the field's item type '%s' does not match the adapter's type '%s'.",
              getName(),
              getContainingDefinition().getBoundClass().getName(),
              javaTypeAdapter.getClass().getName(),
              itemType.getName(),
              javaTypeAdapter.getJavaClass().getName()));
    }
    this.definition = ObjectUtils.notNull(Lazy.lazy(() -> new ScalarFieldDefinition()));
  }

  @Override
  public IBoundFieldDefinition getDefinition() {
    return ObjectUtils.notNull(definition.get());
  }

  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  protected IDataTypeHandler newDataTypeHandler() {
    return new JavaTypeAdapterDataTypeHandler(this);
  }

  @Override
  public boolean isInXmlWrapped() {
    return getFieldAnnotation().inXmlWrapped();
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  protected Object getDefaultValue() {
    return defaultValue;
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveToString(getFieldAnnotation().useName());
  }

  @Override
  public Object defaultValue() {
    return getMaxOccurs() == 1 ? getDefaultValue() : getPropertyInfo().newPropertyCollector().getValue();
  }

  private final class ScalarFieldDefinition implements IBoundFieldDefinition {
    private final Lazy<IValueConstrained> constraints;

    private ScalarFieldDefinition() {
      this.constraints = Lazy.lazy(() -> new ValueConstraintSupport(
          getField().getAnnotation(ValueConstraints.class),
          InternalModelSource.instance()));
    }

    @Override
    public IFlagContainerSupport<IBoundFlagInstance> getFlagContainer() {
      return IFlagContainerSupport.empty();
    }

    @SuppressWarnings("null")
    @Override
    public IValueConstrained getConstraintSupport() {
      return constraints.get();
    }

    @Override
    public @NonNull Object getFieldValue(@NonNull Object item) {
      return item;
    }

    @Override
    public IDataTypeAdapter<?> getJavaTypeAdapter() {
      return ObjectUtils.notNull(SimpleFieldProperty.this.getJavaTypeAdapter());
    }

    @Override
    public boolean isInline() {
      return true;
    }

    @Override
    public IBoundFieldInstance getInlineInstance() {
      return SimpleFieldProperty.this;
    }

    @Override
    public String getFormalName() {
      return SimpleFieldProperty.this.getFormalName();
    }

    @Override
    public MarkupLine getDescription() {
      return SimpleFieldProperty.this.getDescription();
    }

    @Override
    public String getName() {
      return getJavaFieldName();
    }

    @Override
    public String getUseName() {
      return ModelUtil.resolveToString(getFieldAnnotation().useName());
    }

    @Override
    public MarkupMultiline getRemarks() {
      return SimpleFieldProperty.this.getRemarks();
    }

    @Override
    public String toCoordinates() {
      return SimpleFieldProperty.this.toCoordinates();
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
    public IFlagInstance getJsonValueKeyFlagInstance() {
      return null;
    }

    @Override
    public String getJsonValueKeyName() {
      // this will never be used
      return getJavaTypeAdapter().getDefaultJsonValueKey();
    }

    @Override
    public @NonNull ModuleScopeEnum getModuleScope() {
      // TODO: is this the right value?
      return ModuleScopeEnum.INHERITED;
    }

    @Override
    public IMetaschema getContainingMetaschema() {
      return SimpleFieldProperty.this.getContainingMetaschema();
    }

    @Override
    public Object getDefaultValue() {
      return SimpleFieldProperty.this.getDefaultValue();
    }
  }
}
