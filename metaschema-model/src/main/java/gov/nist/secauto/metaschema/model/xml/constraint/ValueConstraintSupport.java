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

import gov.nist.secauto.metaschema.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.constraint.AbstractConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultKeyField;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.xml.MarkupStringConverter;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.AllowedValuesType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.DefineFieldConstraintsType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.DefineFlagConstraintsType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ExpectConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.IndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.KeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.MatchesConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedAllowedValuesType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedExpectConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedIndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedMatchesConstraintType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ValueConstraintSupport implements IValueConstraintSupport {
  static List<DefaultKeyField> toKeyFields(KeyConstraintType xmlConstraint) {
    List<DefaultKeyField> keyFields = new ArrayList<>(xmlConstraint.sizeOfKeyFieldArray());
    for (KeyConstraintType.KeyField xmlKeyField : xmlConstraint.getKeyFieldList()) {
      DefaultKeyField keyField
          = new DefaultKeyField(xmlKeyField.getTarget(), xmlKeyField.isSetPattern() ? xmlKeyField.getPattern() : null,
              xmlKeyField.isSetRemarks() ? MarkupStringConverter.toMarkupString(xmlKeyField.getRemarks()) : null);
      keyFields.add(keyField);
    }
    return Collections.unmodifiableList(keyFields);
  }

  private static final String PATH = "declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
      + "$this/m:allowed-values|$this/m:matches|$this/m:index-has-key|$this/m:expect";

  private final List<AbstractConstraint> constraints;
  private final List<XmlAllowedValuesConstraint> allowedValuesConstraints;
  private final List<XmlMatchesConstraint> matchesConstraints;
  private final List<XmlIndexHasKeyConstraint> indexHasKeyConstraints;
  private final List<XmlExpectConstraint> expectConstraints;

  public ValueConstraintSupport(DefineFlagConstraintsType xmlConstraints) {
    XmlCursor cursor = xmlConstraints.newCursor();
    cursor.selectPath(PATH);

    List<AbstractConstraint> constraints = new LinkedList<>();
    List<XmlAllowedValuesConstraint> allowedValuesConstraints = new LinkedList<>();
    List<XmlMatchesConstraint> matchesConstraints = new LinkedList<>();
    List<XmlIndexHasKeyConstraint> indexHasKeyConstraints = new LinkedList<>();
    List<XmlExpectConstraint> expectConstraints = new LinkedList<>();
    while (cursor.toNextSelection()) {
      XmlObject obj = cursor.getObject();
      if (obj instanceof AllowedValuesType) {
        XmlAllowedValuesConstraint constraint
            = new XmlAllowedValuesConstraint((AllowedValuesType) obj, MetapathExpression.CONTEXT_NODE);
        constraints.add(constraint);
        allowedValuesConstraints.add(constraint);
      } else if (obj instanceof MatchesConstraintType) {
        XmlMatchesConstraint constraint
            = new XmlMatchesConstraint((MatchesConstraintType) obj, MetapathExpression.CONTEXT_NODE);
        constraints.add(constraint);
        matchesConstraints.add(constraint);
      } else if (obj instanceof IndexHasKeyConstraintType) {
        XmlIndexHasKeyConstraint constraint
            = new XmlIndexHasKeyConstraint((IndexHasKeyConstraintType) obj, MetapathExpression.CONTEXT_NODE);
        constraints.add(constraint);
        indexHasKeyConstraints.add(constraint);
      } else if (obj instanceof ExpectConstraintType) {
        XmlExpectConstraint constraint
            = new XmlExpectConstraint((ExpectConstraintType) obj, MetapathExpression.CONTEXT_NODE);
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
  }

  public ValueConstraintSupport(DefineFieldConstraintsType xmlConstraints) {
    XmlCursor cursor = xmlConstraints.newCursor();
    cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
        + "$this/m:allowed-values|$this/m:matches|$this/m:index-has-key|$this/m:expect");

    List<AbstractConstraint> constraints = new LinkedList<>();
    List<XmlAllowedValuesConstraint> allowedValuesConstraints = new LinkedList<>();
    List<XmlMatchesConstraint> matchesConstraints = new LinkedList<>();
    List<XmlIndexHasKeyConstraint> indexHasKeyConstraints = new LinkedList<>();
    List<XmlExpectConstraint> expectConstraints = new LinkedList<>();
    while (cursor.toNextSelection()) {
      XmlObject obj = cursor.getObject();
      if (obj instanceof ScopedAllowedValuesType) {
        XmlAllowedValuesConstraint constraint = new XmlAllowedValuesConstraint((ScopedAllowedValuesType) obj);
        constraints.add(constraint);
        allowedValuesConstraints.add(constraint);
      } else if (obj instanceof ScopedMatchesConstraintType) {
        XmlMatchesConstraint constraint = new XmlMatchesConstraint((ScopedMatchesConstraintType) obj);
        constraints.add(constraint);
        matchesConstraints.add(constraint);
      } else if (obj instanceof ScopedIndexHasKeyConstraintType) {
        XmlIndexHasKeyConstraint constraint = new XmlIndexHasKeyConstraint((ScopedIndexHasKeyConstraintType) obj);
        constraints.add(constraint);
        indexHasKeyConstraints.add(constraint);
      } else if (obj instanceof ScopedExpectConstraintType) {
        XmlExpectConstraint constraint = new XmlExpectConstraint((ScopedExpectConstraintType) obj);
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
  }

  @Override
  public List<? extends IConstraint> getConstraints() {
    return constraints;
  }

  @Override
  public List<XmlAllowedValuesConstraint> getAllowedValuesContraints() {
    return allowedValuesConstraints;
  }

  @Override
  public List<XmlMatchesConstraint> getMatchesConstraints() {
    return matchesConstraints;
  }

  @Override
  public List<XmlIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    return indexHasKeyConstraints;
  }

  @Override
  public List<XmlExpectConstraint> getExpectConstraints() {
    return expectConstraints;
  }
}
