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

import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundAssemblyInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundChoiceGroupInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldInstance;
import gov.nist.secauto.metaschema.databind.model.IFeatureCollectionModelInstance;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.Ignore;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

class AssemblyModelContainerSupport
    extends AbstractModelContainerSupport<IFeatureCollectionModelInstance> {
  @NonNull
  private final List<IBoundChoiceGroupInstance> choiceGroupInstances;

  public AssemblyModelContainerSupport(
      @NonNull IAssemblyClassBinding containingDefinition) {
    super(getModelInstanceStream(containingDefinition, containingDefinition.getBoundClass()));

    this.choiceGroupInstances = CollectionUtil.unmodifiableList(ObjectUtils.notNull(
        getModelInstances().stream()
            .filter(instance -> instance instanceof IBoundChoiceGroupInstance)
            .map(instance -> (IBoundChoiceGroupInstance) instance)
            .map(ObjectUtils::notNull)
            .collect(Collectors.toUnmodifiableList())));
  }

  protected static IFeatureCollectionModelInstance newBoundModelInstance(
      @NonNull Field field,
      @NonNull IAssemblyClassBinding classBinding) {
    IFeatureCollectionModelInstance retval = null;
    if (field.isAnnotationPresent(BoundAssembly.class)) {
      retval = IBoundAssemblyInstance.newInstance(field, classBinding);
    } else if (field.isAnnotationPresent(BoundField.class)) {
      retval = IBoundFieldInstance.newInstance(field, classBinding);
    } else if (field.isAnnotationPresent(BoundChoiceGroup.class)) {
      retval = IBoundChoiceGroupInstance.newInstance(field, classBinding);
    }
    return retval;
  }

  @NonNull
  protected static Stream<IFeatureCollectionModelInstance> getModelInstanceStream(
      @NonNull IAssemblyClassBinding classBinding,
      @NonNull Class<?> clazz) {

    Stream<IFeatureCollectionModelInstance> superInstances;
    Class<?> superClass = clazz.getSuperclass();
    if (superClass == null) {
      superInstances = Stream.empty();
    } else {
      // get instances from superclass
      superInstances = getModelInstanceStream(classBinding, superClass);
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

          IFeatureCollectionModelInstance retval = newBoundModelInstance(field, classBinding);
          if (retval == null) {
            throw new IllegalStateException(
                String.format("The field '%s' on class '%s' is not bound", field.getName(), clazz.getName()));
          }
          return retval;
        })
        .filter(Objects::nonNull)));
  }

  @Override
  public List<IBoundChoiceGroupInstance> getChoiceGroupInstances() {
    return choiceGroupInstances;
  }
}
