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
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
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
import org.apache.xmlbeans.impl.values.XmlValueNotSupportedException;

import java.util.LinkedList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

class ValueConstraintSupport implements IValueConstraintSupport { // NOPMD - intentional
  private static final String PATH = "declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
      + "$this/m:allowed-values|$this/m:matches|$this/m:index-has-key|$this/m:expect";

  @NonNull
  private final List<IConstraint> constraints = new LinkedList<>();
  @NonNull
  private final List<IAllowedValuesConstraint> allowedValuesConstraints = new LinkedList<>();
  @NonNull
  private final List<IMatchesConstraint> matchesConstraints = new LinkedList<>();
  @NonNull
  private final List<IIndexHasKeyConstraint> indexHasKeyConstraints = new LinkedList<>();
  @NonNull
  private final List<IExpectConstraint> expectConstraints = new LinkedList<>();

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
      @NonNull DefineFlagConstraintsType xmlConstraints,
      @NonNull ISource source) {
    try (XmlCursor cursor = xmlConstraints.newCursor()) {
      cursor.selectPath(PATH);
      while (cursor.toNextSelection()) {
        XmlObject obj = cursor.getObject();
        if (obj instanceof AllowedValuesType) {
          DefaultAllowedValuesConstraint constraint
              = ModelFactory.newAllowedValuesConstraint((AllowedValuesType) obj, source);
          addConstraint(constraint);
        } else if (obj instanceof MatchesConstraintType) {
          DefaultMatchesConstraint constraint
              = ModelFactory.newMatchesConstraint((MatchesConstraintType) obj, source);
          addConstraint(constraint);
        } else if (obj instanceof IndexHasKeyConstraintType) {
          DefaultIndexHasKeyConstraint constraint
              = ModelFactory.newIndexHasKeyConstraint((IndexHasKeyConstraintType) obj, source);
          addConstraint(constraint);
        } else if (obj instanceof ExpectConstraintType) {
          DefaultExpectConstraint constraint = ModelFactory.newExpectConstraint((ExpectConstraintType) obj, source);
          addConstraint(constraint);
        }
      }
    } catch (MetapathException | XmlValueNotSupportedException ex) {
      if (ex.getCause() instanceof MetapathException) {
        throw new MetapathException(
            String.format("Unable to compile a Metapath in '%s'. %s",
                source.getSource(),
                ex.getLocalizedMessage()),
            ex);
      }
      throw ex;
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
      @NonNull DefineFieldConstraintsType xmlConstraints,
      @NonNull ISource source) {
    try (XmlCursor cursor = xmlConstraints.newCursor()) {
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
          + "$this/m:allowed-values|$this/m:matches|$this/m:index-has-key|$this/m:expect");

      while (cursor.toNextSelection()) {
        XmlObject obj = cursor.getObject();
        if (obj instanceof ScopedAllowedValuesType) {
          DefaultAllowedValuesConstraint constraint
              = ModelFactory.newAllowedValuesConstraint((ScopedAllowedValuesType) obj, source);
          addConstraint(constraint);
        } else if (obj instanceof ScopedMatchesConstraintType) {
          DefaultMatchesConstraint constraint
              = ModelFactory.newMatchesConstraint((ScopedMatchesConstraintType) obj, source);
          addConstraint(constraint);
        } else if (obj instanceof ScopedIndexHasKeyConstraintType) {
          DefaultIndexHasKeyConstraint constraint
              = ModelFactory.newIndexHasKeyConstraint((ScopedIndexHasKeyConstraintType) obj, source);
          addConstraint(constraint);
        } else if (obj instanceof ScopedExpectConstraintType) {
          DefaultExpectConstraint constraint
              = ModelFactory.newExpectConstraint((ScopedExpectConstraintType) obj, source);
          addConstraint(constraint);
        }
      }
    } catch (MetapathException | XmlValueNotSupportedException ex) {
      if (ex.getCause() instanceof MetapathException) {
        throw new MetapathException(
            String.format("Unable to compile a Metapath in '%s'. %s",
                source.getSource(),
                ex.getLocalizedMessage()),
            ex);
      }
      throw ex;
    }
  }

  @Override
  public List<IConstraint> getConstraints() {
    synchronized (this) {
      return constraints;
    }
  }

  @Override
  public List<IAllowedValuesConstraint> getAllowedValuesConstraints() {
    synchronized (this) {
      return allowedValuesConstraints;
    }
  }

  @Override
  public List<IMatchesConstraint> getMatchesConstraints() {
    synchronized (this) {
      return matchesConstraints;
    }
  }

  @Override
  public List<IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    synchronized (this) {
      return indexHasKeyConstraints;
    }
  }

  @Override
  public List<IExpectConstraint> getExpectConstraints() {
    synchronized (this) {
      return expectConstraints;
    }
  }

  @Override
  public final void addConstraint(@NonNull IAllowedValuesConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      allowedValuesConstraints.add(constraint);
    }
  }

  @Override
  public final void addConstraint(@NonNull IMatchesConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      matchesConstraints.add(constraint);
    }
  }

  @Override
  public final void addConstraint(@NonNull IIndexHasKeyConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      indexHasKeyConstraints.add(constraint);
    }
  }

  @Override
  public final void addConstraint(@NonNull IExpectConstraint constraint) {
    synchronized (this) {
      constraints.add(constraint);
      expectConstraints.add(constraint);
    }
  }
}
