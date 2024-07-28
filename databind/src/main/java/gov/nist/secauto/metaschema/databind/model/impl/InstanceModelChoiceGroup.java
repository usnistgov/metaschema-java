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

import gov.nist.secauto.metaschema.core.model.AbstractChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IBoundObject;
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
import gov.nist.secauto.metaschema.databind.model.info.IModelInstanceCollectionInfo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.talsmasoftware.lazy4j.Lazy;

/**
 * Implements a Metaschema module choice group instance bound to a Java field.
 */
public final class InstanceModelChoiceGroup
    extends AbstractChoiceGroupInstance<
        IBoundDefinitionModelAssembly,
        IBoundInstanceModelGroupedNamed,
        IBoundInstanceModelGroupedField,
        IBoundInstanceModelGroupedAssembly>
    // extends AbstractBoundInstanceModelJavaField<BoundChoiceGroup>
    implements IBoundInstanceModelChoiceGroup,
    IFeatureBoundContainerModelChoiceGroup, IFeatureInstanceModelGroupAs<IBoundObject> {
  @NonNull
  private final Field javaField;
  @NonNull
  private final BoundChoiceGroup annotation;
  @NonNull
  private final Lazy<IModelInstanceCollectionInfo<IBoundObject>> collectionInfo;
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
   * @param parent
   *          the definition containing this instance
   * @return the instance
   */
  @NonNull
  public static InstanceModelChoiceGroup newInstance(
      @NonNull Field javaField,
      @NonNull IBoundDefinitionModelAssembly parent) {
    BoundChoiceGroup annotation = ModelUtil.getAnnotation(javaField, BoundChoiceGroup.class);
    IGroupAs groupAs = ModelUtil.resolveDefaultGroupAs(annotation.groupAs(), parent.getContainingModule());
    if (annotation.maxOccurs() == -1 || annotation.maxOccurs() > 1) {
      if (IGroupAs.SINGLETON_GROUP_AS.equals(groupAs)) {
        throw new IllegalStateException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
            javaField.getName(),
            parent.getBoundClass().getName(),
            GroupAs.class.getName()));
      }
    } else if (!IGroupAs.SINGLETON_GROUP_AS.equals(groupAs)) {
      // max is 1 and a groupAs is set
      throw new IllegalStateException(
          String.format(
              "Field '%s' on class '%s' has the '%s' annotation, but maxOccurs=1. A groupAs must not be specfied.",
              javaField.getName(),
              parent.getBoundClass().getName(),
              GroupAs.class.getName()));
    }
    return new InstanceModelChoiceGroup(javaField, annotation, groupAs, parent);
  }

  @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
  private InstanceModelChoiceGroup(
      @NonNull Field javaField,
      @NonNull BoundChoiceGroup annotation,
      @NonNull IGroupAs groupAs,
      @NonNull IBoundDefinitionModelAssembly parent) {
    super(parent);
    this.javaField = javaField;
    this.annotation = annotation;
    this.groupAs = groupAs;
    this.collectionInfo = ObjectUtils.notNull(Lazy.lazy(() -> IModelInstanceCollectionInfo.of(this)));
    this.modelContainer = ObjectUtils.notNull(Lazy.lazy(() -> new ChoiceGroupModelContainerSupport(
        this.annotation.assemblies(),
        this.annotation.fields(),
        this)));
    this.classToInstanceMap = ObjectUtils.notNull(Lazy.lazy(() -> Collections.unmodifiableMap(
        getNamedModelInstances().stream()
            .map(instance -> instance)
            .collect(Collectors.toMap(
                item -> item.getDefinition().getBoundClass(),
                CustomCollectors.identity())))));
    this.qnameToInstanceMap = ObjectUtils.notNull(Lazy.lazy(() -> Collections.unmodifiableMap(
        getNamedModelInstances().stream()
            .collect(Collectors.toMap(
                IBoundInstanceModelGroupedNamed::getXmlQName,
                CustomCollectors.identity())))));
    this.discriminatorToInstanceMap = ObjectUtils.notNull(Lazy.lazy(() -> Collections.unmodifiableMap(
        getNamedModelInstances().stream()
            .collect(Collectors.toMap(
                IBoundInstanceModelGroupedNamed::getEffectiveDisciminatorValue,
                CustomCollectors.identity())))));
  }

  @Override
  public Field getField() {
    return javaField;
  }

  /**
   * Get the binding Java annotation.
   *
   * @return the binding Java annotation
   */
  @NonNull
  public BoundChoiceGroup getAnnotation() {
    return annotation;
  }

  @SuppressWarnings("null")
  @Override
  public IModelInstanceCollectionInfo<IBoundObject> getCollectionInfo() {
    return collectionInfo.get();
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
    return getParentContainer();
  }

  @Override
  public int getMinOccurs() {
    return getAnnotation().minOccurs();
  }

  @Override
  public int getMaxOccurs() {
    return getAnnotation().maxOccurs();
  }

  @Override
  public String getJsonDiscriminatorProperty() {
    return getAnnotation().discriminator();
  }

  @Override
  public String getJsonKeyFlagInstanceName() {
    return getAnnotation().jsonKey();
  }

  @Override
  public IBoundInstanceFlag getItemJsonKey(Object item) {
    String jsonKeyFlagName = getJsonKeyFlagInstanceName();
    IBoundInstanceFlag retval = null;

    if (jsonKeyFlagName != null) {
      Class<?> clazz = item.getClass();

      IBoundInstanceModelGroupedNamed itemInstance = getClassToInstanceMap().get(clazz);
      retval = itemInstance.getDefinition().getFlagInstanceByName(
          new QName(itemInstance.getXmlNamespace(), jsonKeyFlagName));
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
    private final Map<QName, IBoundInstanceModelGroupedNamed> namedModelInstances;
    @NonNull
    private final Map<QName, IBoundInstanceModelGroupedField> fieldInstances;
    @NonNull
    private final Map<QName, IBoundInstanceModelGroupedAssembly> assemblyInstances;

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
              IBoundInstanceModelGroupedAssembly::getXmlQName,
              Function.identity(),
              CustomCollectors.useLastMapper(),
              LinkedHashMap::new))));
      this.fieldInstances = CollectionUtil.unmodifiableMap(ObjectUtils.notNull(Arrays.stream(fields)
          .map(instance -> {
            assert instance != null;
            return IBoundInstanceModelGroupedField.newInstance(instance, container);
          })
          .collect(Collectors.toMap(
              IBoundInstanceModelGroupedField::getXmlQName,
              Function.identity(),
              CustomCollectors.useLastMapper(),
              LinkedHashMap::new))));
      this.namedModelInstances = CollectionUtil.unmodifiableMap(ObjectUtils.notNull(Stream.concat(
          this.assemblyInstances.entrySet().stream(),
          this.fieldInstances.entrySet().stream())
          .collect(Collectors.toMap(
              Entry::getKey,
              Entry::getValue,
              CustomCollectors.useLastMapper(),
              LinkedHashMap::new))));
    }

    @SuppressWarnings("null")
    @Override
    public Collection<IBoundInstanceModelGroupedNamed> getModelInstances() {
      return namedModelInstances.values();
    }

    @Override
    public Map<QName, IBoundInstanceModelGroupedNamed> getNamedModelInstanceMap() {
      return namedModelInstances;
    }

    @Override
    public Map<QName, IBoundInstanceModelGroupedField> getFieldInstanceMap() {
      return fieldInstances;
    }

    @Override
    public Map<QName, IBoundInstanceModelGroupedAssembly> getAssemblyInstanceMap() {
      return assemblyInstances;
    }
  }
}
