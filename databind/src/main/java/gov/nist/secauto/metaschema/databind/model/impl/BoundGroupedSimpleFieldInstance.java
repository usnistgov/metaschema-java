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
import gov.nist.secauto.metaschema.core.model.IFeatureFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFlagContainerSupport;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundChoiceGroupInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldDefinition;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundGroupedFieldInstance;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedField;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureScalarItemValueHandler;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public class BoundGroupedSimpleFieldInstance
    extends AbstractBoundGroupedFieldInstance
    implements IBoundFieldDefinition, IFeatureScalarItemValueHandler, IFeatureFlagContainer<IBoundFlagInstance> {
  @NonNull
  private final IDataTypeAdapter<?> javaTypeAdapter;
  @NonNull
  private final Lazy<IValueConstrained> constraints;

  public BoundGroupedSimpleFieldInstance(
      @NonNull BoundGroupedField annotation,
      @NonNull IBoundChoiceGroupInstance container) {
    super(annotation, container);
    this.javaTypeAdapter = ModelUtil.getDataTypeAdapter(
        annotation.typeAdapter(),
        container.getBindingContext());
    this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IValueConstrained retval = new ValueConstraintSet();
      ValueConstraints valueAnnotation = getAnnotation().valueConstraints();
      ConstraintSupport.parse(valueAnnotation, ISource.modelSource(), retval);
      return retval;
    }));
  }

  @Override
  public Object getValue(Object parent) {
    return super.getValue(parent);
  }

  @Override
  public void setValue(Object parent, Object value) {
    super.setValue(parent, value);
  }

  @Override
  public Object getValueFromString(String text) {
    return IFeatureScalarItemValueHandler.super.getValueFromString(text);
  }

  @Override
  public Object getFieldValue(Object item) {
    return item;
  }

  @Override
  public IFlagContainerSupport<IBoundFlagInstance> getFlagContainer() {
    return IFlagContainerSupport.empty();
  }

  @Override
  public IBoundFieldDefinition getDefinition() {
    return this;
  }

  @Override
  public boolean isInline() {
    // scalar fields are always inline
    return true;
  }

  @Override
  public IBoundGroupedFieldInstance getInlineInstance() {
    return this;
  }

  @Override
  public String getJsonValueKeyName() {
    throw new UnsupportedOperationException("should never get called");
  }

  @Override
  public IFlagInstance getJsonValueKeyFlagInstance() {
    // will never have one
    return null;
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @SuppressWarnings("null")
  @Override
  public IValueConstrained getConstraintSupport() {
    return constraints.get();
  }

  @Override
  public boolean hasJsonKey() {
    return false;
  }

  @Override
  public @NonNull ModuleScopeEnum getModuleScope() {
    // TODO: is this the right value?
    return ModuleScopeEnum.INHERITED;
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    return IBoundFieldDefinition.super.getProperties();
  }

  @Override
  public String toCoordinates() {
    return IBoundFieldDefinition.super.toCoordinates();
  }
}
