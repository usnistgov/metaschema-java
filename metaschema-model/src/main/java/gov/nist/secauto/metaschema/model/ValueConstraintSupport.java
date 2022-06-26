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

package gov.nist.secauto.metaschema.model;

import gov.nist.secauto.metaschema.model.common.constraint.DefaultAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.ISource;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IValueConstraintSupport;
import gov.nist.secauto.metaschema.model.xmlbeans.AllowedValuesType;
import gov.nist.secauto.metaschema.model.xmlbeans.DefineFieldConstraintsType;
import gov.nist.secauto.metaschema.model.xmlbeans.DefineFlagConstraintsType;
import gov.nist.secauto.metaschema.model.xmlbeans.ExpectConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.IndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.MatchesConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedAllowedValuesType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedExpectConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedIndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedMatchesConstraintType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

class ValueConstraintSupport implements IValueConstraintSupport { // NOPMD - intentional
  private static final String PATH = "declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
      + "$this/m:allowed-values|$this/m:matches|$this/m:index-has-key|$this/m:expect";

  @NotNull
  private final List<@NotNull IConstraint> constraints = new LinkedList<>();
  @NotNull
  private final List<@NotNull IAllowedValuesConstraint> allowedValuesConstraints = new LinkedList<>();
  @NotNull
  private final List<@NotNull IMatchesConstraint> matchesConstraints = new LinkedList<>();
  @NotNull
  private final List<@NotNull IIndexHasKeyConstraint> indexHasKeyConstraints = new LinkedList<>();
  @NotNull
  private final List<@NotNull IExpectConstraint> expectConstraints = new LinkedList<>();

  public ValueConstraintSupport() {
    // do nothing
  }

  /**
   * Generate a set of constraints from the provided XMLBeans instance.
   * 
   * @param xmlConstraints
   *          the XMLBeans instance
   * @param source
   *          information about the source of the constraints
   */
  public ValueConstraintSupport( // NOPMD - intentional
      @NotNull DefineFlagConstraintsType xmlConstraints,
      @NotNull ISource source) {
    XmlCursor cursor = xmlConstraints.newCursor();
    cursor.selectPath(PATH);
    while (cursor.toNextSelection()) {
      XmlObject obj = cursor.getObject();
      if (obj instanceof AllowedValuesType) {
        DefaultAllowedValuesConstraint constraint
            = ConstraintFactory.newAllowedValuesConstraint((AllowedValuesType) obj, source);
        addConstraint(constraint);
      } else if (obj instanceof MatchesConstraintType) {
        DefaultMatchesConstraint constraint
            = ConstraintFactory.newMatchesConstraint((MatchesConstraintType) obj, source);
        addConstraint(constraint);
      } else if (obj instanceof IndexHasKeyConstraintType) {
        DefaultIndexHasKeyConstraint constraint
            = ConstraintFactory.newIndexHasKeyConstraint((IndexHasKeyConstraintType) obj, source);
        addConstraint(constraint);
      } else if (obj instanceof ExpectConstraintType) {
        DefaultExpectConstraint constraint = ConstraintFactory.newExpectConstraint((ExpectConstraintType) obj, source);
        addConstraint(constraint);
      }
    }
  }

  /**
   * Generate a set of constraints from the provided XMLBeans instance.
   * 
   * @param xmlConstraints
   *          the XMLBeans instance
   * @param source
   *          information about the source of the constraints
   */
  public ValueConstraintSupport( // NOPMD - intentional
      @NotNull DefineFieldConstraintsType xmlConstraints,
      @NotNull ISource source) {
    XmlCursor cursor = xmlConstraints.newCursor();
    cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
        + "$this/m:allowed-values|$this/m:matches|$this/m:index-has-key|$this/m:expect");

    while (cursor.toNextSelection()) {
      XmlObject obj = cursor.getObject();
      if (obj instanceof ScopedAllowedValuesType) {
        DefaultAllowedValuesConstraint constraint
            = ConstraintFactory.newAllowedValuesConstraint((ScopedAllowedValuesType) obj, source);
        addConstraint(constraint);
      } else if (obj instanceof ScopedMatchesConstraintType) {
        DefaultMatchesConstraint constraint
            = ConstraintFactory.newMatchesConstraint((ScopedMatchesConstraintType) obj, source);
        addConstraint(constraint);
      } else if (obj instanceof ScopedIndexHasKeyConstraintType) {
        DefaultIndexHasKeyConstraint constraint
            = ConstraintFactory.newIndexHasKeyConstraint((ScopedIndexHasKeyConstraintType) obj, source);
        addConstraint(constraint);
      } else if (obj instanceof ScopedExpectConstraintType) {
        DefaultExpectConstraint constraint
            = ConstraintFactory.newExpectConstraint((ScopedExpectConstraintType) obj, source);
        addConstraint(constraint);
      }
    }
  }

  @Override
  public List<@NotNull IConstraint> getConstraints() {
    synchronized (this) {
      return constraints;
    }
  }

  @Override
  public List<@NotNull IAllowedValuesConstraint> getAllowedValuesConstraints() {
    synchronized (this) {
      return allowedValuesConstraints;
    }
  }

  @Override
  public List<@NotNull IMatchesConstraint> getMatchesConstraints() {
    synchronized (this) {
      return matchesConstraints;
    }
  }

  @Override
  public List<@NotNull IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    synchronized (this) {
      return indexHasKeyConstraints;
    }
  }

  @Override
  public List<@NotNull IExpectConstraint> getExpectConstraints() {
    synchronized (this) {
      return expectConstraints;
    }
  }

  @Override
  public final void addConstraint(@NotNull IAllowedValuesConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      allowedValuesConstraints.add(constraint);
    }
  }

  @Override
  public final void addConstraint(@NotNull IMatchesConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      matchesConstraints.add(constraint);
    }
  }

  @Override
  public final void addConstraint(@NotNull IIndexHasKeyConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      indexHasKeyConstraints.add(constraint);
    }
  }

  @Override
  public final void addConstraint(@NotNull IExpectConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      expectConstraints.add(constraint);
    }
  }
}
