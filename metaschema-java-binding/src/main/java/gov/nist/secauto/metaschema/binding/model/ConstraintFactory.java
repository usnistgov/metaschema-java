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

package gov.nist.secauto.metaschema.binding.model;

import gov.nist.secauto.metaschema.binding.model.annotations.AllowedValue;
import gov.nist.secauto.metaschema.binding.model.annotations.AllowedValues;
import gov.nist.secauto.metaschema.binding.model.annotations.Expect;
import gov.nist.secauto.metaschema.binding.model.annotations.HasCardinality;
import gov.nist.secauto.metaschema.binding.model.annotations.Index;
import gov.nist.secauto.metaschema.binding.model.annotations.IndexHasKey;
import gov.nist.secauto.metaschema.binding.model.annotations.IsUnique;
import gov.nist.secauto.metaschema.binding.model.annotations.KeyField;
import gov.nist.secauto.metaschema.binding.model.annotations.Matches;
import gov.nist.secauto.metaschema.binding.model.annotations.NullJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultAllowedValue;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultCardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultKeyField;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.ISource;
import gov.nist.secauto.metaschema.model.common.datatype.DataTypeService;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class ConstraintFactory {
  private ConstraintFactory() {
    // disable
  }

  @NotNull
  static Map<@NotNull String, DefaultAllowedValue> toAllowedValues(@NotNull AllowedValues constraint) {
    AllowedValue[] values = constraint.values();

    Map<@NotNull String, DefaultAllowedValue> allowedValues = new LinkedHashMap<>(values.length); // NOPMD - intentional
    for (AllowedValue value : values) {
      DefaultAllowedValue allowedValue
          = new DefaultAllowedValue(value.value(), MarkupLine.fromMarkdown(value.description())); // NOPMD - intentional
      allowedValues.put(allowedValue.getValue(), allowedValue);
    }
    return CollectionUtil.unmodifiableMap(allowedValues);
  }

  static String toId(String id) {
    return id.isBlank() ? null : id;
  }

  static MarkupMultiline toRemarks(String remarks) {
    return remarks.isBlank() ? null : MarkupMultiline.fromMarkdown(remarks);
  }

  static Pattern toPattern(String pattern) {
    return pattern.isBlank() ? null : Pattern.compile(pattern);
  }

  static String toMessage(String message) {
    return message.isBlank() ? null : message;
  }

  @Nullable
  static IDataTypeAdapter<?> toDataType(@NotNull Class<? extends IDataTypeAdapter<?>> adapterClass) {
    return adapterClass.isAssignableFrom(NullJavaTypeAdapter.class) ? null
        : DataTypeService.getInstance().getJavaTypeAdapterByClass(adapterClass);
  }

  @NotNull
  static MetapathExpression toMetapath(@NotNull String metapath) {
    return metapath.isBlank() ? MetapathExpression.CONTEXT_NODE : MetapathExpression.compile(metapath);
  }

  @NotNull
  static DefaultAllowedValuesConstraint newAllowedValuesConstraint(@NotNull AllowedValues constraint,
      @NotNull ISource source) {
    return new DefaultAllowedValuesConstraint(
        toId(constraint.id()),
        source,
        constraint.level(),
        toMetapath(constraint.target()),
        toAllowedValues(constraint),
        constraint.allowOthers(),
        constraint.extensible(),
        toRemarks(constraint.remarks()));
  }

  @NotNull
  static DefaultMatchesConstraint newMatchesConstraint(Matches constraint, @NotNull ISource source) {
    return new DefaultMatchesConstraint(
        toId(constraint.id()),
        source,
        constraint.level(),
        toMetapath(constraint.target()),
        toPattern(constraint.pattern()),
        toDataType(constraint.typeAdapter()),
        toRemarks(constraint.remarks()));
  }

  @NotNull
  static List<@NotNull DefaultKeyField> toKeyFields(@NotNull KeyField... keyFields) {
    List<@NotNull DefaultKeyField> retval;
    if (keyFields == null || keyFields.length == 0) {
      retval = CollectionUtil.emptyList();
    } else {
      retval = new ArrayList<>(keyFields.length);
      for (KeyField keyField : keyFields) {
        DefaultKeyField field = new DefaultKeyField(toMetapath(keyField.target()), // NOPMD - intentional
            toPattern(keyField.pattern()), toRemarks(keyField.remarks()));
        retval.add(field);
      }
      retval = CollectionUtil.unmodifiableList(retval);
    }
    return retval;
  }

  @Nullable
  static Integer toCardinality(int value) {
    return value < 0 ? null : Integer.valueOf(value);
  }

  @NotNull
  static DefaultUniqueConstraint newUniqueConstraint(@NotNull IsUnique constraint, @NotNull ISource source) {
    return new DefaultUniqueConstraint(
        toId(constraint.id()),
        source,
        constraint.level(),
        toMetapath(constraint.target()),
        toKeyFields(constraint.keyFields()),
        toRemarks(constraint.remarks()));
  }

  @NotNull
  static DefaultIndexConstraint newIndexConstraint(@NotNull Index constraint, @NotNull ISource source) {
    return new DefaultIndexConstraint(
        toId(constraint.id()),
        source,
        constraint.level(),
        toMetapath(constraint.target()),
        constraint.name(),
        toKeyFields(constraint.keyFields()),
        toRemarks(constraint.remarks()));
  }

  @NotNull
  static DefaultIndexHasKeyConstraint newIndexHasKeyConstraint(@NotNull IndexHasKey constraint,
      @NotNull ISource source) {
    return new DefaultIndexHasKeyConstraint(
        toId(constraint.id()),
        source,
        constraint.level(),
        toMetapath(constraint.target()),
        constraint.indexName(),
        toKeyFields(constraint.keyFields()),
        toRemarks(constraint.remarks()));
  }

  @NotNull
  static DefaultExpectConstraint newExpectConstraint(@NotNull Expect constraint, @NotNull ISource source) {
    return new DefaultExpectConstraint(
        toId(constraint.id()),
        source,
        constraint.level(),
        toMessage(constraint.message()),
        toMetapath(constraint.target()),
        toMetapath(constraint.test()),
        toRemarks(constraint.remarks()));
  }

  @NotNull
  static DefaultCardinalityConstraint newCardinalityConstraint(@NotNull HasCardinality constraint,
      @NotNull ISource source) {
    return new DefaultCardinalityConstraint(
        toId(constraint.id()),
        source,
        constraint.level(),
        toMetapath(constraint.target()),
        toCardinality(constraint.minOccurs()),
        toCardinality(constraint.maxOccurs()),
        toRemarks(constraint.remarks()));
  }
}
