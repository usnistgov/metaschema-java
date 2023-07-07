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

import gov.nist.secauto.metaschema.binding.model.annotations.AssemblyConstraints;
import gov.nist.secauto.metaschema.binding.model.annotations.ValueConstraints;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IAssemblyConstraintSupport;
import gov.nist.secauto.metaschema.model.common.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.ISource;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Support for constraints on Metaschema assembly bound objects.
 */
class AssemblyConstraintSupport implements IAssemblyConstraintSupport {
  @NonNull
  private final List<IConstraint> constraints;
  @NonNull
  private final List<IAllowedValuesConstraint> allowedValuesConstraints;
  @NonNull
  private final List<IMatchesConstraint> matchesConstraints;
  @NonNull
  private final List<IIndexHasKeyConstraint> indexHasKeyConstraints;
  @NonNull
  private final List<IExpectConstraint> expectConstraints;
  @NonNull
  private final List<IIndexConstraint> indexConstraints;
  @NonNull
  private final List<IUniqueConstraint> uniqueConstraints;
  @NonNull
  private final List<ICardinalityConstraint> cardinalityConstraints;

  public AssemblyConstraintSupport() {
    this.constraints = new LinkedList<>();
    this.allowedValuesConstraints = new LinkedList<>();
    this.matchesConstraints = new LinkedList<>();
    this.indexHasKeyConstraints = new LinkedList<>();
    this.expectConstraints = new LinkedList<>();
    this.indexConstraints = new LinkedList<>();
    this.uniqueConstraints = new LinkedList<>();
    this.cardinalityConstraints = new LinkedList<>();
  }

  @SuppressWarnings("null")
  public AssemblyConstraintSupport(
      @Nullable ValueConstraints valueAnnotation,
      @Nullable AssemblyConstraints assemblyAnnotation,
      @NonNull ISource source) {
    if (valueAnnotation == null) {
      this.allowedValuesConstraints = new LinkedList<>();
      this.matchesConstraints = new LinkedList<>();
      this.indexHasKeyConstraints = new LinkedList<>();
      this.expectConstraints = new LinkedList<>();
    } else {
      try {
        allowedValuesConstraints = Arrays.stream(valueAnnotation.allowedValues())
            .map(annotation -> ConstraintFactory.newAllowedValuesConstraint(annotation, source))
            .collect(Collectors.toCollection(LinkedList::new));

        matchesConstraints = Arrays.stream(valueAnnotation.matches())
            .map(annotation -> ConstraintFactory.newMatchesConstraint(annotation, source))
            .collect(Collectors.toCollection(LinkedList::new));

        indexHasKeyConstraints = Arrays.stream(valueAnnotation.indexHasKey())
            .map(annotation -> ConstraintFactory.newIndexHasKeyConstraint(annotation, source))
            .collect(Collectors.toCollection(LinkedList::new));

        expectConstraints = Arrays.stream(valueAnnotation.expect())
            .map(annotation -> ConstraintFactory.newExpectConstraint(annotation, source))
            .collect(Collectors.toCollection(LinkedList::new));
      } catch (MetapathException ex) {
        throw new MetapathException(
            String.format("Unable to compile a Metapath in '%s'. %s", source.getSource(), ex.getLocalizedMessage()),
            ex);
      }
    }

    if (assemblyAnnotation == null) {
      this.indexConstraints = new LinkedList<>();
      this.uniqueConstraints = new LinkedList<>();
      this.cardinalityConstraints = new LinkedList<>();
    } else {
      try {
        indexConstraints = Arrays.stream(assemblyAnnotation.index())
            .map(annotation -> ConstraintFactory.newIndexConstraint(annotation, source))
            .collect(Collectors.toCollection(LinkedList::new));

        uniqueConstraints = Arrays.stream(assemblyAnnotation.isUnique())
            .map(annotation -> ConstraintFactory.newUniqueConstraint(annotation, source))
            .collect(Collectors.toCollection(LinkedList::new));

        cardinalityConstraints = Arrays.stream(assemblyAnnotation.hasCardinality())
            .map(annotation -> ConstraintFactory.newCardinalityConstraint(annotation, source))
            .collect(Collectors.toCollection(LinkedList::new));
      } catch (MetapathException ex) {
        throw new MetapathException(
            String.format("Unable to compile a Metapath in '%s'. %s", source.getSource(), ex.getLocalizedMessage()),
            ex);
      }
    }

    constraints = new LinkedList<>();
    constraints.addAll(allowedValuesConstraints);
    constraints.addAll(matchesConstraints);
    constraints.addAll(indexHasKeyConstraints);
    constraints.addAll(expectConstraints);
    constraints.addAll(indexConstraints);
    constraints.addAll(uniqueConstraints);
    constraints.addAll(cardinalityConstraints);
  }

  @Override
  public List<IConstraint> getConstraints() {
    return constraints;
  }

  @Override
  public List<IAllowedValuesConstraint> getAllowedValuesConstraints() {
    return allowedValuesConstraints;
  }

  @Override
  public List<IMatchesConstraint> getMatchesConstraints() {
    return matchesConstraints;
  }

  @Override
  public List<IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    return indexHasKeyConstraints;
  }

  @Override
  public List<IExpectConstraint> getExpectConstraints() {
    return expectConstraints;
  }

  @Override
  public List<IIndexConstraint> getIndexConstraints() {
    return indexConstraints;
  }

  @Override
  public List<IUniqueConstraint> getUniqueConstraints() {
    return uniqueConstraints;
  }

  @Override
  public List<ICardinalityConstraint> getHasCardinalityConstraints() {
    return cardinalityConstraints;
  }

  @Override
  public void addConstraint(@NonNull IAllowedValuesConstraint constraint) {
    constraints.add(constraint);
    allowedValuesConstraints.add(constraint);
  }

  @Override
  public void addConstraint(@NonNull IMatchesConstraint constraint) {
    constraints.add(constraint);
    matchesConstraints.add(constraint);
  }

  @Override
  public void addConstraint(@NonNull IIndexHasKeyConstraint constraint) {
    constraints.add(constraint);
    indexHasKeyConstraints.add(constraint);
  }

  @Override
  public void addConstraint(@NonNull IExpectConstraint constraint) {
    constraints.add(constraint);
    expectConstraints.add(constraint);
  }

  @Override
  public void addConstraint(@NonNull IIndexConstraint constraint) {
    constraints.add(constraint);
    indexConstraints.add(constraint);
  }

  @Override
  public void addConstraint(@NonNull IUniqueConstraint constraint) {
    constraints.add(constraint);
    uniqueConstraints.add(constraint);
  }

  @Override
  public void addConstraint(@NonNull ICardinalityConstraint constraint) {
    constraints.add(constraint);
    cardinalityConstraints.add(constraint);
  }
}
