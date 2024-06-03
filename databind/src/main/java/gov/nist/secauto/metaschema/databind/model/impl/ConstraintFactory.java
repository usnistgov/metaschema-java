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

package gov.nist.secauto.metaschema.databind.model.impl;

import gov.nist.secauto.metaschema.core.datatype.DataTypeService;
import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IAttributable;
import gov.nist.secauto.metaschema.core.model.constraint.AbstractConstraintBuilder;
import gov.nist.secauto.metaschema.core.model.constraint.AbstractKeyConstraintBuilder;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValue;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IKeyField;
import gov.nist.secauto.metaschema.core.model.constraint.ILet;
import gov.nist.secauto.metaschema.core.model.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.databind.model.annotations.AllowedValue;
import gov.nist.secauto.metaschema.databind.model.annotations.AllowedValues;
import gov.nist.secauto.metaschema.databind.model.annotations.Expect;
import gov.nist.secauto.metaschema.databind.model.annotations.HasCardinality;
import gov.nist.secauto.metaschema.databind.model.annotations.Index;
import gov.nist.secauto.metaschema.databind.model.annotations.IndexHasKey;
import gov.nist.secauto.metaschema.databind.model.annotations.IsUnique;
import gov.nist.secauto.metaschema.databind.model.annotations.KeyField;
import gov.nist.secauto.metaschema.databind.model.annotations.Let;
import gov.nist.secauto.metaschema.databind.model.annotations.Matches;
import gov.nist.secauto.metaschema.databind.model.annotations.NullJavaTypeAdapter;
import gov.nist.secauto.metaschema.databind.model.annotations.Property;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

final class ConstraintFactory {
  private static final Logger LOGGER = LogManager.getLogger(ConstraintFactory.class);

  private ConstraintFactory() {
    // disable
  }

  static MarkupMultiline toRemarks(@NonNull String remarks) {
    return remarks.isBlank() ? null : MarkupMultiline.fromMarkdown(remarks);
  }

  @NonNull
  static String toMetapath(@NonNull String metapath) {
    String path = metapath;
    if (path.startsWith("/")) {
      String newPath = "." + path;

      if (LOGGER.isInfoEnabled()) {
        StringBuilder builder = new StringBuilder(80)
            .append("The path '")
            .append(path)
            .append("' is not properly contextualized using '.'. Using '")
            .append(newPath)
            .append("' instead.");
        LOGGER.atInfo().log(builder.toString());
      }
      path = newPath;
    }

    return path.isBlank() ? IConstraint.DEFAULT_TARGET_METAPATH : path;
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
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // ok
        IAttributable.Key key = IAttributable.key(namespace, name);

        String[] values = property.values();
        List<String> valueList = Arrays.asList(values);
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // ok
        Set<String> valueSet = new LinkedHashSet<>(valueList);
        builder.property(key, valueSet);
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
  static IAllowedValuesConstraint.Builder applyAllowedValues(
      @NonNull IAllowedValuesConstraint.Builder builder,
      @NonNull AllowedValues constraint) {
    for (AllowedValue value : constraint.values()) {
      IAllowedValue allowedValue = IAllowedValue.of(value.value(), MarkupLine.fromMarkdown(value.description()));
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
  static IAllowedValuesConstraint newAllowedValuesConstraint(
      @NonNull AllowedValues constraint,
      @NonNull ISource source) {
    IAllowedValuesConstraint.Builder builder = IAllowedValuesConstraint.builder();
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
  static IMatchesConstraint newMatchesConstraint(Matches constraint, @NonNull ISource source) {
    IMatchesConstraint.Builder builder = IMatchesConstraint.builder();
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
      @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // ok
      IKeyField field = IKeyField.of(
          toMetapath(keyField.target()),
          toPattern(keyField.pattern()),
          toRemarks(keyField.remarks()));
      builder.keyField(field);
    }
    return builder;
  }

  @NonNull
  static IUniqueConstraint newUniqueConstraint(@NonNull IsUnique constraint, @NonNull ISource source) {
    IUniqueConstraint.Builder builder = IUniqueConstraint.builder();
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
  static IIndexConstraint newIndexConstraint(@NonNull Index constraint, @NonNull ISource source) {
    IIndexConstraint.Builder builder = IIndexConstraint.builder();
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
  static IIndexHasKeyConstraint newIndexHasKeyConstraint(
      @NonNull IndexHasKey constraint,
      @NonNull ISource source) {
    IIndexHasKeyConstraint.Builder builder = IIndexHasKeyConstraint.builder();
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
  static IExpectConstraint newExpectConstraint(@NonNull Expect constraint, @NonNull ISource source) {
    IExpectConstraint.Builder builder = IExpectConstraint.builder();
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
    return value < 0 ? null : value;
  }

  @NonNull
  static ICardinalityConstraint newCardinalityConstraint(@NonNull HasCardinality constraint,
      @NonNull ISource source) {
    ICardinalityConstraint.Builder builder = ICardinalityConstraint.builder();
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

  @NonNull
  static ILet newLetExpression(@NonNull Let annotation, @NonNull ISource source) {
    return ILet.of(new QName(annotation.name()), annotation.target(), source);
  }
}
