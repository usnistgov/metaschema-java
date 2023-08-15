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

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IMetaschema;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.InternalModelSource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.xml.IFlagContainerSupport;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.Ignore;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaFieldValue;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.talsmasoftware.lazy4j.Lazy;

@SuppressWarnings("PMD.GodClass")
public class DefaultFieldClassBinding
    extends AbstractClassBinding
    implements IFieldClassBinding {

  @NonNull
  private final MetaschemaField metaschemaField;
  private IBoundFieldValueInstance fieldValue;
  private IBoundFlagInstance jsonValueKeyFlagInstance;
  private final Lazy<ClassBindingFlagContainerSupport> flagContainer;
  private final Lazy<IValueConstrained> constraints;

  /**
   * Create a new {@link IClassBinding} for a Java bean annotated with the
   * {@link BoundField} annotation.
   *
   * @param clazz
   *          the Java bean class
   * @param bindingContext
   *          the Metaschema binding environment context
   * @return the Metaschema field binding for the class
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
    this.flagContainer = Lazy.lazy(() -> new ClassBindingFlagContainerSupport(this, this::handleFlagInstance));
    this.constraints = Lazy.lazy(() -> new ValueConstraintSupport(
        clazz.getAnnotation(ValueConstraints.class),
        InternalModelSource.instance()));
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
  public MetaschemaField getMetaschemaFieldAnnotation() {
    return metaschemaField;
  }

  @Override
  public String getFormalName() {
    return ModelUtil.resolveToString(getMetaschemaFieldAnnotation().formalName());
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
        if (!field.isAnnotationPresent(MetaschemaFieldValue.class)) {
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
                  MetaschemaFieldValue.class.getName()));
        }

        this.fieldValue = new DefaultFieldValueProperty(this, field);
      }
      return this.fieldValue;
    }
  }

  @Override
  public boolean isInline() {
    return false;
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
  protected void writeBody(Object instance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    getFieldValueInstance().write(instance, parentName, context);
  }

  @Override
  public void writeItems(Collection<? extends Object> items, boolean writeObjectWrapper, IJsonWritingContext context)
      throws IOException {
    writeNormal(items, writeObjectWrapper, context);
  }

  @SuppressWarnings("resource") // not owned
  private void writeNormal(Collection<? extends Object> items, boolean writeObjectWrapper,
      IJsonWritingContext context)
      throws IOException {
    if (items.isEmpty()) {
      return;
    }

    Predicate<IBoundFlagInstance> flagFilter = null;

    IBoundFlagInstance jsonKey = getJsonKeyFlagInstance();
    if (jsonKey != null) {
      flagFilter = (flag) -> {
        return !jsonKey.equals(flag);
      };
    }

    IBoundFlagInstance jsonValueKey = getJsonValueKeyFlagInstance();
    if (jsonValueKey != null) {
      if (flagFilter == null) {
        flagFilter = (flag) -> {
          return !jsonValueKey.equals(flag);
        };
      } else {
        flagFilter = flagFilter.and((flag) -> {
          return !jsonValueKey.equals(flag);
        });
      }
    }

    Map<String, ? extends IBoundNamedInstance> properties = getNamedInstances(flagFilter);

    JsonGenerator writer = context.getWriter(); // NOPMD - intentional

    for (Object item : items) {
      assert item != null;
      if (writeObjectWrapper) {
        writer.writeStartObject();
      }

      if (jsonKey != null) {
        // if there is a json key, the first field will be the key
        Object flagValue = jsonKey.getValue(item);
        String key = jsonKey.getValueAsString(flagValue);
        if (key == null) {
          throw new IOException(new NullPointerException("Null key value")); // NOPMD - intentional
        }
        writer.writeFieldName(key);

        // next the value will be a start object
        writer.writeStartObject();
      }

      for (IBoundNamedInstance property : properties.values()) {
        ObjectUtils.notNull(property).write(item, context);
      }

      Object fieldValue = getFieldValueInstance().getValue(item);
      if (fieldValue != null) {
        String valueKeyName;
        if (jsonValueKey != null) {
          valueKeyName = jsonValueKey.getValueAsString(jsonValueKey.getValue(item));
        } else {
          valueKeyName = getFieldValueInstance().getJsonValueKeyName();
        }
        writer.writeFieldName(valueKeyName);
        getFieldValueInstance().writeValue(fieldValue, context);
      }

      if (jsonKey != null) {
        writer.writeEndObject();
      }

      if (writeObjectWrapper) {
        writer.writeEndObject();
      }
    }
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return getFieldValueInstance().getJavaTypeAdapter();
  }

  @Override
  protected void copyBoundObjectInternal(@NonNull Object fromInstance, @NonNull Object toInstance)
      throws BindingException {
    super.copyBoundObjectInternal(fromInstance, toInstance);

    getFieldValueInstance().copyBoundObject(fromInstance, toInstance);
  }

  @Override
  protected Class<? extends IMetaschema> getMetaschemaClass() {
    return getMetaschemaFieldAnnotation().metaschema();
  }
}
