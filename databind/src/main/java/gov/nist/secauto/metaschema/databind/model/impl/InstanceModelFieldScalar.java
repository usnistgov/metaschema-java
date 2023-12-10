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
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.IBindingInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionField;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldScalar;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public class InstanceModelFieldScalar
    extends AbstractBoundInstanceField
    implements IBoundInstanceModelFieldScalar,
    IFeatureBoundDefinitionFlagContainer {
  @NonNull
  private final IDataTypeAdapter<?> javaTypeAdapter;
  @Nullable
  private final Object defaultValue;
  @NonNull
  private final Lazy<IValueConstrained> constraints;

  public InstanceModelFieldScalar(
      @NonNull Field field,
      @NonNull IBoundDefinitionAssembly containingDefinition) {
    super(field, containingDefinition);
    this.javaTypeAdapter = ModelUtil.getDataTypeAdapter(
        getAnnotation().typeAdapter(),
        containingDefinition.getDefinitionBinding().getBindingContext());
    this.defaultValue = ModelUtil.resolveDefaultValue(getAnnotation().defaultValue(), this.javaTypeAdapter);

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

  @Override
  public InstanceModelFieldScalar getInstance() {
    return this;
  }

  @Override
  public InstanceModelFieldScalar getDefinition() {
    return this;
  }

  @Override
  public InstanceModelFieldScalar getInlineInstance() {
    return this;
  }

  @Override
  public InstanceModelFieldScalar getInstanceBinding() {
    return this;
  }

  @Override
  public InstanceModelFieldScalar getDefinitionBinding() {
    return this;
  }

  @Override
  public InstanceModelFieldScalar getFieldValueBinding() {
    return this;
  }

  @Override
  public IBoundDefinitionFlagContainerSupport getFlagContainer() {
    return IBoundDefinitionFlagContainerSupport.empty();
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
  public String getUseName() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().useName());
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }

  @Override
  public String getJsonValueKeyFlagName() {
    // no flags, no JSON value key
    return null;
  }

  @Override
  public IBoundInstanceFlag getJsonValueKeyFlagInstance() {
    // no flags, no value key flag
    return null;
  }

  @Override
  public String getJsonValueKeyName() {
    // no bound value, no value key name
    return null;
  }

  @Override
  public Object getFieldValue(Object item) {
    // the item is the field value
    return item;
  }

  @Override
  public Collection<? extends Object> getItemValues(Object value) {
    return getInstanceBinding().getCollectionInfo().getItemsFromValue(value);
  }

  @Override
  public String getJsonKeyFlagName() {
    // no flags
    return null;
  }

  @Override
  public IBoundInstanceFlag getJsonKeyFlagInstance() {
    // no flags
    return null;
  }

  @Override
  public IBoundInstanceFlag getItemJsonKey(Object item) {
    // no flags, no JSON key
    return null;
  }

  @Override
  public IBoundDefinitionField getParentFieldDefinition() {
    return this;
  }

  @Override
  public IBindingContext getBindingContext() {
    return getContainingDefinition().getDefinitionBinding().getBindingContext();
  }

  @Override
  public Collection<IBindingInstanceFlag> getFlagInstanceBindings() {
    // no flags
    return CollectionUtil.emptyList();
  }

  @Override
  public Object readItem(Object parent, IItemReadHandler handler) throws IOException {
    return handler.readItemField(parent, this);
  }

  // ----------------------------------------
  // - End annotation driven code - CPD-OFF -
  // ----------------------------------------
}
