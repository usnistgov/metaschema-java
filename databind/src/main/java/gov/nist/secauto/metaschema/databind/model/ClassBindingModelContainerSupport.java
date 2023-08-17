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

package gov.nist.secauto.metaschema.databind.model;

import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IModelContainerSupport;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.Ignore;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ClassBindingModelContainerSupport
    implements IModelContainerSupport<IBoundNamedModelInstance, IBoundNamedModelInstance, IBoundFieldInstance,
        IBoundAssemblyInstance, IChoiceInstance> {
  @NonNull
  private final Map<String, IBoundNamedModelInstance> modelInstances;
  @NonNull
  private final Map<String, IBoundFieldInstance> fieldInstances;
  @NonNull
  private final Map<String, IBoundAssemblyInstance> assemblyInstances;

  public ClassBindingModelContainerSupport(
      @NonNull IAssemblyClassBinding classBinding) {
    Class<?> clazz = classBinding.getBoundClass();
    this.modelInstances = CollectionUtil.unmodifiableMap(ObjectUtils.notNull(
        getModelInstanceFieldStream(classBinding, clazz)
            .collect(Collectors.toMap(instance -> instance.getEffectiveName(), Function.identity(),
                CustomCollectors.useLastMapper(),
                LinkedHashMap::new))));

    this.fieldInstances = CollectionUtil.unmodifiableMap(ObjectUtils.notNull(
        getNamedModelInstanceMap().values().stream()
            .filter(instance -> instance instanceof IBoundFieldInstance)
            .map(instance -> (IBoundFieldInstance) instance)
            .map(ObjectUtils::notNull)
            .collect(Collectors.toMap(IBoundFieldInstance::getEffectiveName, Function.identity(),
                CustomCollectors.useLastMapper(),
                LinkedHashMap::new))));

    this.assemblyInstances = CollectionUtil.unmodifiableMap(ObjectUtils.notNull(
        getNamedModelInstanceMap().values().stream()
            .filter(instance -> instance instanceof IBoundAssemblyInstance)
            .map(instance -> (IBoundAssemblyInstance) instance)
            .map(ObjectUtils::notNull)
            .collect(Collectors.toMap(IBoundAssemblyInstance::getEffectiveName, Function.identity(),
                CustomCollectors.useLastMapper(),
                LinkedHashMap::new))));
  }

  protected Stream<IBoundNamedModelInstance> getModelInstanceFieldStream(
      @NonNull IAssemblyClassBinding classBinding,
      @NonNull Class<?> clazz) {

    Stream<IBoundNamedModelInstance> superInstances;
    Class<?> superClass = clazz.getSuperclass();
    if (superClass == null) {
      superInstances = Stream.empty();
    } else {
      // get instances from superclass
      superInstances = getModelInstanceFieldStream(classBinding, superClass);
    }

    IBindingContext bindingContext = classBinding.getBindingContext();
    return Stream.concat(superInstances, Arrays.stream(clazz.getDeclaredFields())
        // skip this field, since it is ignored
        .filter(field -> !field.isAnnotationPresent(Ignore.class))
        // skip fields that aren't a Metaschema field or assembly instance
        .filter(field -> field.isAnnotationPresent(BoundField.class) || field.isAnnotationPresent(BoundAssembly.class))
        .map(field -> {
          assert field != null;

          IBoundNamedModelInstance retval;
          if (field.isAnnotationPresent(BoundAssembly.class)
              && bindingContext.getClassBinding(IBoundNamedModelInstance.getItemType(field)) != null) {
            retval = IBoundAssemblyInstance.newInstance(field, classBinding);
          } else if (field.isAnnotationPresent(BoundField.class)) {
            retval = IBoundFieldInstance.newInstance(field, classBinding);
          } else {
            throw new IllegalStateException(
                String.format("The field '%s' on class '%s' is not bound", field.getName(), clazz.getName()));
          }
          // TODO: handle choice
          return retval;
        })
        .filter(Objects::nonNull)
        .map(ObjectUtils::notNull));
  }

  @Override
  public Collection<IBoundNamedModelInstance> getModelInstances() {
    return ObjectUtils.notNull(getNamedModelInstanceMap().values());
  }

  @Override
  public Map<String, IBoundNamedModelInstance> getNamedModelInstanceMap() {
    return modelInstances;
  }

  @Override
  public Map<String, IBoundFieldInstance> getFieldInstanceMap() {
    return fieldInstances;
  }

  @Override
  public Map<String, IBoundAssemblyInstance> getAssemblyInstanceMap() {
    return assemblyInstances;
  }

  @Override
  public List<IChoiceInstance> getChoiceInstances() {
    // choices are not exposed by this API
    return CollectionUtil.emptyList();
  }
}
