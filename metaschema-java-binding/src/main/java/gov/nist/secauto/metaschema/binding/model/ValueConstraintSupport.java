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

package gov.nist.secauto.metaschema.binding.model;

import gov.nist.secauto.metaschema.binding.model.annotations.BoundField;
import gov.nist.secauto.metaschema.binding.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IValueConstraintSupport;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Support for constraints on valued objects (i.e., fields and flags).
 */
class ValueConstraintSupport implements IValueConstraintSupport {
  @NotNull
  private final List<@NotNull IConstraint> constraints;
  @NotNull
  private final List<@NotNull IAllowedValuesConstraint> allowedValuesConstraints;
  @NotNull
  private final List<@NotNull IMatchesConstraint> matchesConstraints;
  @NotNull
  private final List<@NotNull IIndexHasKeyConstraint> indexHasKeyConstraints;
  @NotNull
  private final List<@NotNull IExpectConstraint> expectConstraints;

  public ValueConstraintSupport() {
    this.constraints = new LinkedList<>();
    this.allowedValuesConstraints = new LinkedList<>();
    this.matchesConstraints = new LinkedList<>();
    this.indexHasKeyConstraints = new LinkedList<>();
    this.expectConstraints = new LinkedList<>();
  }

  /**
   * Generate constraints from a {@link BoundFlag} annotation.
   * 
   * @param propertyAnnotation
   *          the annotation where the constraints are defined
   */
  @SuppressWarnings("null")
  public ValueConstraintSupport(@NotNull BoundFlag propertyAnnotation) { // NOPMD - intentional
    allowedValuesConstraints = Arrays.stream(propertyAnnotation.allowedValues())
        .map(annotation -> ConstraintFactory.newAllowedValuesConstraint(annotation))
        .collect(Collectors.toCollection(LinkedList::new));

    matchesConstraints = Arrays.stream(propertyAnnotation.matches())
        .map(annotation -> ConstraintFactory.newMatchesConstraint(annotation))
        .collect(Collectors.toCollection(LinkedList::new));

    indexHasKeyConstraints = Arrays.stream(propertyAnnotation.indexHasKey())
        .map(annotation -> ConstraintFactory.newIndexHasKeyConstraint(annotation))
        .collect(Collectors.toCollection(LinkedList::new));

    expectConstraints = Arrays.stream(propertyAnnotation.expect())
        .map(annotation -> ConstraintFactory.newExpectConstraint(annotation))
        .collect(Collectors.toCollection(LinkedList::new));

    constraints = new LinkedList<>();
    constraints.addAll(allowedValuesConstraints);
    constraints.addAll(matchesConstraints);
    constraints.addAll(indexHasKeyConstraints);
    constraints.addAll(expectConstraints);
  }

  /**
   * Generate constraints from a {@link BoundField} annotation.
   * 
   * @param propertyAnnotation
   *          the annotation where the constraints are defined
   */
  @SuppressWarnings("null")
  public ValueConstraintSupport(@NotNull BoundField propertyAnnotation) { // NOPMD - intentional
    allowedValuesConstraints = Arrays.stream(propertyAnnotation.allowedValues())
        .map(annotation -> ConstraintFactory.newAllowedValuesConstraint(annotation))
        .collect(Collectors.toCollection(LinkedList::new));

    matchesConstraints = Arrays.stream(propertyAnnotation.matches())
        .map(annotation -> ConstraintFactory.newMatchesConstraint(annotation))
        .collect(Collectors.toCollection(LinkedList::new));

    indexHasKeyConstraints = Arrays.stream(propertyAnnotation.indexHasKey())
        .map(annotation -> ConstraintFactory.newIndexHasKeyConstraint(annotation))
        .collect(Collectors.toCollection(LinkedList::new));

    expectConstraints = Arrays.stream(propertyAnnotation.expect())
        .map(annotation -> ConstraintFactory.newExpectConstraint(annotation))
        .collect(Collectors.toCollection(LinkedList::new));

    constraints = new LinkedList<>();
    constraints.addAll(allowedValuesConstraints);
    constraints.addAll(matchesConstraints);
    constraints.addAll(indexHasKeyConstraints);
    constraints.addAll(expectConstraints);
  }

  /**
   * Generate constraints from a {@link MetaschemaField} annotation.
   * 
   * @param classAnnotation
   *          the annotation where the constraints are defined
   */
  @SuppressWarnings("null")
  public ValueConstraintSupport(@NotNull MetaschemaField classAnnotation) { // NOPMD - intentional
    allowedValuesConstraints = Arrays.stream(classAnnotation.allowedValues())
        .map(annotation -> ConstraintFactory.newAllowedValuesConstraint(annotation))
        .collect(Collectors.toCollection(LinkedList::new));

    matchesConstraints = Arrays.stream(classAnnotation.matches())
        .map(annotation -> ConstraintFactory.newMatchesConstraint(annotation))
        .collect(Collectors.toCollection(LinkedList::new));

    indexHasKeyConstraints = Arrays.stream(classAnnotation.indexHasKey())
        .map(annotation -> ConstraintFactory.newIndexHasKeyConstraint(annotation))
        .collect(Collectors.toCollection(LinkedList::new));

    expectConstraints = Arrays.stream(classAnnotation.expect())
        .map(annotation -> ConstraintFactory.newExpectConstraint(annotation))
        .collect(Collectors.toCollection(LinkedList::new));

    constraints = new LinkedList<>();
    constraints.addAll(allowedValuesConstraints);
    constraints.addAll(matchesConstraints);
    constraints.addAll(indexHasKeyConstraints);
    constraints.addAll(expectConstraints);
  }

  @Override
  public List<@NotNull IConstraint> getConstraints() {
    return constraints;
  }

  @Override
  public List<@NotNull IAllowedValuesConstraint> getAllowedValuesConstraints() {
    return allowedValuesConstraints;
  }

  @Override
  public List<@NotNull IMatchesConstraint> getMatchesConstraints() {
    return matchesConstraints;
  }

  @Override
  public List<@NotNull IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    return indexHasKeyConstraints;
  }

  @Override
  public List<@NotNull IExpectConstraint> getExpectConstraints() {
    return expectConstraints;
  }

  @Override
  public void addConstraint(@NotNull IAllowedValuesConstraint constraint) {
    constraints.add(constraint);
    allowedValuesConstraints.add(constraint);
  }

  @Override
  public void addConstraint(@NotNull IMatchesConstraint constraint) {
    constraints.add(constraint);
    matchesConstraints.add(constraint);
  }

  @Override
  public void addConstraint(@NotNull IIndexHasKeyConstraint constraint) {
    constraints.add(constraint);
    indexHasKeyConstraints.add(constraint);
  }

  @Override
  public void addConstraint(@NotNull IExpectConstraint constraint) {
    constraints.add(constraint);
    expectConstraints.add(constraint);
  }
}
