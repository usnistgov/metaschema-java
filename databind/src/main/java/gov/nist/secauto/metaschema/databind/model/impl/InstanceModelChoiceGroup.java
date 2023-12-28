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

import gov.nist.secauto.metaschema.core.model.IContainerModelSupport;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedField;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedNamed;
import gov.nist.secauto.metaschema.databind.model.IGroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedField;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

/**
 * Implements a Metaschema module choice group instance bound to a Java field.
 */
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
  private final Lazy<Map<String, IBoundInstanceModelGroupedNamed>> discriminatorToInstanceMap;

  /**
   * Construct a new Metaschema module choice group instance.
   *
   * @param javaField
   *          the Java field bound to this instance
   * @param containingDefinition
   *          the definition containing this instance
   */
  public InstanceModelChoiceGroup(
      @NonNull Field javaField,
      @NonNull IBoundDefinitionModelAssembly containingDefinition) {
    super(javaField, BoundChoiceGroup.class, containingDefinition);
    this.groupAs = ModelUtil.groupAs(getAnnotation().groupAs());
    if (getMaxOccurs() == -1 || getMaxOccurs() > 1) {
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
        getNamedModelInstances().stream()
            .map(instance -> instance)
            .collect(Collectors.toMap(
                item -> (Class<?>) item.getDefinition().getBoundClass(),
                CustomCollectors.identity())))));
    this.qnameToInstanceMap = ObjectUtils.notNull(Lazy.lazy(() -> Collections.unmodifiableMap(
        getNamedModelInstances().stream()
            .collect(Collectors.toMap(
                item -> item.getXmlQName(),
                CustomCollectors.identity())))));
    this.discriminatorToInstanceMap = ObjectUtils.notNull(Lazy.lazy(() -> Collections.unmodifiableMap(
        getNamedModelInstances().stream()
            .collect(Collectors.toMap(
                item -> item.getEffectiveDisciminatorValue(),
                CustomCollectors.identity())))));
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  /**
   * Get the mapping of XML qualified names bound to a distinct grouped model
   * instance.
   *
   * @return the mapping
   */
  @SuppressWarnings("null")
  @NonNull
  protected Map<QName, IBoundInstanceModelGroupedNamed> getQNameToInstanceMap() {
    return qnameToInstanceMap.get();
  }

  /**
   * Get the mapping of Java classes bound to a distinct grouped model instance.
   *
   * @return the mapping
   */
  @SuppressWarnings("null")
  @NonNull
  protected Map<Class<?>, IBoundInstanceModelGroupedNamed> getClassToInstanceMap() {
    return classToInstanceMap.get();
  }

  /**
   * Get the mapping of JSON discriminator values bound to a distinct grouped
   * model instance.
   *
   * @return the mapping
   */
  @SuppressWarnings("null")
  @NonNull
  protected Map<String, IBoundInstanceModelGroupedNamed> getDiscriminatorToInstanceMap() {
    return discriminatorToInstanceMap.get();
  }

  @Override
  @Nullable
  public IBoundInstanceModelGroupedNamed getGroupedModelInstance(@NonNull Class<?> clazz) {
    return getClassToInstanceMap().get(clazz);
  }

  @Override
  @Nullable
  public IBoundInstanceModelGroupedNamed getGroupedModelInstance(@NonNull QName name) {
    return getQNameToInstanceMap().get(name);
  }

  @Override
  public IBoundInstanceModelGroupedNamed getGroupedModelInstance(String discriminator) {
    return getDiscriminatorToInstanceMap().get(discriminator);
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
  public IBoundDefinitionModelAssembly getOwningDefinition() {
    return getContainingDefinition();
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
  public String getJsonDiscriminatorProperty() {
    return getAnnotation().discriminator();
  }

  @Override
  public String getJsonKeyFlagName() {
    return getAnnotation().jsonKey();
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

  private static class ChoiceGroupModelContainerSupport
      implements IContainerModelSupport<
          IBoundInstanceModelGroupedNamed,
          IBoundInstanceModelGroupedNamed,
          IBoundInstanceModelGroupedField,
          IBoundInstanceModelGroupedAssembly> {

    @NonNull
    private final Map<String, IBoundInstanceModelGroupedNamed> namedModelInstances;
    @NonNull
    private final Map<String, IBoundInstanceModelGroupedField> fieldInstances;
    @NonNull
    private final Map<String, IBoundInstanceModelGroupedAssembly> assemblyInstances;

    public ChoiceGroupModelContainerSupport(
        @NonNull BoundGroupedAssembly[] assemblies,
        @NonNull BoundGroupedField[] fields,
        @NonNull IBoundInstanceModelChoiceGroup container) {
      this.assemblyInstances = CollectionUtil.unmodifiableMap(ObjectUtils.notNull(Arrays.stream(assemblies)
          .map(instance -> {
            assert instance != null;
            return IBoundInstanceModelGroupedAssembly.newInstance(instance,
                container);
          })
          .collect(Collectors.toMap(
              instance -> instance.getEffectiveName(),
              Function.identity(),
              CustomCollectors.useLastMapper(),
              LinkedHashMap::new))));
      this.fieldInstances = CollectionUtil.unmodifiableMap(ObjectUtils.notNull(Arrays.stream(fields)
          .map(instance -> {
            assert instance != null;
            return IBoundInstanceModelGroupedField.newInstance(instance, container);
          })
          .collect(Collectors.toMap(
              instance -> instance.getEffectiveName(),
              Function.identity(),
              CustomCollectors.useLastMapper(),
              LinkedHashMap::new))));
      this.namedModelInstances = CollectionUtil.unmodifiableMap(ObjectUtils.notNull(Stream.concat(
          this.assemblyInstances.entrySet().stream(),
          this.fieldInstances.entrySet().stream())
          .collect(Collectors.toMap(
              entry -> entry.getKey(),
              entry -> entry.getValue(),
              CustomCollectors.useLastMapper(),
              LinkedHashMap::new))));
    }

    @SuppressWarnings("null")
    @Override
    public Collection<IBoundInstanceModelGroupedNamed> getModelInstances() {
      return namedModelInstances.values();
    }

    @Override
    public Map<String, IBoundInstanceModelGroupedNamed> getNamedModelInstanceMap() {
      return namedModelInstances;
    }

    @Override
    public Map<String, IBoundInstanceModelGroupedField> getFieldInstanceMap() {
      return fieldInstances;
    }

    @Override
    public Map<String, IBoundInstanceModelGroupedAssembly> getAssemblyInstanceMap() {
      return assemblyInstances;
    }
  }
}
