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

package gov.nist.secauto.metaschema.model.common.constraint;

import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DefaultConstraintSet implements IConstraintSet {
  @NonNull
  private final URI resourceLocation;
  @NonNull
  private final Set<IConstraintSet> importedConstraintSets;
  @NonNull
  private final Map<QName, List<IScopedContraints>> scopedContraints;

  /**
   * Construct a new constraint set.
   * 
   * @param resourceLocation
   *          the resource the constraint was provided from
   * @param scopedContraints
   *          a set of constraints qualified by a scope path
   * @param importedConstraintSets
   *          constraint sets imported by this constraint set
   */
  @SuppressWarnings("null")
  public DefaultConstraintSet(
      @NonNull URI resourceLocation,
      @NonNull List<IScopedContraints> scopedContraints,
      @NonNull Set<IConstraintSet> importedConstraintSets) {
    this.resourceLocation = resourceLocation;
    this.scopedContraints = scopedContraints.stream()
        .collect(
            Collectors.collectingAndThen(
                Collectors.groupingBy(
                    scope -> new QName(scope.getMetaschemaNamespace().toString(), scope.getMetaschemaShortName()),
                    Collectors.toUnmodifiableList()),
                Collections::unmodifiableMap));
    this.importedConstraintSets = CollectionUtil.unmodifiableSet(importedConstraintSets);
  }

  /**
   * Get the resource the constraint was provided from.
   * 
   * @return the resource
   */
  @NonNull
  protected URI getResourceLocation() {
    return resourceLocation;
  }

  @Override
  public Map<QName, List<IScopedContraints>> getScopedContraints() {
    return scopedContraints;
  }

  @Override
  public Set<IConstraintSet> getImportedConstraintSets() {
    return importedConstraintSets;
  }

  @Override
  public Stream<ITargetedConstaints> getTargetedConstraintsForMetaschema(@NonNull IMetaschema metaschema) {
    QName metaschemaQName = metaschema.getQName();

    Map<QName, List<IScopedContraints>> map = getScopedContraints();
    List<IScopedContraints> scopes = map.getOrDefault(metaschemaQName, CollectionUtil.emptyList());
    return ObjectUtils.notNull(scopes.stream()
        .flatMap(scoped -> scoped.getTargetedContraints().stream()));
  }

}
