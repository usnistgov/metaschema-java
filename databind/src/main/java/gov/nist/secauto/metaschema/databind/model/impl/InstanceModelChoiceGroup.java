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

import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBindingInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedField;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedNamed;
import gov.nist.secauto.metaschema.databind.model.IFeatureBoundClass;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedField;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemWriteHandler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public class InstanceModelChoiceGroup
    extends AbstractBoundInstanceModelJavaField<BoundChoiceGroup>
    implements IBoundInstanceModelChoiceGroup,
    IFeatureBoundContainerModelChoiceGroup {
  @NonNull
  private final IGroupAs groupAs;
  @NonNull
  private final Lazy<ChoiceGroupModelContainerSupport> modelContainer;
  @NonNull
  private final Lazy<Map<Class<?>, IBoundInstanceModelGroupedNamed>> classToInstanceMap;
  @NonNull
  private final Lazy<Map<QName, IBoundInstanceModelGroupedNamed>> qnameToInstanceMap;
  @NonNull
  private final BindingInstanceChoiceGroup binding;

  public InstanceModelChoiceGroup(
      @NonNull Field javaField,
      @NonNull IBoundDefinitionAssembly containingDefinition) {
    super(javaField, BoundChoiceGroup.class, containingDefinition);
    this.groupAs = IGroupAs.of(getAnnotation().groupAs(), containingDefinition);
    if ((getMaxOccurs() == -1 || getMaxOccurs() > 1)) {
      if (IGroupAs.SINGLETON_GROUP_AS.equals(this.groupAs)) {
        throw new IllegalStateException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
            getField().getName(),
            containingDefinition.getBoundClass().getName(),
            GroupAs.class.getName()));
      }
    } else if (!IGroupAs.SINGLETON_GROUP_AS.equals(this.groupAs)) {
      // max is 1 and a groupAs is set
      throw new IllegalStateException(
          String.format(
              "Field '%s' on class '%s' has the '%s' annotation, but maxOccurs=1. A groupAs must not be specfied.",
              getField().getName(),
              containingDefinition.getBoundClass().getName(),
              GroupAs.class.getName()));
    }
    this.modelContainer = ObjectUtils.notNull(Lazy.lazy(() -> new ChoiceGroupModelContainerSupport(
        getAnnotation().assemblies(),
        getAnnotation().fields(),
        this)));
    this.classToInstanceMap = ObjectUtils.notNull(Lazy.lazy(() -> Collections.unmodifiableMap(
        getModelInstances().stream()
            .map(instance -> instance)
            .collect(Collectors.toMap(
                item -> (Class<?>) ((IFeatureBoundClass) item.getDefinition()).getBoundClass(),
                CustomCollectors.identity())))));
    this.qnameToInstanceMap = ObjectUtils.notNull(Lazy.lazy(() -> Collections.unmodifiableMap(
        getModelInstances().stream()
            .collect(Collectors.toMap(
                item -> item.getXmlQName(),
                CustomCollectors.identity())))));

    this.binding = new BindingInstanceChoiceGroup();
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @SuppressWarnings("null")
  @Override
  public Map<QName, IBoundInstanceModelGroupedNamed> getQNameToInstanceMap() {
    return qnameToInstanceMap.get();
  }

  @SuppressWarnings("null")
  @Override
  public Map<Class<?>, IBoundInstanceModelGroupedNamed> getClassToInstanceMap() {
    return classToInstanceMap.get();
  }

  @Override
  public BindingInstanceChoiceGroup getInstanceBinding() {
    return binding;
  }

  @Override
  public IGroupAs getGroupAs() {
    return groupAs;
  }

  @SuppressWarnings("null")
  @Override
  public ChoiceGroupModelContainerSupport getModelContainer() {
    return modelContainer.get();
  }

  @Override
  public IBoundDefinitionAssembly getOwningDefinition() {
    return getContainingDefinition();
  }

  @Override
  public Collection<? extends Object> getItemValues(Object value) {
    return getInstanceBinding().getCollectionInfo().getItemsFromValue(value);
  }

  @Override
  public final int getMinOccurs() {
    return getAnnotation().minOccurs();
  }

  @Override
  public final int getMaxOccurs() {
    return getAnnotation().maxOccurs();
  }

  @Override
  public String getJsonName() {
    // always the group-as name
    return ObjectUtils.notNull(getGroupAsName());
  }

  @Override
  public String getJsonDiscriminatorProperty() {
    return getAnnotation().discriminator();
  }

  @Override
  public String getJsonKeyFlagName() {
    return getAnnotation().jsonKey();
  }

  private static class ChoiceGroupModelContainerSupport
      extends AbstractModelContainerSupport<
          IBoundInstanceModelGroupedNamed,
          IBoundInstanceModelGroupedNamed,
          IBoundInstanceModelGroupedField,
          IBoundInstanceModelGroupedAssembly,
          IBoundInstanceModelChoiceGroup> {

    public ChoiceGroupModelContainerSupport(
        @NonNull BoundGroupedAssembly[] assemblies,
        @NonNull BoundGroupedField[] fields,
        @NonNull IBoundInstanceModelChoiceGroup container) {
      super(ObjectUtils.notNull(Stream.concat(
          Arrays.stream(assemblies)
              .map(instance -> {
                assert instance != null;
                return IBoundInstanceModelGroupedAssembly.newInstance(instance, container);
              }),
          Arrays.stream(fields)
              .map(instance -> {
                assert instance != null;
                return IBoundInstanceModelGroupedField.newInstance(instance, container);
              }))),
          IBoundInstanceModelGroupedNamed.class,
          IBoundInstanceModelGroupedField.class,
          IBoundInstanceModelGroupedAssembly.class);
    }
  }

  private class BindingInstanceChoiceGroup
      extends
      AbstractBindingInstanceModel
      implements IBindingInstanceModelChoiceGroup {

    @Override
    public IBoundInstanceModel getInstance() {
      return InstanceModelChoiceGroup.this;
    }

    @Override
    public IBoundInstanceFlag getItemJsonKey(Object item) {
      String jsonKeyFlagName = getJsonKeyFlagName();
      IBoundInstanceFlag retval = null;

      if (jsonKeyFlagName != null) {
        Class<?> clazz = item.getClass();

        IBoundInstanceModelGroupedNamed itemInstance = getClassToInstanceMap().get(clazz);
        retval = itemInstance.getDefinition().getFlagInstanceByName(jsonKeyFlagName);
      }
      return retval;
    }

    @Override
    public boolean canHandleJsonPropertyName(String name) {
      return name.equals(getJsonName());
    }

    @Override
    public boolean canHandleXmlQName(QName qname) {
      return qnameToInstanceMap.get().containsKey(qname);
    }

    @Override
    public Field getField() {
      return InstanceModelChoiceGroup.this.getField();
    }

    @Override
    @Nullable
    public String getJsonKeyFlagName() {
      return getInstance().getJsonKeyFlagName();
    }

    @Override
    public Object readItem(Object parent, IItemReadHandler handler) throws IOException {
      return handler.readChoiceGroupItem(parent, InstanceModelChoiceGroup.this);
    }

    @Override
    public void writeItem(Object item, IItemWriteHandler handler) throws IOException {
      IBoundInstanceModelGroupedNamed itemInstance = getItemInstance(item);
      handler.writeChoiceGroupItem(item, InstanceModelChoiceGroup.this, itemInstance);

    }

    @Override
    public Object deepCopyItem(Object item, Object parentInstance) throws BindingException {
      IBoundInstanceModelGroupedNamed itemInstance = getItemInstance(item);
      return itemInstance.getInstanceBinding().deepCopyItem(itemInstance, parentInstance);
    }
  }
}
