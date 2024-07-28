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

import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IContainerModelAssemblySupport;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelField;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelNamed;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.Ignore;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class AssemblyModelContainerSupport
    implements IContainerModelAssemblySupport<
        IBoundInstanceModel<?>,
        IBoundInstanceModelNamed<?>,
        IBoundInstanceModelField<?>,
        IBoundInstanceModelAssembly,
        IChoiceInstance,
        IBoundInstanceModelChoiceGroup> {
  @NonNull
  private final List<IBoundInstanceModel<?>> modelInstances;
  @NonNull
  private final Map<QName, IBoundInstanceModelNamed<?>> namedModelInstances;
  @NonNull
  private final Map<QName, IBoundInstanceModelField<?>> fieldInstances;
  @NonNull
  private final Map<QName, IBoundInstanceModelAssembly> assemblyInstances;
  @NonNull
  private final Map<String, IBoundInstanceModelChoiceGroup> choiceGroupInstances;

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
  public AssemblyModelContainerSupport(
      @NonNull DefinitionAssembly containingDefinition) {
    this.modelInstances = CollectionUtil.unmodifiableList(ObjectUtils.notNull(
        getModelInstanceStream(containingDefinition, containingDefinition.getBoundClass())
            .collect(Collectors.toUnmodifiableList())));

    Map<QName, IBoundInstanceModelNamed<?>> namedModelInstances = new LinkedHashMap<>();
    Map<QName, IBoundInstanceModelField<?>> fieldInstances = new LinkedHashMap<>();
    Map<QName, IBoundInstanceModelAssembly> assemblyInstances = new LinkedHashMap<>();
    Map<String, IBoundInstanceModelChoiceGroup> choiceGroupInstances = new LinkedHashMap<>();
    for (IBoundInstanceModel<?> instance : this.modelInstances) {
      if (instance instanceof IBoundInstanceModelNamed) {
        IBoundInstanceModelNamed<?> named = (IBoundInstanceModelNamed<?>) instance;
        QName key = named.getXmlQName();
        namedModelInstances.put(key, named);

        if (instance instanceof IBoundInstanceModelField) {
          fieldInstances.put(key, (IBoundInstanceModelField<?>) named);
        } else if (instance instanceof IBoundInstanceModelAssembly) {
          assemblyInstances.put(key, (IBoundInstanceModelAssembly) named);
        }
      } else if (instance instanceof IBoundInstanceModelChoiceGroup) {
        IBoundInstanceModelChoiceGroup choiceGroup = (IBoundInstanceModelChoiceGroup) instance;
        String key = ObjectUtils.requireNonNull(choiceGroup.getGroupAsName());
        choiceGroupInstances.put(key, choiceGroup);
      }
    }

    this.namedModelInstances = namedModelInstances.isEmpty()
        ? CollectionUtil.emptyMap()
        : CollectionUtil.unmodifiableMap(namedModelInstances);
    this.fieldInstances = fieldInstances.isEmpty()
        ? CollectionUtil.emptyMap()
        : CollectionUtil.unmodifiableMap(fieldInstances);
    this.assemblyInstances = assemblyInstances.isEmpty()
        ? CollectionUtil.emptyMap()
        : CollectionUtil.unmodifiableMap(assemblyInstances);
    this.choiceGroupInstances = choiceGroupInstances.isEmpty()
        ? CollectionUtil.emptyMap()
        : CollectionUtil.unmodifiableMap(choiceGroupInstances);
  }

  protected static IBoundInstanceModel<?> newBoundModelInstance(
      @NonNull Field field,
      @NonNull IBoundDefinitionModelAssembly definition) {
    IBoundInstanceModel<?> retval = null;
    if (field.isAnnotationPresent(BoundAssembly.class)) {
      retval = IBoundInstanceModelAssembly.newInstance(field, definition);
    } else if (field.isAnnotationPresent(BoundField.class)) {
      retval = IBoundInstanceModelField.newInstance(field, definition);
    } else if (field.isAnnotationPresent(BoundChoiceGroup.class)) {
      retval = IBoundInstanceModelChoiceGroup.newInstance(field, definition);
    }
    return retval;
  }

  @NonNull
  protected static Stream<IBoundInstanceModel<?>> getModelInstanceStream(
      @NonNull IBoundDefinitionModelAssembly definition,
      @NonNull Class<?> clazz) {

    Stream<IBoundInstanceModel<?>> superInstances;
    Class<?> superClass = clazz.getSuperclass();
    if (superClass == null) {
      superInstances = Stream.empty();
    } else {
      // get instances from superclass
      superInstances = getModelInstanceStream(definition, superClass);
    }

    return ObjectUtils.notNull(Stream.concat(superInstances, Arrays.stream(clazz.getDeclaredFields())
        // skip this field, since it is ignored
        .filter(field -> !field.isAnnotationPresent(Ignore.class))
        // skip fields that aren't a Module field or assembly instance
        .filter(field -> field.isAnnotationPresent(BoundField.class)
            || field.isAnnotationPresent(BoundAssembly.class)
            || field.isAnnotationPresent(BoundChoiceGroup.class))
        .map(field -> {
          assert field != null;

          IBoundInstanceModel<?> retval = newBoundModelInstance(field, definition);
          if (retval == null) {
            throw new IllegalStateException(
                String.format("The field '%s' on class '%s' is not bound", field.getName(), clazz.getName()));
          }
          return retval;
        })
        .filter(Objects::nonNull)));
  }

  @Override
  public Collection<IBoundInstanceModel<?>> getModelInstances() {
    return modelInstances;
  }

  @Override
  public Map<QName, IBoundInstanceModelNamed<?>> getNamedModelInstanceMap() {
    return namedModelInstances;
  }

  @Override
  public Map<QName, IBoundInstanceModelField<?>> getFieldInstanceMap() {
    return fieldInstances;
  }

  @Override
  public Map<QName, IBoundInstanceModelAssembly> getAssemblyInstanceMap() {
    return assemblyInstances;
  }

  @Override
  public List<IChoiceInstance> getChoiceInstances() {
    // not supported
    return CollectionUtil.emptyList();
  }

  @Override
  public Map<String, IBoundInstanceModelChoiceGroup> getChoiceGroupInstanceMap() {
    return choiceGroupInstances;
  }
}
