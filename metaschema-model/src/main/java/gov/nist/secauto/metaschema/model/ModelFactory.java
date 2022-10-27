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

import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.constraint.AbstractConstraint.AbstractConstraintBuilder;
import gov.nist.secauto.metaschema.model.common.constraint.AbstractKeyConstraint.AbstractKeyConstraintBuilder;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultAllowedValue;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultCardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultKeyField;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.ISource;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.Level;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.model.xmlbeans.AllowedValuesType;
import gov.nist.secauto.metaschema.model.xmlbeans.ConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.EnumType;
import gov.nist.secauto.metaschema.model.xmlbeans.ExpectConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.HasCardinalityConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.IndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.KeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.MatchesConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.PropertyType;
import gov.nist.secauto.metaschema.model.xmlbeans.RemarksType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedAllowedValuesType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedExpectConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedIndexConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedIndexHasKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedKeyConstraintType;
import gov.nist.secauto.metaschema.model.xmlbeans.ScopedMatchesConstraintType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

final class ModelFactory {
  private ModelFactory() {
    // disable
  }

  @NonNull
  private static MetapathExpression target(@Nullable MetapathExpression target) {
    return target == null ? IConstraint.DEFAULT_TARGET : target;
  }

  @NonNull
  private static Level level(@Nullable Level level) {
    return level == null ? IConstraint.DEFAULT_LEVEL : level;
  }

  @NonNull
  private static MarkupMultiline remarks(@NonNull RemarksType remarks) {
    return MarkupStringConverter.toMarkupString(remarks);
  }

  @SuppressWarnings("null")
  @NonNull
  static Map<QName, Set<String>> toProperties(@NonNull List<PropertyType> properties) {
    return properties.stream()
        .map(prop -> {
          String name = prop.getName();
          String namespace = prop.isSetNamespace() ? prop.getNamespace() : IMetaschema.METASCHEMA_XML_NS;
          QName qname = new QName(namespace, name);
          String value = prop.getValue();

          return Map.entry(qname, value);
        })
        .collect(Collectors.groupingBy(Map.Entry<QName, String>::getKey,
            Collectors.mapping(Map.Entry<QName, String>::getValue, Collectors.toSet())));
  }

  @NonNull
  static Map<String, DefaultAllowedValue> toAllowedValues(@NonNull AllowedValuesType xmlConstraint) {
    Map<String, DefaultAllowedValue> allowedValues // NOPMD - intentional
        = new LinkedHashMap<>(xmlConstraint.sizeOfEnumArray());
    for (EnumType xmlEnum : xmlConstraint.getEnumList()) {
      @SuppressWarnings("null") DefaultAllowedValue allowedValue = new DefaultAllowedValue( // NOPMD - intentional
          xmlEnum.getValue(),
          MarkupStringConverter.toMarkupString(xmlEnum));
      allowedValues.put(allowedValue.getValue(), allowedValue);
    }
    return CollectionUtil.unmodifiableMap(allowedValues);
  }

  @NonNull
  static DefaultAllowedValuesConstraint newAllowedValuesConstraint(
      @NonNull ScopedAllowedValuesType xmlConstraint,
      @NonNull ISource source) {
    return newAllowedValuesConstraint(xmlConstraint, target(xmlConstraint.getTarget()), source);
  }

  @NonNull
  static DefaultAllowedValuesConstraint newAllowedValuesConstraint(
      @NonNull AllowedValuesType xmlConstraint,
      @NonNull ISource source) {
    return newAllowedValuesConstraint(xmlConstraint, MetapathExpression.CONTEXT_NODE, source);
  }

  @NonNull
  private static <T extends AbstractConstraintBuilder<T, ?>> T applyToBuilder(
      @NonNull ConstraintType xmlConstraint,
      @NonNull MetapathExpression target,
      @NonNull ISource source,
      @NonNull T builder) {

    if (xmlConstraint.isSetId()) {
      builder.identifier(ObjectUtils.notNull(xmlConstraint.getId()));
    }
    builder.target(target);
    builder.source(source);
    builder.level(level(xmlConstraint.getLevel()));
    return builder;
  }

  @NonNull
  static DefaultAllowedValuesConstraint newAllowedValuesConstraint(
      @NonNull AllowedValuesType xmlConstraint,
      @NonNull MetapathExpression target,
      @NonNull ISource source) {

    DefaultAllowedValuesConstraint.Builder builder = DefaultAllowedValuesConstraint.builder();

    applyToBuilder(xmlConstraint, target, source, builder);

    if (xmlConstraint.isSetRemarks()) {
      builder.remarks(remarks(ObjectUtils.notNull(xmlConstraint.getRemarks())));
    }

    builder.allowedValues(toAllowedValues(xmlConstraint));
    if (xmlConstraint.isSetAllowOther()) {
      builder.allowedOther(xmlConstraint.getAllowOther());
    }
    if (xmlConstraint.isSetExtensible()) {
      builder.extensible(ObjectUtils.notNull(xmlConstraint.getExtensible()));
    }

    return builder.build();
  }

  @NonNull
  static DefaultMatchesConstraint newMatchesConstraint(
      @NonNull ScopedMatchesConstraintType xmlConstraint,
      @NonNull ISource source) {
    return newMatchesConstraint(xmlConstraint, target(xmlConstraint.getTarget()), source);
  }

  @NonNull
  static DefaultMatchesConstraint newMatchesConstraint(
      @NonNull MatchesConstraintType xmlConstraint,
      @NonNull ISource source) {
    return newMatchesConstraint(xmlConstraint, MetapathExpression.CONTEXT_NODE, source);
  }

  @NonNull
  static DefaultMatchesConstraint newMatchesConstraint(
      @NonNull MatchesConstraintType xmlConstraint,
      @NonNull MetapathExpression target,
      @NonNull ISource source) {
    DefaultMatchesConstraint.Builder builder = DefaultMatchesConstraint.builder();

    applyToBuilder(xmlConstraint, target, source, builder);

    if (xmlConstraint.isSetRemarks()) {
      builder.remarks(remarks(ObjectUtils.notNull(xmlConstraint.getRemarks())));
    }

    if (xmlConstraint.isSetRegex()) {
      builder.regex(ObjectUtils.notNull(xmlConstraint.getRegex()));
    }
    if (xmlConstraint.isSetDatatype()) {
      builder.datatype(ObjectUtils.notNull(xmlConstraint.getDatatype()));
    }

    return builder.build();
  }

  static void buildKeyFields(
      @NonNull KeyConstraintType xmlConstraint,
      @NonNull AbstractKeyConstraintBuilder<?, ?> builder) {
    for (KeyConstraintType.KeyField xmlKeyField : xmlConstraint.getKeyFieldList()) {
      @SuppressWarnings("null") DefaultKeyField keyField
          = new DefaultKeyField( // NOPMD - intentional
              xmlKeyField.getTarget(),
              xmlKeyField.isSetPattern() ? xmlKeyField.getPattern() : null, // NOPMD - intentional
              xmlKeyField.isSetRemarks() ? remarks(xmlKeyField.getRemarks()) : null);
      builder.keyField(keyField);
    }
  }

  @NonNull
  static DefaultUniqueConstraint newUniqueConstraint(
      @NonNull ScopedKeyConstraintType xmlConstraint,
      @NonNull ISource source) {
    DefaultUniqueConstraint.Builder builder = DefaultUniqueConstraint.builder();

    applyToBuilder(xmlConstraint, target(xmlConstraint.getTarget()), source, builder);

    if (xmlConstraint.isSetRemarks()) {
      builder.remarks(remarks(ObjectUtils.notNull(xmlConstraint.getRemarks())));
    }

    buildKeyFields(xmlConstraint, builder);

    return builder.build();
  }

  @NonNull
  static DefaultIndexConstraint newIndexConstraint(
      @NonNull ScopedIndexConstraintType xmlConstraint,
      @NonNull ISource source) {
    DefaultIndexConstraint.Builder builder = DefaultIndexConstraint.builder();

    applyToBuilder(xmlConstraint, target(xmlConstraint.getTarget()), source, builder);

    if (xmlConstraint.isSetRemarks()) {
      builder.remarks(remarks(ObjectUtils.notNull(xmlConstraint.getRemarks())));
    }

    builder.name(ObjectUtils.requireNonNull(xmlConstraint.getName()));
    buildKeyFields(xmlConstraint, builder);

    return builder.build();
  }

  @NonNull
  static DefaultIndexHasKeyConstraint newIndexHasKeyConstraint(
      @NonNull ScopedIndexHasKeyConstraintType xmlConstraint,
      @NonNull ISource source) {
    return newIndexHasKeyConstraint(xmlConstraint, target(xmlConstraint.getTarget()), source);
  }

  @NonNull
  static DefaultIndexHasKeyConstraint newIndexHasKeyConstraint(
      @NonNull IndexHasKeyConstraintType xmlConstraint,
      @NonNull ISource source) {
    return newIndexHasKeyConstraint(xmlConstraint, MetapathExpression.CONTEXT_NODE, source);

  }

  @NonNull
  static DefaultIndexHasKeyConstraint newIndexHasKeyConstraint(
      @NonNull IndexHasKeyConstraintType xmlConstraint,
      @NonNull MetapathExpression target,
      @NonNull ISource source) {
    DefaultIndexHasKeyConstraint.Builder builder = DefaultIndexHasKeyConstraint.builder();

    applyToBuilder(xmlConstraint, target, source, builder);

    if (xmlConstraint.isSetRemarks()) {
      builder.remarks(remarks(ObjectUtils.notNull(xmlConstraint.getRemarks())));
    }

    builder.name(ObjectUtils.requireNonNull(xmlConstraint.getName()));
    buildKeyFields(xmlConstraint, builder);

    return builder.build();
  }

  @NonNull
  static DefaultExpectConstraint newExpectConstraint(
      @NonNull ScopedExpectConstraintType xmlConstraint,
      @NonNull ISource source) {
    return newExpectConstraint(xmlConstraint, target(xmlConstraint.getTarget()), source);
  }

  @NonNull
  static DefaultExpectConstraint newExpectConstraint(
      @NonNull ExpectConstraintType xmlConstraint,
      @NonNull ISource source) {
    return newExpectConstraint(xmlConstraint, MetapathExpression.CONTEXT_NODE, source);
  }

  @NonNull
  static DefaultExpectConstraint newExpectConstraint(
      @NonNull ExpectConstraintType xmlConstraint,
      @NonNull MetapathExpression target,
      @NonNull ISource source) {

    DefaultExpectConstraint.Builder builder = DefaultExpectConstraint.builder();

    applyToBuilder(xmlConstraint, target, source, builder);

    if (xmlConstraint.isSetRemarks()) {
      builder.remarks(remarks(ObjectUtils.notNull(xmlConstraint.getRemarks())));
    }

    if (xmlConstraint.isSetMessage()) {
      builder.message(ObjectUtils.notNull(xmlConstraint.getMessage()));
    }

    builder.test(ObjectUtils.requireNonNull(xmlConstraint.getTest()));

    return builder.build();
  }

  @NonNull
  static DefaultCardinalityConstraint newCardinalityConstraint(
      @NonNull HasCardinalityConstraintType xmlConstraint,
      @NonNull ISource source) {

    DefaultCardinalityConstraint.Builder builder = DefaultCardinalityConstraint.builder();

    applyToBuilder(xmlConstraint, target(xmlConstraint.getTarget()), source, builder);

    if (xmlConstraint.isSetRemarks()) {
      builder.remarks(remarks(ObjectUtils.notNull(xmlConstraint.getRemarks())));
    }

    if (xmlConstraint.isSetMinOccurs()) {
      builder.minOccurs(xmlConstraint.getMinOccurs().intValueExact());
    }

    if (xmlConstraint.isSetMaxOccurs()) {
      builder.maxOccurs(xmlConstraint.getMaxOccurs().intValueExact());
    }

    return builder.build();
  }
}
