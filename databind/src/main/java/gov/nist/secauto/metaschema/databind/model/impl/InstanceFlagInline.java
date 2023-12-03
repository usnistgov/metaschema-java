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
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBindingDefinitionFlag;
import gov.nist.secauto.metaschema.databind.model.IBindingInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IFeatureJavaField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.JsonFieldValueKeyFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.JsonKey;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureScalarItemValueHandler;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public class InstanceFlagInline
    extends AbstractBoundInstanceJavaField<BoundFlag, IBoundDefinitionModel>
    implements IBoundInstanceFlag, IBoundDefinitionFlag,
    IFeatureInlineDefinition {
  @NonNull
  private final IDataTypeAdapter<?> javaTypeAdapter;
  @Nullable
  private final Object defaultValue;
  @NonNull
  private final Lazy<IValueConstrained> constraints;
  @NonNull
  private final BindingInstanceFlag binding;

  public InstanceFlagInline(
      @NonNull Field javaField,
      @NonNull IBoundDefinitionModel containingDefinition) {
    super(javaField, BoundFlag.class, containingDefinition);
    Class<? extends IDataTypeAdapter<?>> adapterClass = ObjectUtils.notNull(getAnnotation().typeAdapter());
    this.javaTypeAdapter
        = ModelUtil.getDataTypeAdapter(adapterClass, containingDefinition.getDefinitionBinding().getBindingContext());
    this.defaultValue = ModelUtil.resolveNullOrValue(getAnnotation().defaultValue(), getJavaTypeAdapter());

    this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IValueConstrained retval = new ValueConstraintSet();
      ValueConstraints valueAnnotation = getAnnotation().valueConstraints();
      ConstraintSupport.parse(valueAnnotation, ISource.modelSource(), retval);
      return retval;
    }));

    this.binding = new BindingInstanceFlag();
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @Override
  public IBoundDefinitionFlag getDefinition() {
    return this;
  }

  @Override
  public InstanceFlagInline getInlineInstance() {
    return this;
  }

  @Override
  public BindingInstanceFlag getDefinitionBinding() {
    return binding;
  }

  @Override
  public BindingInstanceFlag getInstanceBinding() {
    return binding;
  }

  @Override
  public Object getValue(Object parent) {
    return getInstanceBinding().getValue(parent);
  }

  @Override
  public boolean isRequired() {
    return getAnnotation().required();
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }

  @Override
  public boolean isJsonKey() {
    return getField().isAnnotationPresent(JsonKey.class);
  }

  @Override
  public boolean isJsonValueKey() {
    return getField().isAnnotationPresent(JsonFieldValueKeyFlag.class);
  }

  @SuppressWarnings("null")
  @Override
  @NonNull
  public IValueConstrained getConstraintSupport() {
    return constraints.get();
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
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
  public String getName() {
    return getField().getName();
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().useName());
  }

  @Override
  public Integer getUseIndex() {
    return getAnnotation().useIndex();
  }

  @Override
  public String getXmlNamespace() {
    return ModelUtil.resolveOptionalNamespace(getAnnotation().namespace(), getContainingDefinition());
  }

  @Override
  @Nullable
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getAnnotation().remarks());
  }

  // ----------------------------------------
  // - End annotation driven code - CPD-OFF -
  // ----------------------------------------

  private class BindingInstanceFlag
      implements IBindingInstanceFlag, IBindingDefinitionFlag,
      IFeatureJavaField,
      IFeatureScalarItemValueHandler {

    @Override
    public IBindingContext getBindingContext() {
      return getContainingDefinition().getDefinitionBinding().getBindingContext();
    }

    @Override
    public Object getValue(Object parent) {
      return IFeatureJavaField.super.getValue(parent);
    }

    @Override
    public void setValue(Object parentObject, Object value) {
      IFeatureJavaField.super.setValue(parentObject, value);
    }

    @Override
    public Field getField() {
      return InstanceFlagInline.this.getField();
    }

    @Override
    public InstanceFlagInline getInstance() {
      return InstanceFlagInline.this;
    }

    @Override
    public InstanceFlagInline getDefinition() {
      return InstanceFlagInline.this;
    }

    @Override
    public IDataTypeAdapter<?> getJavaTypeAdapter() {
      return InstanceFlagInline.this.getJavaTypeAdapter();
    }

    @Override
    public void deepCopy(Object fromInstance, Object toInstance) throws BindingException {
      Object value = getValue(fromInstance);
      if (value != null) {
        setValue(toInstance, deepCopyItem(value, toInstance));
      }
    }

    @Override
    public String toString(Object parent) {
      Object value = getValue(parent);
      return getJavaTypeAdapter().asString(value);
    }
  }
}
