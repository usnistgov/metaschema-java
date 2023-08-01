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
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.ModelType;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaFieldValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class DefaultFieldValueProperty
    extends AbstractProperty<IFieldClassBinding>
    implements IBoundFieldValueInstance {
  private static final Logger LOGGER = LogManager.getLogger(DefaultFieldValueProperty.class);

  @NonNull
  private final Field field;
  @NonNull
  private final MetaschemaFieldValue fieldValue;
  @NonNull
  private final IDataTypeAdapter<?> javaTypeAdapter;
  @Nullable
  private final Object defaultValue;

  public DefaultFieldValueProperty(
      @NonNull IFieldClassBinding fieldClassBinding,
      @NonNull Field field) {
    super(fieldClassBinding);
    this.field = ObjectUtils.requireNonNull(field, "field");
    MetaschemaFieldValue valueAnnotation = field.getAnnotation(MetaschemaFieldValue.class);
    if (valueAnnotation == null) {
      throw new IllegalArgumentException(
          String.format("Class '%s' is missing the '%s' annotation.",
              fieldClassBinding.getBoundClass().getName(),
              MetaschemaFieldValue.class.getName()));
    }
    this.fieldValue = valueAnnotation;

    Class<? extends IDataTypeAdapter<?>> adapterClass = ObjectUtils.notNull(fieldValue.typeAdapter());
    this.javaTypeAdapter = ModelUtil.getDataTypeAdapter(adapterClass, fieldClassBinding.getBindingContext());
    this.defaultValue = ModelUtil.resolveDefaultValue(getFieldValueAnnotation().defaultValue(), this.javaTypeAdapter);
  }

  @Override
  public Field getField() {
    return field;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }

  protected final MetaschemaFieldValue getFieldValueAnnotation() {
    return fieldValue;
  }

  @Override
  public IFieldClassBinding getDefinition() {
    return getParentClassBinding();
  }

  @Override
  public String getJsonValueKeyName() {
    String name = ModelUtil.resolveToString(getFieldValueAnnotation().valueKeyName());
    if (name == null) {
      name = getJavaTypeAdapter().getDefaultJsonValueKey();
    }
    return name;
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  public String getFormalName() {
    // a field value doesn't have a formal name
    return null;
  }

  @Override
  public MarkupLine getDescription() {
    // a field value doesn't have a formal name
    return null;
  }

  @Override
  public MarkupMultiline getRemarks() {
    // a field value doesn't have a formal name
    return null;
  }

  @Override
  public String getUseName() {
    // TODO: implement?
    return null;
  }

  @Override
  public IPropertyCollector newPropertyCollector() {
    return new SingletonPropertyCollector();
  }

  @Override
  public boolean write(Object instance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    Object value = getValue(instance);
    if (value != null) {
      getJavaTypeAdapter().writeXmlValue(value, parentName, context.getWriter());
    }
    return true;
  }

  @SuppressWarnings("resource") // not owned
  @Override
  public void write(Object instance, IJsonWritingContext context) throws IOException {
    Object value = getValue(instance);
    if (value != null) {
      // There are two modes:
      // 1) use of a JSON value key, or
      // 2) a simple value named "value"
      IBoundFlagInstance jsonValueKey = getParentClassBinding().getJsonValueKeyFlagInstance();

      String valueKeyName;
      if (jsonValueKey != null) {
        // this is the JSON value key case
        valueKeyName = jsonValueKey.getValue(instance).toString();
      } else {
        valueKeyName = getJsonValueKeyName();
      }
      context.getWriter().writeFieldName(valueKeyName);
      LOGGER.info("FIELD: {}", valueKeyName);
      writeValue(value, context);
    }
  }

  @SuppressWarnings("resource") // not owned
  @Override
  public void writeValue(Object value, IJsonWritingContext context) throws IOException {
    if (value != null) {
      getJavaTypeAdapter().writeJsonValue(value, context.getWriter());
    }
  }

  @Override
  public @NonNull ModelType getModelType() {
    // TODO: is this right? Is there a way to not make this derived from a property?
    return ModelType.FIELD;
  }

  @Override
  public void copyBoundObject(Object fromInstance, Object toInstance) {
    Object value = getValue(fromInstance);
    IDataTypeAdapter<?> adapter = getJavaTypeAdapter();
    setValue(toInstance, value == null ? null : adapter.copy(value));
  }
}
