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

import gov.nist.secauto.metaschema.core.model.GroupedModelContainerSupport;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IFeatureGroupedModelContainer;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IGroupedAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IGroupedFieldInstance;
import gov.nist.secauto.metaschema.core.model.IGroupedNamedModelInstance;
import gov.nist.secauto.metaschema.core.model.IModelContainer;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyModel;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyModel.ChoiceGroup.Assembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyModel.ChoiceGroup.DefineAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.JsonKey;

import java.math.BigInteger;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public class InstanceModelChoiceGroup
    extends AbstractInstanceModel<AssemblyModel.ChoiceGroup, IModelContainer>
    implements IChoiceGroupInstance,
    IFeatureGroupedModelContainer<
        IGroupedNamedModelInstance,
        IGroupedFieldInstance,
        IGroupedAssemblyInstance> {
  @NonNull
  private final Lazy<ModelContainerSupport> modelContainer;

  public InstanceModelChoiceGroup(
      @NonNull AssemblyModel.ChoiceGroup binding,
      @NonNull IAssemblyDefinition parent) {
    super(binding, parent);
    this.modelContainer = ObjectUtils.notNull(Lazy.lazy(() -> {
      return new ModelContainerSupport(binding, this);
    }));
  }

  @Override
  public ModelContainerSupport getModelContainer() {
    return ObjectUtils.notNull(modelContainer.get());
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
  public IAssemblyDefinition getOwningDefinition() {
    return getContainingDefinition();
  }

  @Override
  public String getJsonDiscriminatorProperty() {
    String discriminator = getBinding().getDiscriminator();
    return discriminator == null ? MetaschemaModelConstants.DEFAULT_JSON_DISCRIMINATOR_PROPERTY_NAME : discriminator;
  }

  @Override
  public String getJsonKeyFlagName() {
    JsonKey jsonKey = getBinding().getJsonKey();
    return jsonKey == null ? null : jsonKey.getFlagRef();
  }

  private static class ModelContainerSupport
      extends GroupedModelContainerSupport {

    public ModelContainerSupport(
        @NonNull AssemblyModel.ChoiceGroup binding,
        @NonNull InstanceModelChoiceGroup parent) {
      for (Object obj : ObjectUtils.notNull(binding.getChoices())) {
        if (obj instanceof Assembly) {
          IGroupedAssemblyInstance assembly = newInstance((AssemblyModel.ChoiceGroup.Assembly) obj, parent);
          addInstance(assembly);
        } else if (obj instanceof DefineAssembly) {
          IGroupedAssemblyInstance assembly
              = new InstanceModelGroupedAssemblyInline((AssemblyModel.ChoiceGroup.DefineAssembly) obj, parent);
          addInstance(assembly);
        } else if (obj instanceof AssemblyModel.ChoiceGroup.Field) {
          IGroupedFieldInstance field = newInstance((AssemblyModel.ChoiceGroup.Field) obj, parent);
          addInstance(field);
        } else if (obj instanceof AssemblyModel.ChoiceGroup.DefineField) {
          IGroupedFieldInstance field
              = new InstanceModelGroupedFieldInline((AssemblyModel.ChoiceGroup.DefineField) obj, parent);
          addInstance(field);
        } else {
          throw new UnsupportedOperationException(String.format("Unknown model instance class: %s", obj.getClass()));
        }
      }
    }

    private void addInstance(IGroupedAssemblyInstance assembly) {
      String effectiveName = assembly.getEffectiveName();
      getAssemblyInstanceMap().put(effectiveName, assembly);
      getNamedModelInstanceMap().put(effectiveName, assembly);
    }

    private void addInstance(IGroupedFieldInstance field) {
      String effectiveName = field.getEffectiveName();
      getFieldInstanceMap().put(effectiveName, field);
      getNamedModelInstanceMap().put(effectiveName, field);
    }

    private static IGroupedAssemblyInstance newInstance(
        @NonNull AssemblyModel.ChoiceGroup.Assembly obj,
        @NonNull IChoiceGroupInstance parent) {
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
      return new InstanceModelGroupedAssemblyReference(obj, definition, parent);
    }

    private static IGroupedFieldInstance newInstance(
        @NonNull AssemblyModel.ChoiceGroup.Field obj,
        @NonNull IChoiceGroupInstance parent) {
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
      return new InstanceModelGroupedFieldReference(obj, definition, parent);
    }
  }

}
