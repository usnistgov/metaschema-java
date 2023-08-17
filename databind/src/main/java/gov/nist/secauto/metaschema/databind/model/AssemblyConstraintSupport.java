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

import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.model.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.databind.model.annotations.AssemblyConstraints;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Support for constraints on Metaschema assembly bound objects.
 */
class AssemblyConstraintSupport
    extends ValueConstraintSupport
    implements IModelConstrained {
  @NonNull
  private final List<IIndexConstraint> indexConstraints;
  @NonNull
  private final List<IUniqueConstraint> uniqueConstraints;
  @NonNull
  private final List<ICardinalityConstraint> cardinalityConstraints;

  public AssemblyConstraintSupport() {
    this.indexConstraints = new LinkedList<>();
    this.uniqueConstraints = new LinkedList<>();
    this.cardinalityConstraints = new LinkedList<>();
  }

  @SuppressWarnings("null")
  public AssemblyConstraintSupport(
      @Nullable ValueConstraints valueAnnotation,
      @Nullable AssemblyConstraints assemblyAnnotation,
      @NonNull ISource source) {
    super(valueAnnotation, source);
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
    getConstraints().addAll(indexConstraints);
    getConstraints().addAll(uniqueConstraints);
    getConstraints().addAll(cardinalityConstraints);
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
  public void addConstraint(@NonNull IIndexConstraint constraint) {
    getConstraints().add(constraint);
    indexConstraints.add(constraint);
  }

  @Override
  public void addConstraint(@NonNull IUniqueConstraint constraint) {
    getConstraints().add(constraint);
    uniqueConstraints.add(constraint);
  }

  @Override
  public void addConstraint(@NonNull ICardinalityConstraint constraint) {
    getConstraints().add(constraint);
    cardinalityConstraints.add(constraint);
  }
}
