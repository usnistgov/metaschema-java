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
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelField;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelNamed;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractModelContainerSupport<
    MI extends IBoundInstanceModel,
    NMI extends IBoundInstanceModelNamed,
    FI extends IBoundInstanceModelField,
    AI extends IBoundInstanceModelAssembly,
    CGI extends IBoundInstanceModelChoiceGroup>
    implements IBoundDefinitionModelContainerSupport<MI, NMI, FI, AI, CGI> {
  @NonNull
  private final List<MI> modelInstances;
  @NonNull
  private final Map<String, NMI> namedModelInstances;
  @NonNull
  private final Map<String, FI> fieldInstances;
  @NonNull
  private final Map<String, AI> assemblyInstances;

  public AbstractModelContainerSupport(
      @NonNull Stream<? extends MI> instances,
      @NonNull Class<? extends NMI> namedModelInstanceClass,
      @NonNull Class<? extends FI> fieldInstanceClass,
      @NonNull Class<? extends AI> assemblyInstanceClass) {
    this.modelInstances = CollectionUtil.unmodifiableList(ObjectUtils.notNull(instances
        .collect(Collectors.toUnmodifiableList())));

    this.namedModelInstances = CollectionUtil.unmodifiableMap(ObjectUtils.notNull(
        getModelInstances().stream()
            .filter(instance -> namedModelInstanceClass.isInstance(instance))
            .map(instance -> namedModelInstanceClass.cast(instance))
            .map(ObjectUtils::notNull)
            .collect(Collectors.toMap(
                instance -> instance.getEffectiveName(),
                Function.identity(),
                CustomCollectors.useLastMapper(),
                LinkedHashMap::new))));

    this.fieldInstances = CollectionUtil.unmodifiableMap(ObjectUtils.notNull(
        getNamedModelInstanceMap().values().stream()
            .filter(instance -> fieldInstanceClass.isInstance(instance))
            .map(instance -> fieldInstanceClass.cast(instance))
            .map(ObjectUtils::notNull)
            .collect(Collectors.toMap(
                instance -> instance.getEffectiveName(),
                Function.identity(),
                CustomCollectors.useLastMapper(),
                LinkedHashMap::new))));

    this.assemblyInstances = CollectionUtil.unmodifiableMap(ObjectUtils.notNull(
        getNamedModelInstanceMap().values().stream()
            .filter(instance -> assemblyInstanceClass.isInstance(instance))
            .map(instance -> assemblyInstanceClass.cast(instance))
            .map(ObjectUtils::notNull)
            .collect(Collectors.toMap(
                instance -> instance.getEffectiveName(),
                Function.identity(),
                CustomCollectors.useLastMapper(),
                LinkedHashMap::new))));
  }

  @Override
  public Collection<MI> getModelInstances() {
    return modelInstances;
  }

  @Override
  public Map<String, NMI> getNamedModelInstanceMap() {
    return namedModelInstances;
  }

  @Override
  public Map<String, FI> getFieldInstanceMap() {
    return fieldInstances;
  }

  @Override
  public Map<String, AI> getAssemblyInstanceMap() {
    return assemblyInstances;
  }

  @Override
  public List<CGI> getChoiceGroupInstances() {
    return CollectionUtil.emptyList();
  }
}
