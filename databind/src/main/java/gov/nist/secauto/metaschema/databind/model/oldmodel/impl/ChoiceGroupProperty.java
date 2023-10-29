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

package gov.nist.secauto.metaschema.databind.model.oldmodel.impl;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.AbstractModelContainerSupport;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IModelContainerSupport;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.IGroupAs;
import gov.nist.secauto.metaschema.databind.model.info.IDataTypeHandler;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundAssemblyInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundChoiceGroupInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundFieldInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundGroupedAssemblyInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundGroupedFieldInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundGroupedNamedModelInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundModelInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IFieldClassBinding;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public class ChoiceGroupProperty
    extends AbstractModelProperty
    implements IBoundChoiceGroupInstance, IFeatureStandardBoundModelContainer {
  @NonNull
  private final BoundChoiceGroup boundChoiceGroup;
  @NonNull
  private final IGroupAs groupAs;
  @NonNull
  private final Lazy<ModelContainerSupport> modelContainer;

  public ChoiceGroupProperty(
      @NonNull Field field,
      @NonNull IAssemblyClassBinding containingDefinition) {
    super(field, containingDefinition);

    BoundChoiceGroup boundChoiceGroup = field.getAnnotation(BoundChoiceGroup.class);
    if (boundChoiceGroup == null) {
      throw new IllegalStateException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
          field.getName(), containingDefinition.getBoundClass().getName(), BoundChoiceGroup.class.getName()));
    }
    this.boundChoiceGroup = boundChoiceGroup;
    this.groupAs = IGroupAs.of(boundChoiceGroup.groupAs(), containingDefinition);
    if ((getMaxOccurs() == -1 || getMaxOccurs() > 1)) {
      if (IGroupAs.SINGLETON_GROUP_AS.equals(this.groupAs)) {
        throw new IllegalStateException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
            field.getName(),
            containingDefinition.getBoundClass().getName(),
            GroupAs.class.getName()));
      }
    } else if (!IGroupAs.SINGLETON_GROUP_AS.equals(this.groupAs)) {
      // max is 1 and a groupAs is set
      throw new IllegalStateException(
          String.format(
              "Field '%s' on class '%s' has the '%s' annotation, but maxOccurs=1. A groupAs must not be specfied.",
              field.getName(),
              containingDefinition.getBoundClass().getName(),
              GroupAs.class.getName()));
    }
    this.modelContainer = ObjectUtils.notNull(Lazy.lazy(() -> IFeatureBoundModelContainerSupport.newInstance(this)));
  }

  @Override
  public IAssemblyDefinition getOwningDefinition() {
    // TODO Auto-generated method stub
    return IBoundChoiceGroupInstance.super.getOwningDefinition();
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @NonNull
  protected BoundChoiceGroup getBindingAnnotation() {
    return boundChoiceGroup;
  }

  @Override
  public final int getMinOccurs() {
    return getBindingAnnotation().minOccurs();
  }

  @Override
  public final int getMaxOccurs() {
    return getBindingAnnotation().maxOccurs();
  }

  @Override
  public String getGroupAsName() {
    return groupAs.getGroupAsName();
  }

  @Override
  public String getGroupAsXmlNamespace() {
    return groupAs.getGroupAsXmlNamespace();
  }

  @Override
  public JsonGroupAsBehavior getJsonGroupAsBehavior() {
    return groupAs.getJsonGroupAsBehavior();
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    return groupAs.getXmlGroupAsBehavior();
  }

  @Override
  public String getJsonDiscriminatorProperty() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getJsonKeyFlagName() {
    // TODO Auto-generated method stub
    return null;
  }

  // ---------------------------------------
  // - End annotation driven code - CPD-ON -
  // ---------------------------------------
  @Override
  protected IDataTypeHandler newDataTypeHandler() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return null;
  }

  @Override
  public IModelContainerSupport<IBoundModelInstance, IBoundNamedModelInstance, IBoundFieldInstance,
      IBoundAssemblyInstance, IChoiceInstance, IBoundChoiceGroupInstance> getModelContainer() {
    // TODO Auto-generated method stub
    return null;
  }

  private class ModelContainerSupport
      extends AbstractModelContainerSupport<
          IBoundModelInstance,
          IBoundGroupedNamedModelInstance,
          IBoundGroupedFieldInstance,
          IBoundGroupedAssemblyInstance,
          IChoiceInstance,
          IBoundChoiceGroupInstance> {

    private ModelContainerSupport() {
      IBindingContext bindingContext = getContainingDefinition().getBindingContext();

      Map<String, IBoundGroupedNamedModelInstance> namedModelInstances = getNamedModelInstanceMap();

      Arrays.stream(getBindingAnnotation().modelInstances())
          .map(instance -> {
            Class<?> clazz = instance.type();
            IClassBinding classBinding = bindingContext.getClassBindingStrategy(clazz);

            IBoundGroupedNamedModelInstance retval;
            if (classBinding instanceof IAssemblyClassBinding) {
              retval = new GroupedAssemblyInstanceImpl(instance, (IAssemblyClassBinding) classBinding, this);
            } else if (classBinding instanceof IFieldClassBinding) {
              retval = new GroupedFieldInstanceImpl(instance, (IAssemblyClassBinding) classBinding, this);
            } else {
              throw new UnsupportedOperationException(classBinding.getClass().getName());
            }
            return retval;
          })
          .forEachOrdered(instance -> namedModelInstances.put(instance.getEffectiveName(), instance));

      {
        List<IBoundModelInstance> instances = getModelInstances();
        instances.addAll(namedModelInstances.values());
      }

      {
        Map<String, IBoundGroupedFieldInstance> instances = getFieldInstanceMap();
        namedModelInstances.values().stream()
            .filter(instance -> instance instanceof IBoundGroupedFieldInstance)
            .map(instance -> (IBoundGroupedFieldInstance) instance)
            .forEachOrdered(instance -> instances.put(instance.getEffectiveName(), instance));
      }

      {
        Map<String, IBoundGroupedAssemblyInstance> instances = getAssemblyInstanceMap();
        namedModelInstances.values().stream()
            .filter(instance -> instance instanceof IBoundGroupedAssemblyInstance)
            .map(instance -> (IBoundGroupedAssemblyInstance) instance)
            .forEachOrdered(instance -> instances.put(instance.getEffectiveName(), instance));
      }
    }
  }
}
