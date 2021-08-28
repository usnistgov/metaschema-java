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

package gov.nist.secauto.metaschema.binding.model.constraint;

import gov.nist.secauto.metaschema.binding.model.DefaultAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.AllowedValues;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Expect;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.HasCardinality;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Index;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.IndexHasKey;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.IsUnique;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Matches;
import gov.nist.secauto.metaschema.model.common.constraint.AbstractConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultCardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IAssemblyConstraintSupport;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AssemblyConstraintSupport implements IAssemblyConstraintSupport {
  private final List<AbstractConstraint> constraints;
  private final List<DefaultAllowedValuesConstraint> allowedValuesConstraints;
  private final List<DefaultMatchesConstraint> matchesConstraints;
  private final List<DefaultIndexHasKeyConstraint> indexHasKeyConstraints;
  private final List<DefaultExpectConstraint> expectConstraints;
  private final List<DefaultIndexConstraint> indexConstraints;
  private final List<DefaultUniqueConstraint> uniqueConstraints;
  private final List<DefaultCardinalityConstraint> cardinalityConstraints;

  public AssemblyConstraintSupport(DefaultAssemblyClassBinding classBinding) {
    MetaschemaAssembly classAnnotation = classBinding.getMetaschemaAssemblyAnnotation();
    List<AbstractConstraint> constraints = new LinkedList<>();

    List<DefaultAllowedValuesConstraint> allowedValuesConstraints
        = new ArrayList<>(classAnnotation.allowedValues().length);
    for (AllowedValues annotation : classAnnotation.allowedValues()) {
      DefaultAllowedValuesConstraint constraint = ConstraintFactory.newAllowedValuesConstraint(annotation);
      allowedValuesConstraints.add(constraint);
      constraints.add(constraint);
    }
    this.allowedValuesConstraints = allowedValuesConstraints.isEmpty() ? Collections.emptyList()
        : Collections.unmodifiableList(allowedValuesConstraints);

    List<DefaultMatchesConstraint> matchesConstraints = new ArrayList<>(classAnnotation.matches().length);
    for (Matches annotation : classAnnotation.matches()) {
      DefaultMatchesConstraint constraint = ConstraintFactory.newMatchesConstraint(annotation);
      matchesConstraints.add(constraint);
      constraints.add(constraint);
    }
    this.matchesConstraints
        = matchesConstraints.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(matchesConstraints);

    List<DefaultUniqueConstraint> uniqueConstraints = new ArrayList<>(classAnnotation.isUnique().length);
    for (IsUnique annotation : classAnnotation.isUnique()) {
      DefaultUniqueConstraint constraint = ConstraintFactory.newUniqueConstraint(annotation);
      uniqueConstraints.add(constraint);
      constraints.add(constraint);
    }
    this.uniqueConstraints = uniqueConstraints.isEmpty() ? Collections.emptyList()
        : Collections.unmodifiableList(uniqueConstraints);

    List<DefaultIndexConstraint> indexConstraints = new ArrayList<>(classAnnotation.index().length);
    for (Index annotation : classAnnotation.index()) {
      DefaultIndexConstraint constraint = ConstraintFactory.newIndexConstraint(annotation);
      indexConstraints.add(constraint);
      constraints.add(constraint);
    }
    this.indexConstraints = indexConstraints.isEmpty() ? Collections.emptyList()
        : Collections.unmodifiableList(indexConstraints);

    List<DefaultIndexHasKeyConstraint> indexHasKeyConstraints = new ArrayList<>(classAnnotation.indexHasKey().length);
    for (IndexHasKey annotation : classAnnotation.indexHasKey()) {
      DefaultIndexHasKeyConstraint constraint = ConstraintFactory.newIndexHasKeyConstraint(annotation);
      indexHasKeyConstraints.add(constraint);
      constraints.add(constraint);
    }
    this.indexHasKeyConstraints = indexHasKeyConstraints.isEmpty() ? Collections.emptyList()
        : Collections.unmodifiableList(indexHasKeyConstraints);

    List<DefaultExpectConstraint> expectConstraints = new ArrayList<>(classAnnotation.expect().length);
    for (Expect annotation : classAnnotation.expect()) {
      DefaultExpectConstraint constraint = ConstraintFactory.newExpectConstraint(annotation);
      expectConstraints.add(constraint);
      constraints.add(constraint);
    }
    this.expectConstraints
        = expectConstraints.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(expectConstraints);

    List<DefaultCardinalityConstraint> cardinalityConstraints
        = new ArrayList<>(classAnnotation.hasCardinality().length);
    for (HasCardinality annotation : classAnnotation.hasCardinality()) {
      DefaultCardinalityConstraint constraint = ConstraintFactory.newCardinalityConstraint(annotation);
      cardinalityConstraints.add(constraint);
      constraints.add(constraint);
    }
    this.cardinalityConstraints
        = cardinalityConstraints.isEmpty() ? Collections.emptyList()
            : Collections.unmodifiableList(cardinalityConstraints);

    this.constraints = constraints.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(constraints);
  }

  @Override
  public List<? extends IConstraint> getConstraints() {
    return constraints;
  }

  @Override
  public List<DefaultAllowedValuesConstraint> getAllowedValuesContraints() {
    return allowedValuesConstraints;
  }

  @Override
  public List<DefaultMatchesConstraint> getMatchesConstraints() {
    return matchesConstraints;
  }

  @Override
  public List<DefaultIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    return indexHasKeyConstraints;
  }

  @Override
  public List<DefaultExpectConstraint> getExpectConstraints() {
    return expectConstraints;
  }

  @Override
  public List<DefaultIndexConstraint> getIndexContraints() {
    return indexConstraints;
  }

  @Override
  public List<DefaultUniqueConstraint> getUniqueConstraints() {
    return uniqueConstraints;
  }

  @Override
  public List<DefaultCardinalityConstraint> getHasCardinalityConstraints() {
    return cardinalityConstraints;
  }
}
