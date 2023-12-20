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

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.AssemblyModelContainerSupport;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IFeatureFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFeatureInlinedDefinition;
import gov.nist.secauto.metaschema.core.model.IFeatureStandardModelContainer;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModelContainer;
import gov.nist.secauto.metaschema.core.model.IStandardModelContainerSupport;
import gov.nist.secauto.metaschema.core.model.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.core.model.constraint.AssemblyConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyConstraints;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyModel;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.InlineDefineAssembly;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public class InstanceModelAssemblyInline
    extends AbstractInstanceModel<InlineDefineAssembly, IModelContainer>
    implements IAssemblyInstance, IAssemblyDefinition,
    IFeatureInlinedDefinition<IAssemblyDefinition, IAssemblyInstance>,
    IFeatureStandardModelContainer,
    IFeatureFlagContainer<IFlagInstance> {
  @NonNull
  private final Map<QName, Set<String>> properties;
  @NonNull
  private final Lazy<FlagContainerSupport> flagContainer;
  @NonNull
  private final Lazy<IStandardModelContainerSupport> modelContainer;
  @NonNull
  private final Lazy<IModelConstrained> modelConstraints;

  public InstanceModelAssemblyInline(
      @NonNull InlineDefineAssembly binding,
      @NonNull IModelContainer parent) {
    super(binding, parent);
    this.properties = ModelSupport.parseProperties(ObjectUtils.requireNonNull(getBinding().getProps()));
    this.flagContainer = ObjectUtils.notNull(Lazy.lazy(() -> new FlagContainerSupport(binding.getFlags(), this)));
    this.modelContainer = ObjectUtils.notNull(Lazy.lazy(() -> {
      AssemblyModel model = binding.getModel();

      return model == null
          ? new AssemblyModelContainerSupport()
          : new BindingModelContainerSupport(model, this);
    }));
    this.modelConstraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IModelConstrained retval = new AssemblyConstraintSet();
      AssemblyConstraints constraints = getBinding().getConstraint();
      if (constraints != null) {
        ConstraintBindingSupport.parse(retval, constraints, ISource.modelSource());
      }
      return retval;
    }));
  }

  @Override
  public FlagContainerSupport getFlagContainer() {
    return ObjectUtils.notNull(flagContainer.get());
  }

  @Override
  public IStandardModelContainerSupport getModelContainer() {
    return ObjectUtils.notNull(modelContainer.get());
  }

  @Override
  public IModelConstrained getConstraintSupport() {
    return ObjectUtils.notNull(modelConstraints.get());
  }

  @Override
  public IAssemblyDefinition getDefinition() {
    return this;
  }

  @Override
  public IAssemblyInstance getInlineInstance() {
    return this;
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    return properties;
  }

  @Override
  public String getName() {
    return ObjectUtils.notNull(getBinding().getName());
  }

  @Override
  public Integer getIndex() {
    return ModelSupport.index(getBinding().getIndex());
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
  public MarkupMultiline getRemarks() {
    return ModelSupport.remarks(getBinding().getRemarks());
  }

  @Override
  public int getMinOccurs() {
    BigInteger min = getBinding().getMinOccurs();
    return min == null ? MetaschemaModelConstants.DEFAULT_GROUP_AS_MIN_OCCURS : min.intValueExact();
  }

  @Override
  public int getMaxOccurs() {
    String max = getBinding().getMaxOccurs();
    return max == null
        ? MetaschemaModelConstants.DEFAULT_GROUP_AS_MIN_OCCURS
        : ModelSupport.maxOccurs(max);
  }

  @Override
  public boolean isRoot() {
    return getRootName() != null || getRootIndex() != null;
  }

  @Override
  public String getRootName() {
    // inline assemblies are never a root
    return null;
  }

  @Override
  public Integer getRootIndex() {
    // inline assemblies are never a root
    return null;
  }
}
