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

import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItemFactory;
import gov.nist.secauto.metaschema.core.model.IContainerModelSupport;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceModelAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceModelAssemblyAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceModelFieldAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceModelNamedAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.IInstanceModelChoiceBinding;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyModel.Choice;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyReference;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.FieldReference;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.InlineDefineAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.InlineDefineField;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class ChoiceModelContainerSupport
    extends AbstractBindingModelContainerSupport {
  @NonNull
  private final List<IBindingInstanceModelAbsolute> modelInstances;
  @NonNull
  private final Map<String, IBindingInstanceModelNamedAbsolute> namedModelInstances;
  @NonNull
  private final Map<String, IBindingInstanceModelFieldAbsolute> fieldInstances;
  @NonNull
  private final Map<String, IBindingInstanceModelAssemblyAbsolute> assemblyInstances;

  @SuppressWarnings("PMD.ShortMethodName")
  public static IContainerModelSupport<
      IBindingInstanceModelAbsolute,
      IBindingInstanceModelNamedAbsolute,
      IBindingInstanceModelFieldAbsolute,
      IBindingInstanceModelAssemblyAbsolute> of(
          @Nullable Choice binding,
          @NonNull IBoundInstanceModelGroupedAssembly bindingInstance,
          @NonNull IInstanceModelChoiceBinding parent,
          @NonNull INodeItemFactory nodeItemFactory) {
    List<Object> instances;
    return binding == null || (instances = binding.getChoices()) == null || instances.isEmpty()
        ? IContainerModelSupport.empty()
        : new ChoiceModelContainerSupport(
            binding,
            bindingInstance,
            parent,
            nodeItemFactory);
  }

  /**
   * Construct a new assembly model container.
   *
   * @param binding
   *          the choice model object bound to a Java class
   * @param bindingInstance
   *          the Metaschema module instance for the bound model object
   * @param parent
   *          the assembly definition containing this container
   * @param nodeItemFactory
   *          the node item factory used to generate child nodes
   */
  @SuppressWarnings({ "PMD.AvoidInstantiatingObjectsInLoops", "PMD.UseConcurrentHashMap", "PMD.PrematureDeclaration" })
  public ChoiceModelContainerSupport(
      @NonNull Choice binding,
      @NonNull IBoundInstanceModelGroupedAssembly bindingInstance,
      @NonNull IInstanceModelChoiceBinding parent,
      @NonNull INodeItemFactory nodeItemFactory) {

    // create temporary collections to store the child binding objects
    final List<IBindingInstanceModelAbsolute> modelInstances = new LinkedList<>();
    final Map<String, IBindingInstanceModelNamedAbsolute> namedModelInstances = new LinkedHashMap<>();
    final Map<String, IBindingInstanceModelFieldAbsolute> fieldInstances = new LinkedHashMap<>();
    final Map<String, IBindingInstanceModelAssemblyAbsolute> assemblyInstances = new LinkedHashMap<>();

    // create counters to track child positions
    int assemblyReferencePosition = 0;
    int assemblyInlineDefinitionPosition = 0;
    int fieldReferencePosition = 0;
    int fieldInlineDefinitionPosition = 0;

    // TODO: make "instances" a constant
    IBoundInstanceModelChoiceGroup instance = ObjectUtils.requireNonNull(
        bindingInstance.getDefinition().getChoiceGroupInstanceByName("choices"));
    for (Object obj : ObjectUtils.notNull(binding.getChoices())) {
      IBoundInstanceModelGroupedAssembly objInstance
          = (IBoundInstanceModelGroupedAssembly) instance.getItemInstance(obj);

      if (obj instanceof AssemblyReference) {
        IBindingInstanceModelAssemblyAbsolute assembly = newInstance(
            (AssemblyReference) obj,
            objInstance,
            assemblyReferencePosition++,
            parent);
        addInstance(assembly, modelInstances, namedModelInstances, assemblyInstances);
      } else if (obj instanceof InlineDefineAssembly) {
        IBindingInstanceModelAssemblyAbsolute assembly = new InstanceModelAssemblyInline(
            (InlineDefineAssembly) obj,
            objInstance,
            assemblyInlineDefinitionPosition++,
            parent,
            nodeItemFactory);
        addInstance(assembly, modelInstances, namedModelInstances, assemblyInstances);
      } else if (obj instanceof FieldReference) {
        IBindingInstanceModelFieldAbsolute field = newInstance(
            (FieldReference) obj,
            objInstance,
            fieldReferencePosition++,
            parent);
        addInstance(field, modelInstances, namedModelInstances, fieldInstances);
      } else if (obj instanceof InlineDefineField) {
        IBindingInstanceModelFieldAbsolute field = new InstanceModelFieldInline(
            (InlineDefineField) obj,
            objInstance,
            fieldInlineDefinitionPosition++,
            parent);
        addInstance(field, modelInstances, namedModelInstances, fieldInstances);
      } else {
        throw new UnsupportedOperationException(String.format("Unknown model instance class: %s", obj.getClass()));
      }
    }

    this.modelInstances = modelInstances.isEmpty()
        ? CollectionUtil.emptyList()
        : CollectionUtil.unmodifiableList(modelInstances);
    this.namedModelInstances = namedModelInstances.isEmpty()
        ? CollectionUtil.emptyMap()
        : CollectionUtil.unmodifiableMap(namedModelInstances);
    this.fieldInstances = fieldInstances.isEmpty()
        ? CollectionUtil.emptyMap()
        : CollectionUtil.unmodifiableMap(fieldInstances);
    this.assemblyInstances = assemblyInstances.isEmpty()
        ? CollectionUtil.emptyMap()
        : CollectionUtil.unmodifiableMap(assemblyInstances);
  }

  @Override
  public List<IBindingInstanceModelAbsolute> getModelInstances() {
    return modelInstances;
  }

  @Override
  public Map<String, IBindingInstanceModelNamedAbsolute> getNamedModelInstanceMap() {
    return namedModelInstances;
  }

  @Override
  public Map<String, IBindingInstanceModelFieldAbsolute> getFieldInstanceMap() {
    return fieldInstances;
  }

  @Override
  public Map<String, IBindingInstanceModelAssemblyAbsolute> getAssemblyInstanceMap() {
    return assemblyInstances;
  }
}
