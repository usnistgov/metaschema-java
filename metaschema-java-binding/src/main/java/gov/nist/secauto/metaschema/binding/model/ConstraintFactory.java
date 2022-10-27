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
import gov.nist.secauto.metaschema.binding.model.annotations.Property;
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
import gov.nist.secauto.metaschema.model.common.datatype.DataTypeService;
import gov.nist.secauto.metaschema.model.common.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

final class ConstraintFactory {
  private ConstraintFactory() {
    // disable
  }

  static MarkupMultiline toRemarks(@NonNull String remarks) {
    return remarks.isBlank() ? null : MarkupMultiline.fromMarkdown(remarks);
  }

  @NonNull
  static MetapathExpression toMetapath(@NonNull String metapath) {
    return metapath.isBlank() ? IConstraint.DEFAULT_TARGET : MetapathExpression.compile(metapath);
  }

  @NonNull
  static <T extends AbstractConstraintBuilder<T, ?>> T applyId(@NonNull T builder, @NonNull String id) {
    if (!id.isBlank()) {
      builder.identifier(id);
    }
    return builder;
  }

  @NonNull
  static <T extends AbstractConstraintBuilder<T, ?>> T applyFormalName(@NonNull T builder, @NonNull String name) {
    if (!name.isBlank()) {
      builder.formalName(name);
    }
    return builder;
  }

  @NonNull
  static <T extends AbstractConstraintBuilder<T, ?>> T applyDescription(@NonNull T builder, @NonNull String value) {
    if (!value.isBlank()) {
      builder.description(MarkupLine.fromMarkdown(value));
    }
    return builder;
  }

  @NonNull
  static <T extends AbstractConstraintBuilder<T, ?>> T applyTarget(@NonNull T builder, @NonNull String target) {
    builder.target(toMetapath(target));
    return builder;
  }

  @NonNull
  static <T extends AbstractConstraintBuilder<T, ?>> T applyProperties(
      @NonNull T builder,
      @Nullable Property... properties) {
    if (properties != null) {
      for (Property property : properties) {
        String name = property.name();
        String namespace = property.namespace();
        QName qname = new QName(namespace, name);

        String[] values = property.values();
        List<String> valueList = Arrays.asList(values);
        Set<String> valueSet = new LinkedHashSet<>(valueList);
        builder.property(qname, valueSet);
      }
    }
    return builder;
  }

  static <T extends AbstractConstraintBuilder<T, ?>> T applyRemarks(@NonNull T builder, @NonNull String remarks) {
    if (!remarks.isBlank()) {
      builder.remarks(MarkupMultiline.fromMarkdown(remarks));
    }
    return builder;
  }

  @NonNull
  static DefaultAllowedValuesConstraint.Builder applyAllowedValues(
      @NonNull DefaultAllowedValuesConstraint.Builder builder,
      @NonNull AllowedValues constraint) {
    for (AllowedValue value : constraint.values()) {
      DefaultAllowedValue allowedValue
          = new DefaultAllowedValue(value.value(), MarkupLine.fromMarkdown(value.description())); // NOPMD - intentional
      builder.allowedValue(allowedValue);
    }
    return builder;
  }

  @Nullable
  static Pattern toPattern(@NonNull String pattern) {
    return pattern.isBlank() ? null : Pattern.compile(pattern);
  }

  @Nullable
  static String toMessage(@NonNull String message) {
    return message.isBlank() ? null : message;
  }

  @Nullable
  static IDataTypeAdapter<?> toDataType(@NonNull Class<? extends IDataTypeAdapter<?>> adapterClass) {
    return adapterClass.isAssignableFrom(NullJavaTypeAdapter.class) ? null
        : DataTypeService.getInstance().getJavaTypeAdapterByClass(adapterClass);
  }

  @NonNull
  static DefaultAllowedValuesConstraint newAllowedValuesConstraint(
      @NonNull AllowedValues constraint,
      @NonNull ISource source) {
    DefaultAllowedValuesConstraint.Builder builder = DefaultAllowedValuesConstraint.builder();
    applyId(builder, constraint.id());
    applyFormalName(builder, constraint.formalName());
    applyDescription(builder, constraint.description());
    builder
        .source(source)
        .level(constraint.level());
    applyTarget(builder, constraint.target());
    applyProperties(builder, constraint.properties());
    applyRemarks(builder, constraint.remarks());

    applyAllowedValues(builder, constraint);
    builder.allowedOther(constraint.allowOthers());
    builder.extensible(constraint.extensible());

    return builder.build();
  }

  @NonNull
  static DefaultMatchesConstraint newMatchesConstraint(Matches constraint, @NonNull ISource source) {
    DefaultMatchesConstraint.Builder builder = DefaultMatchesConstraint.builder();
    applyId(builder, constraint.id());
    applyFormalName(builder, constraint.formalName());
    applyDescription(builder, constraint.description());
    builder
        .source(source)
        .level(constraint.level());
    applyTarget(builder, constraint.target());
    applyProperties(builder, constraint.properties());
    applyRemarks(builder, constraint.remarks());

    Pattern pattern = toPattern(constraint.pattern());
    if (pattern != null) {
      builder.regex(pattern);
    }

    IDataTypeAdapter<?> dataType = toDataType(constraint.typeAdapter());
    if (dataType != null) {
      builder.datatype(dataType);
    }

    return builder.build();
  }

  @NonNull
  static <T extends AbstractKeyConstraintBuilder<T, ?>> T applyKeyFields(@NonNull T builder,
      @NonNull KeyField... keyFields) {
    for (KeyField keyField : keyFields) {
      DefaultKeyField field = new DefaultKeyField(
          toMetapath(keyField.target()),
          toPattern(keyField.pattern()),
          toRemarks(keyField.remarks()));
      builder.keyField(field);
    }
    return builder;
  }

  @NonNull
  static DefaultUniqueConstraint newUniqueConstraint(@NonNull IsUnique constraint, @NonNull ISource source) {
    DefaultUniqueConstraint.Builder builder = DefaultUniqueConstraint.builder();
    applyId(builder, constraint.id());
    applyFormalName(builder, constraint.formalName());
    applyDescription(builder, constraint.description());
    builder
        .source(source)
        .level(constraint.level());
    applyTarget(builder, constraint.target());
    applyProperties(builder, constraint.properties());
    applyRemarks(builder, constraint.remarks());

    applyKeyFields(builder, constraint.keyFields());

    return builder.build();
  }

  @NonNull
  static DefaultIndexConstraint newIndexConstraint(@NonNull Index constraint, @NonNull ISource source) {
    DefaultIndexConstraint.Builder builder = DefaultIndexConstraint.builder();
    applyId(builder, constraint.id());
    applyFormalName(builder, constraint.formalName());
    applyDescription(builder, constraint.description());
    builder
        .source(source)
        .level(constraint.level());
    applyTarget(builder, constraint.target());
    applyProperties(builder, constraint.properties());
    applyRemarks(builder, constraint.remarks());

    builder.name(constraint.name());
    applyKeyFields(builder, constraint.keyFields());

    return builder.build();
  }

  @NonNull
  static DefaultIndexHasKeyConstraint newIndexHasKeyConstraint(@NonNull IndexHasKey constraint,
      @NonNull ISource source) {
    DefaultIndexHasKeyConstraint.Builder builder = DefaultIndexHasKeyConstraint.builder();
    applyId(builder, constraint.id());
    applyFormalName(builder, constraint.formalName());
    applyDescription(builder, constraint.description());
    builder
        .source(source)
        .level(constraint.level());
    applyTarget(builder, constraint.target());
    applyProperties(builder, constraint.properties());
    applyRemarks(builder, constraint.remarks());

    builder.name(constraint.indexName());
    applyKeyFields(builder, constraint.keyFields());

    return builder.build();
  }

  @NonNull
  static DefaultExpectConstraint newExpectConstraint(@NonNull Expect constraint, @NonNull ISource source) {
    DefaultExpectConstraint.Builder builder = DefaultExpectConstraint.builder();
    applyId(builder, constraint.id());
    applyFormalName(builder, constraint.formalName());
    applyDescription(builder, constraint.description());
    builder
        .source(source)
        .level(constraint.level());
    applyTarget(builder, constraint.target());
    applyProperties(builder, constraint.properties());
    applyRemarks(builder, constraint.remarks());

    builder.test(toMetapath(constraint.test()));

    String message = constraint.message();
    if (!message.isBlank()) {
      builder.message(message);
    }

    return builder.build();
  }

  @Nullable
  static Integer toCardinality(int value) {
    return value < 0 ? null : Integer.valueOf(value);
  }

  @NonNull
  static DefaultCardinalityConstraint newCardinalityConstraint(@NonNull HasCardinality constraint,
      @NonNull ISource source) {
    DefaultCardinalityConstraint.Builder builder = DefaultCardinalityConstraint.builder();
    applyId(builder, constraint.id());
    applyFormalName(builder, constraint.formalName());
    applyDescription(builder, constraint.description());
    builder
        .source(source)
        .level(constraint.level());
    applyTarget(builder, constraint.target());
    applyProperties(builder, constraint.properties());
    applyRemarks(builder, constraint.remarks());

    Integer min = toCardinality(constraint.minOccurs());
    if (min != null) {
      builder.minOccurs(min);
    }
    Integer max = toCardinality(constraint.maxOccurs());
    if (max != null) {
      builder.maxOccurs(max);
    }

    return builder.build();
  }
}
