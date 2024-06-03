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

package gov.nist.secauto.metaschema.core.model.constraint;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ValueConstraintSet implements IValueConstrained { // NOPMD - intentional
  @SuppressWarnings("PMD.UseConcurrentHashMap") // need ordering
  @NonNull
  private final Map<QName, ILet> lets = new LinkedHashMap<>();
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

  @Override
  public Map<QName, ILet> getLetExpressions() {
    return lets;
  }

  @Override
  public ILet addLetExpression(ILet let) {
    return lets.put(let.getName(), let);
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
