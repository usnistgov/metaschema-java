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

package gov.nist.secauto.metaschema.model.xml.constraint;

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
import gov.nist.secauto.metaschema.model.xmlbeans.xml.DefineAssemblyConstraintsType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.HasCardinalityConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedAllowedValuesType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedExpectConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedIndexConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedIndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedMatchesConstraintType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AssemblyConstraintSupport implements IAssemblyConstraintSupport {
  private static final String PATH = "declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
      + "$this/m:allowed-values|$this/m:index|$this/m:index-has-key|$this/m:is-unique|"
      + "$this/m:has-cardinality|$this/m:matches|$this/m:expect";
  private final List<AbstractConstraint> constraints;
  private final List<DefaultAllowedValuesConstraint> allowedValuesConstraints;
  private final List<DefaultMatchesConstraint> matchesConstraints;
  private final List<DefaultIndexHasKeyConstraint> indexHasKeyConstraints;
  private final List<DefaultExpectConstraint> expectConstraints;
  private final List<DefaultIndexConstraint> indexConstraints;
  private final List<DefaultUniqueConstraint> uniqueConstraints;
  private final List<DefaultCardinalityConstraint> cardinalityConstraints;

  public AssemblyConstraintSupport(DefineAssemblyConstraintsType xmlConstraints) {
    XmlCursor cursor = xmlConstraints.newCursor();
    cursor.selectPath(PATH);

    List<AbstractConstraint> constraints = new LinkedList<>();
    List<DefaultAllowedValuesConstraint> allowedValuesConstraints = new LinkedList<>();
    List<DefaultMatchesConstraint> matchesConstraints = new LinkedList<>();
    List<DefaultIndexHasKeyConstraint> indexHasKeyConstraints = new LinkedList<>();
    List<DefaultExpectConstraint> expectConstraints = new LinkedList<>();
    List<DefaultIndexConstraint> indexConstraints = new LinkedList<>();
    List<DefaultUniqueConstraint> uniqueConstraints = new LinkedList<>();
    List<DefaultCardinalityConstraint> cardinalityConstraints = new LinkedList<>();

    while (cursor.toNextSelection()) {
      XmlObject obj = cursor.getObject();
      if (obj instanceof ScopedAllowedValuesType) {
        DefaultAllowedValuesConstraint constraint
            = ConstraintFactory.newAllowedValuesConstraint((ScopedAllowedValuesType) obj);
        constraints.add(constraint);
        allowedValuesConstraints.add(constraint);
      } else if (obj instanceof ScopedIndexConstraintType) {
        DefaultIndexConstraint constraint = ConstraintFactory.newIndexConstraint((ScopedIndexConstraintType) obj);
        constraints.add(constraint);
        indexConstraints.add(constraint);
      } else if (obj instanceof ScopedIndexHasKeyConstraintType) {
        DefaultIndexHasKeyConstraint constraint
            = ConstraintFactory.newIndexHasKeyConstraint((ScopedIndexHasKeyConstraintType) obj);
        constraints.add(constraint);
        indexHasKeyConstraints.add(constraint);
      } else if (obj instanceof ScopedKeyConstraintType) {
        DefaultUniqueConstraint constraint = ConstraintFactory.newUniqueConstraint((ScopedKeyConstraintType) obj);
        constraints.add(constraint);
        uniqueConstraints.add(constraint);
      } else if (obj instanceof HasCardinalityConstraintType) {
        DefaultCardinalityConstraint constraint = ConstraintFactory.newCardinalityConstraint((HasCardinalityConstraintType) obj);
        constraints.add(constraint);
        cardinalityConstraints.add(constraint);
      } else if (obj instanceof ScopedMatchesConstraintType) {
        DefaultMatchesConstraint constraint = ConstraintFactory.newMatchesConstraint((ScopedMatchesConstraintType) obj);
        constraints.add(constraint);
        matchesConstraints.add(constraint);
      } else if (obj instanceof ScopedExpectConstraintType) {
        DefaultExpectConstraint constraint = ConstraintFactory.newExpectConstraint((ScopedExpectConstraintType) obj);
        constraints.add(constraint);
        expectConstraints.add(constraint);
      }
    }
    this.constraints = constraints.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(constraints);
    this.allowedValuesConstraints = allowedValuesConstraints.isEmpty() ? Collections.emptyList()
        : Collections.unmodifiableList(allowedValuesConstraints);
    this.matchesConstraints
        = matchesConstraints.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(matchesConstraints);
    this.indexHasKeyConstraints = indexHasKeyConstraints.isEmpty() ? Collections.emptyList()
        : Collections.unmodifiableList(indexHasKeyConstraints);
    this.expectConstraints
        = expectConstraints.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(expectConstraints);
    this.indexConstraints
        = indexConstraints.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(indexConstraints);
    this.uniqueConstraints
        = uniqueConstraints.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(uniqueConstraints);
    this.cardinalityConstraints = cardinalityConstraints.isEmpty() ? Collections.emptyList()
        : Collections.unmodifiableList(cardinalityConstraints);
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