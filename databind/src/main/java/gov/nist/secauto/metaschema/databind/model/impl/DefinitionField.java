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
import gov.nist.secauto.metaschema.core.model.constraint.AssemblyConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBindingDefinitionField;
import gov.nist.secauto.metaschema.databind.model.IBindingFieldValue;
import gov.nist.secauto.metaschema.databind.model.IBindingInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionField;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelField;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;
import gov.nist.secauto.metaschema.databind.model.IFeatureJavaField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.annotations.Ignore;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public class DefinitionField
    extends AbstractBoundDefinitionFlagContainer<MetaschemaField>
    implements IBoundDefinitionField, IBoundDefinitionModelComplex {
  @NonNull
  private final FieldValue fieldValue;
  @Nullable
  private IBoundInstanceFlag jsonValueKeyFlagInstance;
  @NonNull
  private final Lazy<FlagContainerSupport> flagContainer;
  @NonNull
  private final Lazy<IValueConstrained> constraints;
  @NonNull
  private final Lazy<BindingDefinitionField> binding;

  /**
   * Collect all fields that are part of the model for this class.
   *
   * @param clazz
   *          the class
   * @return the field value instances if found or {@code null} otherwise
   */
  @Nullable
  private static Field getFieldValueField(Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();

    Field retval = null;
    for (Field field : fields) {
      if (!field.isAnnotationPresent(BoundFieldValue.class)) {
        // skip fields that aren't a field or assembly instance
        continue;
      }

      if (field.isAnnotationPresent(Ignore.class)) {
        // skip this field, since it is ignored
        continue;
      }
      retval = field;
    }

    if (retval == null) {
      Class<?> superClass = clazz.getSuperclass();
      if (superClass != null) {
        // get instances from superclass
        retval = getFieldValueField(superClass);
      }
    }
    return retval;
  }

  public DefinitionField(
      @NonNull Class<?> clazz,
      @NonNull IBindingContext bindingContext) {
    super(clazz, MetaschemaField.class, bindingContext);
    this.binding = ObjectUtils.notNull(Lazy.lazy(() -> new BindingDefinitionField()));

    Field field = getFieldValueField(getBoundClass());
    if (field == null) {
      throw new IllegalArgumentException(
          String.format("Class '%s' is missing the '%s' annotation on one of its fields.",
              getBoundClass().getName(),
              BoundFieldValue.class.getName()));
    }
    this.fieldValue = new FieldValue(field, BoundFieldValue.class);
    this.flagContainer = ObjectUtils.notNull(Lazy.lazy(() -> new FlagContainerSupport(this, this::handleFlagInstance)));
    this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IModelConstrained retval = new AssemblyConstraintSet();
      ValueConstraints valueAnnotation = getAnnotation().valueConstraints();
      ConstraintSupport.parse(valueAnnotation, ISource.modelSource(), retval);
      return retval;
    }));
  }

  protected void handleFlagInstance(IBoundInstanceFlag instance) {
    if (instance.isJsonValueKey()) {
      this.jsonValueKeyFlagInstance = instance;
    }
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @SuppressWarnings("null")
  @Override
  @NonNull
  public BindingDefinitionField getDefinitionBinding() {
    return binding.get();
  }

  @Override
  @Nullable
  public IBoundInstanceModelField getInlineInstance() {
    // never inline
    return null;
  }

  @Override
  @SuppressWarnings("null")
  @NonNull
  public FlagContainerSupport getFlagContainer() {
    return flagContainer.get();
  }

  @Override
  @NonNull
  public IValueConstrained getConstraintSupport() {
    return ObjectUtils.notNull(constraints.get());
  }

  @Override
  public FieldValue getFieldValueBinding() {
    return fieldValue;
  }

  @Override
  @Nullable
  public String getFormalName() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().formalName());
  }

  @Override
  @Nullable
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getAnnotation().description());
  }

  @Override
  @NonNull
  public Map<QName, Set<String>> getProperties() {
    // TODO: implement
    return CollectionUtil.emptyMap();
  }

  @Override
  @NonNull
  public String getName() {
    return getAnnotation().name();
  }

  @Override
  @Nullable
  public Integer getIndex() {
    return ModelUtil.resolveNullOrInteger(getAnnotation().index());
  }

  @Override
  @Nullable
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getAnnotation().description());
  }

  @Override
  @NonNull
  protected Class<? extends IBoundModule> getModuleClass() {
    return getAnnotation().moduleClass();
  }

  @Override
  public IBoundInstanceFlag getJsonValueKeyFlagInstance() {
    // lazy load flags
    getFlagContainer();
    return jsonValueKeyFlagInstance;
  }

  @Override
  public String getJsonValueKeyName() {
    return getFieldValueBinding().getJsonValueKeyName();
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return getFieldValueBinding().getJavaTypeAdapter();
  }

  @Override
  public Object getFieldValue(Object item) {
    return getFieldValueBinding().getValue(item);
  }

  protected class FieldValue
      extends AbstractBoundAnnotatedJavaField<BoundFieldValue>
      implements IBindingFieldValue, IFeatureJavaField {
    @NonNull
    private final IDataTypeAdapter<?> javaTypeAdapter;
    @Nullable
    private final Object defaultValue;

    protected FieldValue(
        @NonNull Field javaField,
        @NonNull Class<BoundFieldValue> annotationClass) {
      super(javaField, annotationClass);
      this.javaTypeAdapter
          = ModelUtil.getDataTypeAdapter(getAnnotation().typeAdapter(), getDefinitionBinding().getBindingContext());
      this.defaultValue = ModelUtil.resolveNullOrValue(getAnnotation().defaultValue(), getJavaTypeAdapter());
    }

    @Override
    public DefinitionField getContainingDefinition() {
      return DefinitionField.this;
    }

    @Override
    public FieldValue getInstanceBinding() {
      return this;
    }

    @Override
    public Object getValue(Object parent) {
      return IFeatureJavaField.super.getValue(parent);
    }

    @Override
    public void setValue(Object parentObject, Object value) {
      IFeatureJavaField.super.setValue(parentObject, value);
    }

    public String getJsonValueKeyName() {
      String name = ModelUtil.resolveNoneOrValue(getAnnotation().valueKeyName());
      return name == null ? getJavaTypeAdapter().getDefaultJsonValueKey() : name;
    }

    @Override
    public String getJsonValueKeyFlagName() {
      return ModelUtil.resolveNoneOrValue(getAnnotation().valueKeyName());
    }

    @Override
    public Object getDefaultValue() {
      return defaultValue;
    }

    @Override
    public IDataTypeAdapter<?> getJavaTypeAdapter() {
      return javaTypeAdapter;
    }

    @Override
    public boolean canHandleJsonPropertyName(String name) {
      return name.equals(getJsonValueKeyName());
    }

    @Override
    public boolean canHandleXmlQName(QName qname) {
      return getJavaTypeAdapter().canHandleQName(qname);
    }

    @Override
    public Object getEffectiveDefaultValue() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getJsonName() {
      return getEffectiveJsonValueKeyName();
    }

    @Override
    public FieldValue getInstance() {
      return this;
    }

    @Override
    public void deepCopy(@NonNull Object fromInstance, @NonNull Object toInstance) throws BindingException {
      Object value = getValue(fromInstance);
      setValue(toInstance, value);
    }
  }

  protected class BindingDefinitionField
      extends AbstractBindingFlagContainerDefinition
      implements IBindingDefinitionField {
    @NonNull
    private final Lazy<List<IBindingInstanceFlag>> flagInstanceBindings;

    private BindingDefinitionField() {
      this.flagInstanceBindings = ObjectUtils.notNull(Lazy.lazy(() -> getFlagInstances().stream()
          .map(instance -> instance.getInstanceBinding())
          .collect(Collectors.toUnmodifiableList())));
    }

    @Override
    @NonNull
    public DefinitionField getDefinition() {
      return DefinitionField.this;
    }

    @SuppressWarnings("null")
    @Override
    @NonNull
    public List<IBindingInstanceFlag> getFlagInstanceBindings() {
      return flagInstanceBindings.get();
    }

    @Override
    @NonNull
    public Class<?> getBoundClass() {
      return DefinitionField.this.getBoundClass();
    }

    @Override
    public boolean canHandleJsonPropertyName(String name) {
      // not handled, since not root
      return false;
    }

    @Override
    public boolean canHandleXmlQName(QName qname) {
      // not handled, since not root
      return false;
    }

    @Override
    protected void deepCopyItemInternal(Object fromObject, Object toObject) throws BindingException {
      super.deepCopyItemInternal(fromObject, toObject);

      getFieldValueBinding().deepCopy(fromObject, toObject);
    }

  }

  // ----------------------------------------
  // - End annotation driven code - CPD-OFF -
  // ----------------------------------------
}
