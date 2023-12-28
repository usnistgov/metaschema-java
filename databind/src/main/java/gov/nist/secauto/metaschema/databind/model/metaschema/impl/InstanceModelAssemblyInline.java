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
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItemFactory;
import gov.nist.secauto.metaschema.core.model.IContainerFlagSupport;
import gov.nist.secauto.metaschema.core.model.IContainerModelAssemblySupport;
import gov.nist.secauto.metaschema.core.model.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.core.model.constraint.AssemblyConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingContainerModelAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceModelAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceModelAssemblyAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceModelFieldAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceModelNamedAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.IInstanceModelChoiceBinding;
import gov.nist.secauto.metaschema.databind.model.metaschema.IInstanceModelChoiceGroupBinding;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyConstraints;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.InlineDefineAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.JsonKey;

import java.math.BigInteger;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public class InstanceModelAssemblyInline
    extends AbstractInstanceModelNamedInline<
        InlineDefineAssembly,
        IBindingDefinitionAssembly,
        IBindingInstanceModelAssemblyAbsolute,
        IBindingContainerModelAbsolute>
    implements IBindingInstanceModelAssemblyAbsolute, IBindingDefinitionAssembly,
    IFeatureBindingContainerModelAssembly {
  @NonNull
  private final Lazy<IContainerFlagSupport<IBindingInstanceFlag>> flagContainer;
  @NonNull
  private final Lazy<IContainerModelAssemblySupport<
      IBindingInstanceModelAbsolute,
      IBindingInstanceModelNamedAbsolute,
      IBindingInstanceModelFieldAbsolute,
      IBindingInstanceModelAssemblyAbsolute,
      IInstanceModelChoiceBinding,
      IInstanceModelChoiceGroupBinding>> modelContainer;
  @NonNull
  private final Lazy<IModelConstrained> modelConstraints;

  /**
   * Construct a new assembly instance that defines the assembly inline.
   *
   * @param binding
   *          the assembly reference instance object bound to a Java class
   * @param bindingInstance
   *          the Metaschema module instance for the bound object
   * @param position
   *          the zero-based position of this bound object relative to its bound
   *          object siblings
   * @param parent
   *          the assembly definition containing this binding
   * @param nodeItemFactory
   *          the node item factory used to generate child nodes
   */
  public InstanceModelAssemblyInline(
      @NonNull InlineDefineAssembly binding,
      @NonNull IBoundInstanceModelGroupedAssembly bindingInstance,
      int position,
      @NonNull IBindingContainerModelAbsolute parent,
      @NonNull INodeItemFactory nodeItemFactory) {
    super(binding,
        bindingInstance,
        position,
        parent,
        ObjectUtils.requireNonNull(binding.getProps()),
        binding.getGroupAs());
    this.flagContainer = ObjectUtils.notNull(Lazy.lazy(() -> FlagContainerSupport.of(
        binding.getFlags(),
        bindingInstance,
        this)));
    this.modelContainer = ObjectUtils.notNull(Lazy.lazy(() -> AssemblyModelContainerSupport.of(
        binding.getModel(),
        ObjectUtils.requireNonNull(bindingInstance.getDefinition().getAssemblyInstanceByName("model")),
        this,
        nodeItemFactory)));
    this.modelConstraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IModelConstrained retval = new AssemblyConstraintSet();
      AssemblyConstraints constraints = getBinding().getConstraint();
      if (constraints != null) {
        ConstraintBindingSupport.parse(
            retval,
            constraints,
            ISource.modelSource(parent.getOwningDefinition().getContainingModule().getLocation()));
      }
      return retval;
    }));
  }

  @SuppressWarnings("null")
  @Override
  public IContainerFlagSupport<IBindingInstanceFlag> getFlagContainer() {
    return flagContainer.get();
  }

  @Override
  public IContainerModelAssemblySupport<
      IBindingInstanceModelAbsolute,
      IBindingInstanceModelNamedAbsolute,
      IBindingInstanceModelFieldAbsolute,
      IBindingInstanceModelAssemblyAbsolute,
      IInstanceModelChoiceBinding,
      IInstanceModelChoiceGroupBinding> getModelContainer() {
    return ObjectUtils.notNull(modelContainer.get());
  }

  @Override
  public IModelConstrained getConstraintSupport() {
    return ObjectUtils.notNull(modelConstraints.get());
  }

  @Override
  public IBindingDefinitionAssembly getDefinition() {
    return this;
  }

  @Override
  public IBindingInstanceModelAssemblyAbsolute getInlineInstance() {
    return this;
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
  public String getJsonKeyFlagName() {
    JsonKey jsonKey = getBinding().getJsonKey();
    return jsonKey == null ? null : jsonKey.getFlagRef();
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
}
