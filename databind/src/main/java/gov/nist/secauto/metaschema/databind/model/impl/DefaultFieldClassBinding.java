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
import gov.nist.secauto.metaschema.core.model.IFlagContainerSupport;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValueInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.IFieldClassBinding;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.annotations.Ignore;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.talsmasoftware.lazy4j.Lazy;

public class DefaultFieldClassBinding
    extends AbstractClassBinding
    implements IFieldClassBinding {

  @NonNull
  private final MetaschemaField metaschemaField;
  private IBoundFieldValueInstance fieldValue;
  private IBoundFlagInstance jsonValueKeyFlagInstance;
  @NonNull
  private final Lazy<ClassBindingFlagContainerSupport> flagContainer;
  @NonNull
  private final Lazy<IValueConstrained> constraints;

  /**
   * Create a new {@link IClassBinding} for a Java bean annotated with the
   * {@link BoundField} annotation.
   *
   * @param clazz
   *          the Java bean class
   * @param bindingContext
   *          the Module binding environment context
   * @return the Module field binding for the class
   */
  @NonNull
  public static DefaultFieldClassBinding createInstance(
      @NonNull Class<?> clazz,
      @NonNull IBindingContext bindingContext) {
    Objects.requireNonNull(clazz, "clazz");
    if (!clazz.isAnnotationPresent(MetaschemaField.class)) {
      throw new IllegalArgumentException(
          String.format("Class '%s' is missing the '%s' annotation.",
              clazz.getName(),
              MetaschemaField.class.getName()));
    }
    return new DefaultFieldClassBinding(clazz, bindingContext);
  }

  /**
   * Construct a new {@link IClassBinding} for a Java bean annotated with the
   * {@link BoundField} annotation.
   *
   * @param clazz
   *          the Java bean class
   * @param bindingContext
   *          the class binding context for which this class is participating
   */
  protected DefaultFieldClassBinding(
      @NonNull Class<?> clazz,
      @NonNull IBindingContext bindingContext) {
    super(clazz, bindingContext);
    this.metaschemaField = ObjectUtils.notNull(clazz.getAnnotation(MetaschemaField.class));
    this.flagContainer = ObjectUtils.notNull(Lazy.lazy(() -> {
      return new ClassBindingFlagContainerSupport(this, this::handleFlagInstance);
    }));
    this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IValueConstrained retval = new ValueConstraintSet();
      ValueConstraints valueAnnotation = this.metaschemaField.valueConstraints();
      ConstraintSupport.parse(valueAnnotation, ISource.modelSource(), retval);
      return retval;
    }));
  }

  @SuppressWarnings("null")
  @Override
  public IFlagContainerSupport<IBoundFlagInstance> getFlagContainer() {
    return flagContainer.get();
  }

  @SuppressWarnings("null")
  @Override
  public IValueConstrained getConstraintSupport() {
    return constraints.get();
  }

  @NonNull
  private MetaschemaField getMetaschemaFieldAnnotation() {
    return metaschemaField;
  }

  @Override
  public String getFormalName() {
    return ModelUtil.resolveNoneOrValue(getMetaschemaFieldAnnotation().formalName());
  }

  @Override
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getMetaschemaFieldAnnotation().description());
  }

  @Override
  public @Nullable MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getMetaschemaFieldAnnotation().description());
  }

  @Override
  public String getName() {
    return getMetaschemaFieldAnnotation().name();
  }

  @Override
  public Integer getIndex() {
    int value = getMetaschemaFieldAnnotation().index();
    return value == Integer.MIN_VALUE ? null : value;
  }

  @Override
  public Object getDefaultValue() {
    return getFieldValueInstance().getDefaultValue();
  }

  /**
   * Collect all fields that are part of the model for this class.
   *
   * @param clazz
   *          the class
   * @return the field value instances if found or {@code null} otherwise
   */
  protected java.lang.reflect.Field getFieldValueField(Class<?> clazz) {
    java.lang.reflect.Field[] fields = clazz.getDeclaredFields();

    java.lang.reflect.Field retval = null;

    Class<?> superClass = clazz.getSuperclass();
    if (superClass != null) {
      // get instances from superclass
      retval = getFieldValueField(superClass);
    }

    if (retval == null) {
      for (java.lang.reflect.Field field : fields) {
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
    }
    return retval;
  }

  /**
   * Initialize the flag instances for this class.
   *
   * @return the field value instance
   */
  protected IBoundFieldValueInstance initalizeFieldValueInstance() {
    synchronized (this) {
      if (this.fieldValue == null) {
        java.lang.reflect.Field field = getFieldValueField(getBoundClass());
        if (field == null) {
          throw new IllegalArgumentException(
              String.format("Class '%s' is missing the '%s' annotation on one of its fields.",
                  getBoundClass().getName(),
                  BoundFieldValue.class.getName()));
        }

        this.fieldValue = new DefaultFieldValueProperty(this, field);
      }
      return this.fieldValue;
    }
  }

  @Override
  public IBoundFieldInstance getInlineInstance() {
    return null;
  }

  @SuppressWarnings("null")
  @Override
  public IBoundFieldValueInstance getFieldValueInstance() {
    return initalizeFieldValueInstance();
  }

  @Override
  public Object getFieldValue(@NonNull Object item) {
    return ObjectUtils.requireNonNull(getFieldValueInstance().getValue(item));
  }

  protected void handleFlagInstance(IBoundFlagInstance instance) {
    if (instance.isJsonValueKey()) {
      this.jsonValueKeyFlagInstance = instance;
    }
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "access is restricted using interface")
  public IBoundFlagInstance getJsonValueKeyFlagInstance() {
    // lazy load flags
    flagContainer.get();
    return jsonValueKeyFlagInstance;
  }

  @Override
  public String getJsonValueKeyName() {
    return getFieldValueInstance().getJsonValueKeyName();
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return getFieldValueInstance().getJavaTypeAdapter();
  }

  @Override
  protected void deepCopyItemInternal(@NonNull Object fromInstance, @NonNull Object toInstance)
      throws BindingException {
    super.deepCopyItemInternal(fromInstance, toInstance);

    getFieldValueInstance().deepCopy(fromInstance, toInstance);
  }

  @Override
  protected Class<? extends IModule> getModuleClass() {
    return getMetaschemaFieldAnnotation().moduleClass();
  }
}
