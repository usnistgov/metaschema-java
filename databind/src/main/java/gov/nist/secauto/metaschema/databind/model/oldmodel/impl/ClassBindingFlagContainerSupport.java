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

import gov.nist.secauto.metaschema.core.model.IFlagContainerSupport;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.Ignore;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundFlagInstance;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class ClassBindingFlagContainerSupport implements IFlagContainerSupport<IBoundFlagInstance> {
  @NonNull
  private final Map<String, IBoundFlagInstance> flagInstances;
  @Nullable
  private IBoundFlagInstance jsonKeyFlag;

  @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
  public ClassBindingFlagContainerSupport(
      @NonNull IClassBinding classBinding,
      @Nullable Consumer<IBoundFlagInstance> peeker) {
    Class<?> clazz = classBinding.getBoundClass();

    Stream<IBoundFlagInstance> instances = getFlagInstanceFields(clazz).stream()
        .flatMap(field -> {
          Stream<IBoundFlagInstance> stream;
          if (field.isAnnotationPresent(BoundFlag.class)) {
            stream = Stream.of(new DefaultFlagProperty(field, classBinding));
          } else {
            stream = Stream.empty();
          }
          return stream;
        });

    Consumer<IBoundFlagInstance> intermediate = this::handle;

    if (peeker != null) {
      intermediate = intermediate.andThen(peeker);
    }

    this.flagInstances = ObjectUtils.notNull(instances
        .peek(intermediate)
        .collect(Collectors.toUnmodifiableMap(
            IBoundFlagInstance::getEffectiveName,
            Function.identity())));
  }

  private void handle(IBoundFlagInstance instance) {
    if (instance.isJsonKey()) {
      this.jsonKeyFlag = instance;
    }
  }

  /**
   * Collect all fields that are flag instances on this class.
   *
   * @param clazz
   *          the class
   * @return an immutable collection of flag instances
   */
  @SuppressWarnings("PMD.UseArraysAsList")
  @NonNull
  protected static Collection<Field> getFlagInstanceFields(Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();

    List<Field> retval = new LinkedList<>();

    Class<?> superClass = clazz.getSuperclass();
    if (superClass != null) {
      // get flags from superclass
      retval.addAll(getFlagInstanceFields(superClass));
    }

    for (Field field : fields) {
      if (!field.isAnnotationPresent(BoundFlag.class)) {
        // skip non-flag fields
        continue;
      }

      if (field.isAnnotationPresent(Ignore.class)) {
        // skip this field, since it is ignored
        continue;
      }

      retval.add(field);
    }
    return ObjectUtils.notNull(Collections.unmodifiableCollection(retval));
  }

  /**
   * Used to delegate flag instance initialization to subclasses.
   *
   * @param instance
   *          the flag instance to process
   */
  protected void handleFlagInstance(IBoundFlagInstance instance) {
    if (instance.isJsonKey()) {
      this.jsonKeyFlag = instance;
    }
  }

  @Override
  @NonNull
  public Map<String, IBoundFlagInstance> getFlagInstanceMap() {
    return flagInstances;
  }

  @Override
  public IBoundFlagInstance getJsonKeyFlagInstance() {
    return jsonKeyFlag;
  }
}
