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
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.ISource;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.Level;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.model.xmlbeans.AllowedValuesType;
import gov.nist.secauto.metaschema.model.xmlbeans.EnumType;
import gov.nist.secauto.metaschema.model.xmlbeans.ExpectConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.HasCardinalityConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.IndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.KeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.MatchesConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.RemarksType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedAllowedValuesType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedExpectConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedIndexConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedIndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedMatchesConstraintType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ConstraintFactory {
  private ConstraintFactory() {
    // disable
  }

  @NotNull
  private static MetapathExpression target(@Nullable MetapathExpression target) {
    return target == null ? IConstraint.DEFAULT_TARGET : target;
  }

  @NotNull
  private static Level level(@Nullable Level level) {
    return level == null ? IConstraint.DEFAULT_LEVEL : level;
  }

  @Nullable
  private static MarkupMultiline remarks(@Nullable RemarksType remarks) {
    return remarks == null ? null : MarkupStringConverter.toMarkupString(remarks);
  }

  @NotNull
  static Map<@NotNull String, DefaultAllowedValue> toAllowedValues(@NotNull AllowedValuesType xmlConstraint) {
    Map<@NotNull String, DefaultAllowedValue> allowedValues // NOPMD - intentional
        = new LinkedHashMap<>(xmlConstraint.sizeOfEnumArray());
    for (EnumType xmlEnum : xmlConstraint.getEnumList()) {
      @SuppressWarnings("null")
      DefaultAllowedValue allowedValue = new DefaultAllowedValue( // NOPMD - intentional
          xmlEnum.getValue(),
          MarkupStringConverter.toMarkupString(xmlEnum));
      allowedValues.put(allowedValue.getValue(), allowedValue);
    }
    return CollectionUtil.unmodifiableMap(allowedValues);
  }

  @NotNull
  static DefaultAllowedValuesConstraint newAllowedValuesConstraint(
      @NotNull ScopedAllowedValuesType xmlConstraint,
      @NotNull ISource source) {
    return newAllowedValuesConstraint(xmlConstraint, target(xmlConstraint.getTarget()), source);
  }

  @NotNull
  static DefaultAllowedValuesConstraint newAllowedValuesConstraint(
      @NotNull AllowedValuesType xmlConstraint,
      @NotNull ISource source) {
    return newAllowedValuesConstraint(xmlConstraint, MetapathExpression.CONTEXT_NODE, source);
  }

  @NotNull
  static DefaultAllowedValuesConstraint newAllowedValuesConstraint(
      @NotNull AllowedValuesType xmlConstraint,
      @NotNull MetapathExpression target,
      @NotNull ISource source) {
    return new DefaultAllowedValuesConstraint(
        xmlConstraint.isSetId() ? xmlConstraint.getId() : null, // NOPMD - intentional
        source,
        level(xmlConstraint.getLevel()),
        target,
        toAllowedValues(xmlConstraint),
        xmlConstraint.isSetAllowOther() ? xmlConstraint.getAllowOther() : IAllowedValuesConstraint.DEFAULT_ALLOW_OTHER,
        xmlConstraint.isSetExtensible() ? ObjectUtils.notNull(xmlConstraint.getExtensible())
            : IAllowedValuesConstraint.DEFAULT_EXTENSIBLE,
        remarks(xmlConstraint.getRemarks()));
  }

  @NotNull
  static DefaultMatchesConstraint newMatchesConstraint(
      @NotNull ScopedMatchesConstraintType xmlConstraint,
      @NotNull ISource source) {
    return newMatchesConstraint(xmlConstraint, target(xmlConstraint.getTarget()), source);
  }

  @NotNull
  static DefaultMatchesConstraint newMatchesConstraint(
      @NotNull MatchesConstraintType xmlConstraint,
      @NotNull ISource source) {
    return newMatchesConstraint(xmlConstraint, MetapathExpression.CONTEXT_NODE, source);
  }

  @NotNull
  static DefaultMatchesConstraint newMatchesConstraint(
      @NotNull MatchesConstraintType xmlConstraint,
      @NotNull MetapathExpression target,
      @NotNull ISource source) {
    return new DefaultMatchesConstraint(
        xmlConstraint.isSetId() ? xmlConstraint.getId() : null, // NOPMD - intentional
        source,
        level(xmlConstraint.getLevel()),
        target,
        xmlConstraint.isSetRegex() ? xmlConstraint.getRegex() : null, // NOPMD - intentional
        xmlConstraint.isSetDatatype() ? xmlConstraint.getDatatype() : null, // NOPMD - intentional
        remarks(xmlConstraint.getRemarks()));
  }

  @NotNull
  static List<@NotNull DefaultKeyField> newKeyFields(@NotNull KeyConstraintType xmlConstraint) {
    List<@NotNull DefaultKeyField> keyFields = new ArrayList<>(xmlConstraint.sizeOfKeyFieldArray());
    for (KeyConstraintType.KeyField xmlKeyField : xmlConstraint.getKeyFieldList()) {
      @SuppressWarnings("null")
      DefaultKeyField keyField
          = new DefaultKeyField( // NOPMD - intentional
              xmlKeyField.getTarget(),
              xmlKeyField.isSetPattern() ? xmlKeyField.getPattern() : null, // NOPMD - intentional
              remarks(xmlKeyField.getRemarks()));
      keyFields.add(keyField);
    }
    return CollectionUtil.unmodifiableList(keyFields);
  }

  @NotNull
  static DefaultUniqueConstraint newUniqueConstraint(
      @NotNull ScopedKeyConstraintType xmlConstraint,
      @NotNull ISource source) {
    return new DefaultUniqueConstraint(
        xmlConstraint.isSetId() ? xmlConstraint.getId() : null, // NOPMD - intentional
        source,
        level(xmlConstraint.getLevel()),
        target(xmlConstraint.getTarget()),
        newKeyFields(xmlConstraint),
        remarks(xmlConstraint.getRemarks()));
  }

  @NotNull
  static DefaultIndexConstraint newIndexConstraint(
      @NotNull ScopedIndexConstraintType xmlConstraint,
      @NotNull ISource source) {
    return new DefaultIndexConstraint(
        xmlConstraint.isSetId() ? xmlConstraint.getId() : null, // NOPMD - intentional
        source,
        level(xmlConstraint.getLevel()),
        target(xmlConstraint.getTarget()),
        xmlConstraint.getName(),
        newKeyFields(xmlConstraint),
        remarks(xmlConstraint.getRemarks()));
  }

  @NotNull
  static DefaultIndexHasKeyConstraint newIndexHasKeyConstraint(
      @NotNull ScopedIndexHasKeyConstraintType xmlConstraint,
      @NotNull ISource source) {
    return newIndexHasKeyConstraint(xmlConstraint, target(xmlConstraint.getTarget()), source);
  }

  @NotNull
  static DefaultIndexHasKeyConstraint newIndexHasKeyConstraint(
      @NotNull IndexHasKeyConstraintType xmlConstraint,
      @NotNull ISource source) {
    return newIndexHasKeyConstraint(xmlConstraint, MetapathExpression.CONTEXT_NODE, source);

  }

  @NotNull
  static DefaultIndexHasKeyConstraint newIndexHasKeyConstraint(
      @NotNull IndexHasKeyConstraintType xmlConstraint,
      @NotNull MetapathExpression target,
      @NotNull ISource source) {
    return new DefaultIndexHasKeyConstraint(
        xmlConstraint.isSetId() ? xmlConstraint.getId() : null, // NOPMD - intentional
        source,
        level(xmlConstraint.getLevel()),
        target,
        xmlConstraint.getName(),
        newKeyFields(xmlConstraint),
        remarks(xmlConstraint.getRemarks()));
  }

  @NotNull
  static DefaultExpectConstraint newExpectConstraint(
      @NotNull ScopedExpectConstraintType xmlConstraint,
      @NotNull ISource source) {
    return newExpectConstraint(xmlConstraint, target(xmlConstraint.getTarget()), source);
  }

  @NotNull
  static DefaultExpectConstraint newExpectConstraint(
      @NotNull ExpectConstraintType xmlConstraint,
      @NotNull ISource source) {
    return newExpectConstraint(xmlConstraint, MetapathExpression.CONTEXT_NODE, source);
  }

  @NotNull
  static DefaultExpectConstraint newExpectConstraint(
      @NotNull ExpectConstraintType xmlConstraint,
      @NotNull MetapathExpression target,
      @NotNull ISource source) {
    return new DefaultExpectConstraint(
        xmlConstraint.isSetId() ? xmlConstraint.getId() : null, // NOPMD - intentional
        source,
        level(xmlConstraint.getLevel()),
        xmlConstraint.isSetMessage() ? xmlConstraint.getMessage() : null, // NOPMD - intentional
        target,
        ObjectUtils.requireNonNull(xmlConstraint.getTest()),
        remarks(xmlConstraint.getRemarks()));
  }

  @NotNull
  static DefaultCardinalityConstraint newCardinalityConstraint(
      @NotNull HasCardinalityConstraintType xmlConstraint,
      @NotNull ISource source) {
    return new DefaultCardinalityConstraint(
        xmlConstraint.isSetId() ? xmlConstraint.getId() : null, // NOPMD - intentional
        source,
        level(xmlConstraint.getLevel()),
        target(xmlConstraint.getTarget()),
        xmlConstraint.isSetMinOccurs() ? xmlConstraint.getMinOccurs().intValueExact() : null, // NOPMD - intentional
        xmlConstraint.isSetMaxOccurs() ? xmlConstraint.getMaxOccurs().intValueExact() : null, // NOPMD - intentional
        remarks(xmlConstraint.getRemarks()));
  }
}
