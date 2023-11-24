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

package gov.nist.secauto.metaschema.databind.model.impl;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IFeatureFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFlagContainerSupport;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldDefinition;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IFeatureJavaField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureScalarItemValueHandler;

import java.lang.reflect.Field;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public class SimpleFieldInstance
    extends AbstractBoundFieldInstance
    implements IFeatureScalarItemValueHandler, IFeatureJavaField {
  @NonNull
  private final IDataTypeAdapter<?> javaTypeAdapter;
  @Nullable
  private final Object definitionDefaultValue;
  @Nullable
  private final Object instanceDefaultValue;

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
  public SimpleFieldInstance(
      @NonNull Field field,
      @NonNull IAssemblyClassBinding parentClassBinding) {
    super(field, parentClassBinding);

    BoundField boundField = getAnnotation();
    this.javaTypeAdapter = ModelUtil.getDataTypeAdapter(
        boundField.typeAdapter(),
        parentClassBinding.getBindingContext());
    this.definitionDefaultValue = ModelUtil.resolveDefaultValue(boundField.defaultValue(), this.javaTypeAdapter);

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
    this.instanceDefaultValue
        = ModelUtil.resolveDefaultValue(getAnnotation().defaultValue(), this.javaTypeAdapter);
  }

  @Override
  public IBoundFieldDefinition getDefinition() {
    return ObjectUtils.notNull(definition.get());
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  public IBoundFlagInstance getItemJsonKey(Object item) {
    // no flags, no JSON key
    return null;
  }

  @Override
  public Object getDefaultValue() {
    return instanceDefaultValue;
  }

  @Override
  public Object getValue(Object parent) {
    return IFeatureJavaField.super.getValue(parent);
  }

  @Override
  public void setValue(Object parent, Object value) {
    IFeatureJavaField.super.setValue(parent, value);
  }

  // REFACTOR: Cleanup interfaces and methods to use IFeatureInline, etc. Remove
  // this extra instance. Cleanup default value methods.
  private final class ScalarFieldDefinition
      implements IBoundFieldDefinition, IFeatureFlagContainer<IBoundFlagInstance> {
    @NonNull
    private final Lazy<IValueConstrained> constraints;

    private ScalarFieldDefinition() {
      this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
        IValueConstrained retval = new ValueConstraintSet();
        ValueConstraints valueAnnotation = getAnnotation().valueConstraints();
        ConstraintSupport.parse(valueAnnotation, ISource.modelSource(), retval);
        return retval;
      }));
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
    public Object getFieldValue(@NonNull Object item) {
      return item;
    }

    @Override
    public IDataTypeAdapter<?> getJavaTypeAdapter() {
      return ObjectUtils.notNull(SimpleFieldInstance.this.getJavaTypeAdapter());
    }

    @Override
    public boolean isInline() {
      // scalar fields are always inline
      return true;
    }

    @Override
    public IBoundFieldInstance getInlineInstance() {
      return SimpleFieldInstance.this;
    }

    @Override
    public String getFormalName() {
      return SimpleFieldInstance.this.getFormalName();
    }

    @Override
    public MarkupLine getDescription() {
      return SimpleFieldInstance.this.getDescription();
    }

    @Override
    public String getName() {
      return getJavaFieldName();
    }

    @Override
    public Integer getIndex() {
      return null; // no index by default;
    }

    @Override
    public Integer getUseIndex() {
      return null; // none
    }

    @Override
    public String getUseName() {
      return ModelUtil.resolveNoneOrValue(getAnnotation().useName());
    }

    @Override
    public MarkupMultiline getRemarks() {
      return SimpleFieldInstance.this.getRemarks();
    }

    @Override
    public String toCoordinates() {
      return SimpleFieldInstance.this.toCoordinates();
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
      throw new UnsupportedOperationException("should never get called");
    }

    @Override
    public @NonNull ModuleScopeEnum getModuleScope() {
      // TODO: is this the right value?
      return ModuleScopeEnum.INHERITED;
    }

    @Override
    public IModule getContainingModule() {
      return SimpleFieldInstance.this.getContainingModule();
    }

    @Override
    public Object getDefaultValue() {
      return definitionDefaultValue;
    }
  }
}
