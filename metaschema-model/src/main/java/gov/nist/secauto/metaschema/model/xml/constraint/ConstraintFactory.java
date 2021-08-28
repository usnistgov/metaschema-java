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

import gov.nist.secauto.metaschema.model.common.constraint.DefaultAllowedValue;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultCardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultKeyField;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.xml.MarkupStringConverter;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.AllowedValuesType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.EnumDocument;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ExpectConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.HasCardinalityConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.IndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.KeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.MatchesConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedAllowedValuesType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedExpectConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedIndexConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedIndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ScopedMatchesConstraintType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConstraintFactory {
  private ConstraintFactory() {
    // disable
  }

  static Map<String, DefaultAllowedValue> toAllowedValues(AllowedValuesType xmlConstraint) {
    Map<String, DefaultAllowedValue> allowedValues = new LinkedHashMap<>(xmlConstraint.sizeOfEnumArray());
    for (EnumDocument.Enum1 xmlEnum : xmlConstraint.getEnumList()) {
      DefaultAllowedValue allowedValue
          = new DefaultAllowedValue(xmlEnum.getValue(), MarkupStringConverter.toMarkupString(xmlEnum));
      allowedValues.put(allowedValue.getValue(), allowedValue);
    }
    return Collections.unmodifiableMap(allowedValues);
  }

  static DefaultAllowedValuesConstraint newAllowedValuesConstraint(ScopedAllowedValuesType xmlConstraint) {
    return newAllowedValuesConstraint(xmlConstraint, xmlConstraint.getTarget());
  }

  static DefaultAllowedValuesConstraint newAllowedValuesConstraint(AllowedValuesType xmlConstraint) {
    return newAllowedValuesConstraint(xmlConstraint, MetapathExpression.CONTEXT_NODE);
  }

  static DefaultAllowedValuesConstraint newAllowedValuesConstraint(AllowedValuesType xmlConstraint,
      MetapathExpression target) {
    return new DefaultAllowedValuesConstraint(xmlConstraint.isSetId() ? xmlConstraint.getId() : null,
        target == null ? IConstraint.DEFAULT_TARGET : target, toAllowedValues(xmlConstraint),
        xmlConstraint.isSetAllowOther() ? xmlConstraint.getAllowOther() : IAllowedValuesConstraint.DEFAULT_ALLOW_OTHER,
        xmlConstraint.isSetRemarks() ? MarkupStringConverter.toMarkupString(xmlConstraint.getRemarks()) : null);
  }

  static DefaultMatchesConstraint newMatchesConstraint(ScopedMatchesConstraintType xmlConstraint) {
    return newMatchesConstraint(xmlConstraint, xmlConstraint.getTarget());
  }

  static DefaultMatchesConstraint newMatchesConstraint(MatchesConstraintType xmlConstraint) {
    return newMatchesConstraint(xmlConstraint, MetapathExpression.CONTEXT_NODE);
  }

  static DefaultMatchesConstraint newMatchesConstraint(MatchesConstraintType xmlConstraint, MetapathExpression target) {
    return new DefaultMatchesConstraint(xmlConstraint.isSetId() ? xmlConstraint.getId() : null,
        target == null ? IConstraint.DEFAULT_TARGET : target,
        xmlConstraint.isSetRegex() ? xmlConstraint.getRegex() : null,
        xmlConstraint.isSetDatatype() ? xmlConstraint.getDatatype() : null,
        xmlConstraint.isSetRemarks() ? MarkupStringConverter.toMarkupString(xmlConstraint.getRemarks()) : null);
  }

  static List<DefaultKeyField> newKeyFields(KeyConstraintType xmlConstraint) {
    List<DefaultKeyField> keyFields = new ArrayList<>(xmlConstraint.sizeOfKeyFieldArray());
    for (KeyConstraintType.KeyField xmlKeyField : xmlConstraint.getKeyFieldList()) {
      DefaultKeyField keyField
          = new DefaultKeyField(xmlKeyField.getTarget(), xmlKeyField.isSetPattern() ? xmlKeyField.getPattern() : null,
              xmlKeyField.isSetRemarks() ? MarkupStringConverter.toMarkupString(xmlKeyField.getRemarks()) : null);
      keyFields.add(keyField);
    }
    return Collections.unmodifiableList(keyFields);
  }

  static DefaultUniqueConstraint newUniqueConstraint(ScopedKeyConstraintType xmlConstraint) {
    return new DefaultUniqueConstraint(xmlConstraint.isSetId() ? xmlConstraint.getId() : null,
        xmlConstraint.getTarget(), newKeyFields(xmlConstraint),
        xmlConstraint.isSetRemarks() ? MarkupStringConverter.toMarkupString(xmlConstraint.getRemarks()) : null);
  }

  static DefaultIndexConstraint newIndexConstraint(ScopedIndexConstraintType xmlConstraint) {
    return new DefaultIndexConstraint(xmlConstraint.isSetId() ? xmlConstraint.getId() : null, xmlConstraint.getTarget(),
        xmlConstraint.getName(), newKeyFields(xmlConstraint),
        xmlConstraint.isSetRemarks() ? MarkupStringConverter.toMarkupString(xmlConstraint.getRemarks()) : null);
  }

  static DefaultIndexHasKeyConstraint newIndexHasKeyConstraint(ScopedIndexHasKeyConstraintType xmlConstraint) {
    return newIndexHasKeyConstraint(xmlConstraint, xmlConstraint.getTarget());
  }

  static DefaultIndexHasKeyConstraint newIndexHasKeyConstraint(IndexHasKeyConstraintType xmlConstraint) {
    return newIndexHasKeyConstraint(xmlConstraint, MetapathExpression.CONTEXT_NODE);

  }

  static DefaultIndexHasKeyConstraint newIndexHasKeyConstraint(IndexHasKeyConstraintType xmlConstraint,
      MetapathExpression target) {
    return new DefaultIndexHasKeyConstraint(xmlConstraint.isSetId() ? xmlConstraint.getId() : null,
        target == null ? IConstraint.DEFAULT_TARGET : target, xmlConstraint.getName(), newKeyFields(xmlConstraint),
        xmlConstraint.isSetRemarks() ? MarkupStringConverter.toMarkupString(xmlConstraint.getRemarks()) : null);
  }

  static DefaultExpectConstraint newExpectConstraint(ScopedExpectConstraintType xmlConstraint) {
    return newExpectConstraint(xmlConstraint, xmlConstraint.getTarget());
  }

  static DefaultExpectConstraint newExpectConstraint(ExpectConstraintType xmlConstraint) {
    return newExpectConstraint(xmlConstraint, MetapathExpression.CONTEXT_NODE);
  }

  static DefaultExpectConstraint newExpectConstraint(ExpectConstraintType xmlConstraint, MetapathExpression target) {
    return new DefaultExpectConstraint(xmlConstraint.isSetId() ? xmlConstraint.getId() : null,
        target == null ? IConstraint.DEFAULT_TARGET : target, xmlConstraint.getTest(),
        xmlConstraint.isSetRemarks() ? MarkupStringConverter.toMarkupString(xmlConstraint.getRemarks()) : null);
  }

  static DefaultCardinalityConstraint newCardinalityConstraint(HasCardinalityConstraintType xmlConstraint) {
    return new DefaultCardinalityConstraint(xmlConstraint.isSetId() ? xmlConstraint.getId() : null,
        xmlConstraint.getTarget(), xmlConstraint.isSetMinOccurs() ? xmlConstraint.getMinOccurs().intValueExact() : null,
        xmlConstraint.isSetMaxOccurs() ? xmlConstraint.getMaxOccurs().intValueExact() : null,
        xmlConstraint.isSetRemarks() ? MarkupStringConverter.toMarkupString(xmlConstraint.getRemarks()) : null);
  }
}
