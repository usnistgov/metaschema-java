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
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.JsonFieldValueKeyFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.JsonKey;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

/**
 * Implements a Metaschema module inline flag instance/definition bound to a
 * Java field.
 */
public class InstanceFlagInline
    extends AbstractBoundInstanceJavaField<BoundFlag, IBoundDefinitionModel>
    implements IBoundInstanceFlag {
  @NonNull
  private final IDataTypeAdapter<?> javaTypeAdapter;
  @Nullable
  private final Object defaultValue;
  @NonNull
  private final Lazy<IValueConstrained> constraints;

  /**
   * Construct a new inline field instance/definition.
   *
   * @param javaField
   *          the Java field bound to this instance
   * @param containingDefinition
   *          the definition containing this instance
   */
  public InstanceFlagInline(
      @NonNull Field javaField,
      @NonNull IBoundDefinitionModel containingDefinition) {
    super(javaField, BoundFlag.class, containingDefinition);
    Class<? extends IDataTypeAdapter<?>> adapterClass = ObjectUtils.notNull(getAnnotation().typeAdapter());
    this.javaTypeAdapter
        = ModelUtil.getDataTypeAdapter(adapterClass, containingDefinition.getBindingContext());
    this.defaultValue = ModelUtil.resolveNullOrValue(getAnnotation().defaultValue(), getJavaTypeAdapter());

    this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IValueConstrained retval = new ValueConstraintSet();
      ValueConstraints valueAnnotation = getAnnotation().valueConstraints();
      ConstraintSupport.parse(valueAnnotation, ISource.modelSource(), retval);
      return retval;
    }));
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @SuppressWarnings("null")
  @Override
  @NonNull
  public IValueConstrained getConstraintSupport() {
    return constraints.get();
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
    return ObjectUtils.notNull(getField().getName());
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

  @Override
  public IBindingContext getBindingContext() {
    return getContainingDefinition().getBindingContext();
  }

  // ----------------------------------------
  // - End annotation driven code - CPD-OFF -
  // ----------------------------------------
}
