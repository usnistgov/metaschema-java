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

package gov.nist.secauto.metaschema.databind.model.metaschema.impl;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.model.IContainerFlagSupport;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingDefinitionModelField;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingModule;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.FieldConstraints;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.JsonKey;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.JsonValueKeyFlag;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.METASCHEMA;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public class DefinitionFieldGlobal
    extends AbstractDefinition<METASCHEMA.DefineField>
    implements IBindingDefinitionModelField,
    IFeatureBindingContainerFlag {
  @NonNull
  private final Map<QName, Set<String>> properties;
  @NonNull
  private final IDataTypeAdapter<?> javaTypeAdapter;
  @Nullable
  private final Object defaultValue;
  @NonNull
  private final Lazy<IContainerFlagSupport<IBindingInstanceFlag>> flagContainer;
  @NonNull
  private final Lazy<IValueConstrained> valueConstraints;
  @NonNull
  private final Lazy<IAssemblyNodeItem> boundNodeItem;

  public DefinitionFieldGlobal(
      @NonNull METASCHEMA.DefineField binding,
      @NonNull IBoundInstanceModelGroupedAssembly bindingInstance,
      int position,
      @NonNull IBindingModule module) {
    super(binding, module);
    this.properties = ModelSupport.parseProperties(ObjectUtils.requireNonNull(getBinding().getProps()));
    this.javaTypeAdapter = ModelSupport.dataType(getBinding().getAsType());
    this.defaultValue = ModelSupport.defaultValue(getBinding().getDefault(), this.javaTypeAdapter);
    this.flagContainer = ObjectUtils.notNull(Lazy.lazy(() -> FlagContainerSupport.of(
        binding.getFlags(),
        bindingInstance,
        this)));
    this.valueConstraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IValueConstrained retval = new ValueConstraintSet();
      FieldConstraints constraints = getBinding().getConstraint();
      if (constraints != null) {
        ConstraintBindingSupport.parse(retval, constraints, ISource.modelSource(module.getLocation()));
      }
      return retval;
    }));
    this.boundNodeItem = ObjectUtils.notNull(
        Lazy.lazy(() -> (IAssemblyNodeItem) getContainingModule().getBoundNodeItem()
            .getModelItemsByName(bindingInstance.getEffectiveName())
            .get(position)));
  }

  @Override
  public IContainerFlagSupport<IBindingInstanceFlag> getFlagContainer() {
    return ObjectUtils.notNull(flagContainer.get());
  }

  @Override
  public IValueConstrained getConstraintSupport() {
    return ObjectUtils.notNull(valueConstraints.get());
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    return properties;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }

  @Override
  public String getFormalName() {
    return getBinding().getFormalName();
  }

  @Override
  public MarkupLine getDescription() {
    return getBinding().getDescription();
  }

  @Override
  public String getName() {
    return ObjectUtils.notNull(getBinding().getName());
  }

  @Override
  public ModuleScopeEnum getModuleScope() {
    return ModelSupport.moduleScope(ObjectUtils.requireNonNull(getBinding().getScope()));
  }

  @Override
  public Integer getIndex() {
    return ModelSupport.index(getBinding().getIndex());
  }

  @Override
  public String getUseName() {
    return ModelSupport.useName(getBinding().getUseName());
  }

  @Override
  public Integer getUseIndex() {
    return ModelSupport.useIndex(getBinding().getUseName());
  }

  @Override
  public MarkupMultiline getRemarks() {
    return ModelSupport.remarks(getBinding().getRemarks());
  }

  @Override
  public String getJsonKeyFlagName() {
    JsonKey jsonKey = getBinding().getJsonKey();
    return jsonKey == null ? null : jsonKey.getFlagRef();
  }

  @Override
  public IFlagInstance getJsonValueKeyFlagInstance() {
    JsonValueKeyFlag obj = getBinding().getJsonValueKeyFlag();
    String flagName = obj == null ? null : obj.getFlagRef();
    return flagName == null ? null : getFlagInstanceByName(flagName);
  }

  @Override
  public String getJsonValueKeyName() {
    return getBinding().getJsonValueKey();
  }

  @SuppressWarnings("null")
  @Override
  public INodeItem getBoundNodeItem() {
    return boundNodeItem.get();
  }
}
