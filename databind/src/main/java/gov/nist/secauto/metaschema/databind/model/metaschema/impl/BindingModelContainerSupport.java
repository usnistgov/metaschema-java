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

import gov.nist.secauto.metaschema.core.model.AssemblyModelContainerSupport;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IModelContainer;
import gov.nist.secauto.metaschema.core.model.IModelInstance;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyModel;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyModel.Choice;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyModel.ChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyReference;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.FieldReference;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.InlineDefineAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.InlineDefineField;

import java.util.Collection;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public class BindingModelContainerSupport
    extends AssemblyModelContainerSupport {

  public BindingModelContainerSupport(
      @NonNull AssemblyModel model,
      @NonNull IAssemblyDefinition parent) {

    Collection<IModelInstance> modelInstances = getModelInstances();
    for (Object obj : ObjectUtils.notNull(model.getInstances())) {
      if (obj instanceof AssemblyReference) {
        IAssemblyInstance assembly = newInstance((AssemblyReference) obj, parent);
        addInstance(assembly);
      } else if (obj instanceof InlineDefineAssembly) {
        IAssemblyInstance assembly = new InstanceModelAssemblyInline((InlineDefineAssembly) obj, parent);
        addInstance(assembly);
      } else if (obj instanceof FieldReference) {
        IFieldInstance field = newInstance((FieldReference) obj, parent);
        addInstance(field);
      } else if (obj instanceof InlineDefineField) {
        IFieldInstance field = new InstanceModelFieldInline((InlineDefineField) obj, parent);
        addInstance(field);
      } else if (obj instanceof Choice) {
        IChoiceInstance choice = new InstanceModelChoice((Choice) obj, parent);
        modelInstances.add(choice);
      } else if (obj instanceof ChoiceGroup) {
        IChoiceGroupInstance choiceGroup = new InstanceModelChoiceGroup((ChoiceGroup) obj, parent);
        modelInstances.add(choiceGroup);
      } else {
        throw new UnsupportedOperationException(String.format("Unknown model instance class: %s", obj.getClass()));
      }
    }
  }

  public BindingModelContainerSupport(
      @NonNull List<Object> choices,
      @NonNull InstanceModelChoice parent) {
    for (Object obj : ObjectUtils.notNull(choices)) {
      if (obj instanceof AssemblyReference) {
        IAssemblyInstance assembly = newInstance((AssemblyReference) obj, parent);
        addInstance(assembly);
      } else if (obj instanceof InlineDefineAssembly) {
        IAssemblyInstance assembly = new InstanceModelAssemblyInline((InlineDefineAssembly) obj, parent);
        addInstance(assembly);
      } else if (obj instanceof FieldReference) {
        IFieldInstance field = newInstance((FieldReference) obj, parent);
        addInstance(field);
      } else if (obj instanceof InlineDefineField) {
        IFieldInstance field = new InstanceModelFieldInline((InlineDefineField) obj, parent);
        addInstance(field);
      } else {
        throw new UnsupportedOperationException(String.format("Unknown model instance class: %s", obj.getClass()));
      }
    }
  }

  private void addInstance(IAssemblyInstance assembly) {
    String effectiveName = assembly.getEffectiveName();
    getAssemblyInstanceMap().put(effectiveName, assembly);
    getNamedModelInstanceMap().put(effectiveName, assembly);
    getModelInstances().add(assembly);
  }

  private void addInstance(IFieldInstance field) {
    String effectiveName = field.getEffectiveName();
    getFieldInstanceMap().put(effectiveName, field);
    getNamedModelInstanceMap().put(effectiveName, field);
    getModelInstances().add(field);
  }

  private static IAssemblyInstance newInstance(
      @NonNull AssemblyReference obj,
      @NonNull IModelContainer parent) {
    IAssemblyDefinition owningDefinition = parent.getOwningDefinition();
    IModule<?, ?, ?, ?, ?> module = owningDefinition.getContainingModule();

    String name = ObjectUtils.requireNonNull(obj.getRef());
    IAssemblyDefinition definition = module.getScopedAssemblyDefinitionByName(name);

    if (definition == null) {
      throw new IllegalStateException(
          String.format("Unable to resolve assembly reference '%s' in definition '%s' in module '%s'",
              name,
              owningDefinition.getName(),
              module.getShortName()));
    }
    return new InstanceModelAssemblyReference(obj, definition, parent);
  }

  private static IFieldInstance newInstance(
      @NonNull FieldReference obj,
      @NonNull IModelContainer parent) {
    IAssemblyDefinition owningDefinition = parent.getOwningDefinition();
    IModule<?, ?, ?, ?, ?> module = owningDefinition.getContainingModule();

    String name = ObjectUtils.requireNonNull(obj.getRef());
    IFieldDefinition definition = module.getScopedFieldDefinitionByName(name);
    if (definition == null) {
      throw new IllegalStateException(
          String.format("Unable to resolve field reference '%s' in definition '%s' in module '%s'",
              name,
              owningDefinition.getName(),
              module.getShortName()));
    }
    return new InstanceModelFieldReference(obj, definition, parent);
  }
}
