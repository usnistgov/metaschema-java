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
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;
import gov.nist.secauto.metaschema.databind.model.IBoundProperty;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.annotations.Ignore;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Predicate;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

//TODO: implement getProperties()
public final class DefinitionField
    extends AbstractBoundDefinitionModelComplex<MetaschemaField>
    implements IBoundDefinitionModelFieldComplex {
  @NonNull
  private final FieldValue fieldValue;
  @Nullable
  private IBoundInstanceFlag jsonValueKeyFlagInstance;
  @NonNull
  private final Lazy<FlagContainerSupport> flagContainer;
  @NonNull
  private final Lazy<IValueConstrained> constraints;
  @NonNull
  private final Lazy<Map<String, IBoundProperty>> jsonProperties;

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
      if (!field.isAnnotationPresent(BoundFieldValue.class) || field.isAnnotationPresent(Ignore.class)) {
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

  /**
   * Construct a new Metaschema module field definition.
   *
   * @param clazz
   *          the Java class the definition is bound to
   * @param bindingContext
   *          the Metaschema binding context managing this class
   * @return the instance
   */
  @NonNull
  public static DefinitionField newInstance(
      @NonNull Class<?> clazz,
      @NonNull IBindingContext bindingContext) {
    MetaschemaField annotation = ModelUtil.getAnnotation(clazz, MetaschemaField.class);
    Class<? extends IBoundModule> moduleClass = annotation.moduleClass();
    return new DefinitionField(clazz, annotation, moduleClass, bindingContext);
  }

  private DefinitionField(
      @NonNull Class<?> clazz,
      @NonNull MetaschemaField annotation,
      @NonNull Class<? extends IBoundModule> moduleClass,
      @NonNull IBindingContext bindingContext) {
    super(clazz, annotation, moduleClass, bindingContext);
    Field field = getFieldValueField(getBoundClass());
    if (field == null) {
      throw new IllegalArgumentException(
          String.format("Class '%s' is missing the '%s' annotation on one of its fields.",
              clazz.getName(),
              BoundFieldValue.class.getName())); // NOPMD false positive
    }
    this.fieldValue = new FieldValue(field, BoundFieldValue.class, bindingContext);
    this.flagContainer = ObjectUtils.notNull(Lazy.lazy(() -> new FlagContainerSupport(this, this::handleFlagInstance)));
    this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IModelConstrained retval = new AssemblyConstraintSet();
      ValueConstraints valueAnnotation = getAnnotation().valueConstraints();
      ConstraintSupport.parse(valueAnnotation, ISource.modelSource(), retval);
      return retval;
    }));
    this.jsonProperties = ObjectUtils.notNull(Lazy.lazy(() -> {
      IBoundInstanceFlag jsonValueKey = getJsonValueKeyFlagInstance();
      Predicate<IBoundInstanceFlag> flagFilter = jsonValueKey == null ? null : flag -> !flag.equals(jsonValueKey);
      return getJsonProperties(flagFilter);
    }));
  }

  /**
   * A callback used to identify the JSON value key flag.
   *
   * @param instance
   *          a flag instance
   */
  protected void handleFlagInstance(@NonNull IBoundInstanceFlag instance) {
    if (instance.isJsonValueKey()) {
      this.jsonValueKeyFlagInstance = instance;
    }
  }

  @Override
  @NonNull
  public FieldValue getFieldValue() {
    return fieldValue;
  }

  @Override
  public IBoundInstanceFlag getJsonValueKeyFlagInstance() {
    // lazy load flags
    getFlagContainer();
    return jsonValueKeyFlagInstance;
  }

  @Override
  protected void deepCopyItemInternal(Object fromObject, Object toObject) throws BindingException {
    // copy the flags
    super.deepCopyItemInternal(fromObject, toObject);

    getFieldValue().deepCopy(fromObject, toObject);
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

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
  public Map<String, IBoundProperty> getJsonProperties() {
    return ObjectUtils.notNull(jsonProperties.get());
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

  protected class FieldValue
      implements IBoundFieldValue {
    @NonNull
    private final Field javaField;
    @NonNull
    private final BoundFieldValue annotation;
    @NonNull
    private final IDataTypeAdapter<?> javaTypeAdapter;
    @Nullable
    private final Object defaultValue;

    /**
     * Construct a new field value binding.
     *
     * @param javaField
     *          the Java field the field value is bound to
     * @param annotationClass
     *          the field value binding annotation Java class
     * @param bindingContext
     *          the Metaschema binding context managing this class
     */
    protected FieldValue(
        @NonNull Field javaField,
        @NonNull Class<BoundFieldValue> annotationClass,
        @NonNull IBindingContext bindingContext) {
      this.javaField = javaField;
      this.annotation = ModelUtil.getAnnotation(javaField, annotationClass);
      this.javaTypeAdapter = ModelUtil.getDataTypeAdapter(
          this.annotation.typeAdapter(),
          bindingContext);
      this.defaultValue = ModelUtil.resolveNullOrValue(this.annotation.defaultValue(), this.javaTypeAdapter);
    }

    /**
     * Get the bound Java field.
     *
     * @return the bound Java field
     */
    @Override
    @NonNull
    public Field getField() {
      return javaField;
    }

    /**
     * Get the binding Java annotation.
     *
     * @return the binding Java annotation
     */
    @NonNull
    public BoundFieldValue getAnnotation() {
      return annotation;
    }

    @Override
    public IBoundDefinitionModelFieldComplex getParentFieldDefinition() {
      return DefinitionField.this;
    }

    @Override
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
    public Object getEffectiveDefaultValue() {
      return getDefaultValue();
    }

    @Override
    public String getJsonName() {
      return getEffectiveJsonValueKeyName();
    }
  }
  // ----------------------------------------
  // - End annotation driven code - CPD-OFF -
  // ----------------------------------------
}
