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

package gov.nist.secauto.metaschema.databind.codegen.impl;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.AnnotationSpec.Builder;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDefinitionNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItemFactory;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.INamedInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValue;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IKeyField;
import gov.nist.secauto.metaschema.core.model.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.annotations.AllowedValue;
import gov.nist.secauto.metaschema.databind.model.annotations.AllowedValues;
import gov.nist.secauto.metaschema.databind.model.annotations.AssemblyConstraints;
import gov.nist.secauto.metaschema.databind.model.annotations.Expect;
import gov.nist.secauto.metaschema.databind.model.annotations.HasCardinality;
import gov.nist.secauto.metaschema.databind.model.annotations.Index;
import gov.nist.secauto.metaschema.databind.model.annotations.IndexHasKey;
import gov.nist.secauto.metaschema.databind.model.annotations.IsUnique;
import gov.nist.secauto.metaschema.databind.model.annotations.KeyField;
import gov.nist.secauto.metaschema.databind.model.annotations.Matches;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A variety of utility functions for creating Module annotations.
 */
@SuppressWarnings({
    "PMD.GodClass", "PMD.CouplingBetweenObjects" // utility class
})
public final class AnnotationGenerator {
  private static final Logger LOGGER = LogManager.getLogger(AnnotationGenerator.class);

  private AnnotationGenerator() {
    // disable construction
  }

  /**
   * Get the default vale of the given member of an annotation.
   *
   * @param annotation
   *          the annotation to analyze
   * @param member
   *          the annotation member to analyze
   * @return the default value for the annotation member or {@code null} if there
   *         is not default value
   */
  public static Object getDefaultValue(Class<?> annotation, String member) {
    Method method;
    try {
      method = annotation.getDeclaredMethod(member);
    } catch (NoSuchMethodException ex) {
      throw new IllegalArgumentException(ex);
    }
    Object retval;
    try {
      retval = method.getDefaultValue();
    } catch (TypeNotPresentException ex) {
      retval = null; // NOPMD readability
    }
    return retval;
  }

  private static void buildConstraint(Class<?> annotationType, AnnotationSpec.Builder annotation,
      IConstraint constraint) {
    String id = constraint.getId();
    if (id != null) {
      annotation.addMember("id", "$S", id);
    }

    String formalName = constraint.getFormalName();
    if (formalName != null) {
      annotation.addMember("formalName", "$S", formalName);
    }

    MarkupLine description = constraint.getDescription();
    if (description != null) {
      annotation.addMember("description", "$S", description.toMarkdown());
    }

    annotation.addMember("level", "$T.$L", IConstraint.Level.class, constraint.getLevel());

    String target = constraint.getTarget();
    if (!target.equals(getDefaultValue(annotationType, "target"))) {
      annotation.addMember("target", "$S", target);
    }
  }

  public static void buildValueConstraints(
      @NonNull AnnotationSpec.Builder builder,
      @NonNull IFlagDefinition definition) {
    if (!definition.getConstraints().isEmpty()) {
      AnnotationSpec.Builder annotation = AnnotationSpec.builder(ValueConstraints.class);

      applyAllowedValuesConstraints(annotation, definition.getAllowedValuesConstraints());
      applyIndexHasKeyConstraints(annotation, definition.getIndexHasKeyConstraints());
      applyMatchesConstraints(annotation, definition.getMatchesConstraints());
      applyExpectConstraints(annotation, definition.getExpectConstraints());

      builder.addMember("valueConstraints", "$L", annotation.build());
    }
  }

  public static void buildValueConstraints(
      @NonNull AnnotationSpec.Builder builder,
      @NonNull IModelDefinition definition) {

    List<? extends IAllowedValuesConstraint> allowedValues = definition.getAllowedValuesConstraints();
    List<? extends IIndexHasKeyConstraint> indexHasKey = definition.getIndexHasKeyConstraints();
    List<? extends IMatchesConstraint> matches = definition.getMatchesConstraints();
    List<? extends IExpectConstraint> expects = definition.getExpectConstraints();

    if (!allowedValues.isEmpty() || !indexHasKey.isEmpty() || !matches.isEmpty() || !expects.isEmpty()) {
      AnnotationSpec.Builder annotation = AnnotationSpec.builder(ValueConstraints.class);

      applyAllowedValuesConstraints(annotation, allowedValues);
      applyIndexHasKeyConstraints(annotation, indexHasKey);
      applyMatchesConstraints(annotation, matches);
      applyExpectConstraints(annotation, expects);

      builder.addMember("valueConstraints", "$L", annotation.build());
    }
  }

  public static void buildAssemblyConstraints(
      @NonNull AnnotationSpec.Builder builder,
      @NonNull IAssemblyDefinition definition) {

    List<? extends IIndexConstraint> index = definition.getIndexConstraints();
    List<? extends IUniqueConstraint> unique = definition.getUniqueConstraints();
    List<? extends ICardinalityConstraint> cardinality = definition.getHasCardinalityConstraints();

    if (!index.isEmpty() || !unique.isEmpty() || !cardinality.isEmpty()) {
      AnnotationSpec.Builder annotation = ObjectUtils.notNull(AnnotationSpec.builder(AssemblyConstraints.class));

      applyIndexConstraints(annotation, index);
      applyUniqueConstraints(annotation, unique);
      applyHasCardinalityConstraints(definition, annotation, cardinality);

      builder.addMember("modelConstraints", "$L", annotation.build());
    }
  }

  private static void applyAllowedValuesConstraints(AnnotationSpec.Builder annotation,
      List<? extends IAllowedValuesConstraint> constraints) {
    for (IAllowedValuesConstraint constraint : constraints) {
      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(AllowedValues.class);
      buildConstraint(AllowedValues.class, constraintAnnotation, constraint);

      boolean isAllowedOther = constraint.isAllowedOther();
      if (isAllowedOther != (boolean) getDefaultValue(AllowedValues.class, "allowOthers")) {
        constraintAnnotation.addMember("allowOthers", "$L", isAllowedOther);
      }

      for (IAllowedValue value : constraint.getAllowedValues().values()) {
        AnnotationSpec.Builder valueAnnotation = AnnotationSpec.builder(AllowedValue.class);

        valueAnnotation.addMember("value", "$S", value.getValue());
        valueAnnotation.addMember("description", "$S", value.getDescription().toMarkdown());

        constraintAnnotation.addMember("values", "$L", valueAnnotation.build());
      }

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        constraintAnnotation.addMember("remarks", "$S", remarks.toMarkdown());
      }
      annotation.addMember("allowedValues", "$L", constraintAnnotation.build());
    }
  }

  private static void applyIndexHasKeyConstraints(AnnotationSpec.Builder annotation,
      List<? extends IIndexHasKeyConstraint> constraints) {
    for (IIndexHasKeyConstraint constraint : constraints) {
      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(IndexHasKey.class);
      buildConstraint(IndexHasKey.class, constraintAnnotation, constraint);

      constraintAnnotation.addMember("indexName", "$S", constraint.getIndexName());

      buildKeyFields(constraintAnnotation, constraint.getKeyFields());

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        constraintAnnotation.addMember("remarks", "$S", remarks.toMarkdown());
      }

      annotation.addMember("indexHasKey", "$L", constraintAnnotation.build());
    }
  }

  private static void buildKeyFields(@NonNull Builder constraintAnnotation,
      @NonNull List<? extends IKeyField> keyFields) {
    for (IKeyField key : keyFields) {
      AnnotationSpec.Builder keyAnnotation = AnnotationSpec.builder(KeyField.class);

      String target = key.getTarget();
      if (!target.equals(getDefaultValue(KeyField.class, "target"))) {
        keyAnnotation.addMember("target", "$S", target);
      }

      Pattern pattern = key.getPattern();
      if (pattern != null) {
        keyAnnotation.addMember("pattern", "$S", pattern.pattern());
      }

      MarkupMultiline remarks = key.getRemarks();
      if (remarks != null) {
        keyAnnotation.addMember("remarks", "$S", remarks.toMarkdown());
      }

      constraintAnnotation.addMember("keyFields", "$L", keyAnnotation.build());
    }
  }

  private static void applyMatchesConstraints(AnnotationSpec.Builder annotation,
      List<? extends IMatchesConstraint> constraints) {
    for (IMatchesConstraint constraint : constraints) {
      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(Matches.class);
      buildConstraint(Matches.class, constraintAnnotation, constraint);

      Pattern pattern = constraint.getPattern();
      if (pattern != null) {
        constraintAnnotation.addMember("pattern", "$S", pattern.pattern());
      }

      IDataTypeAdapter<?> dataType = constraint.getDataType();
      if (dataType != null) {
        constraintAnnotation.addMember("typeAdapter", "$T.class", dataType.getClass());
      }

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        constraintAnnotation.addMember("remarks", "$S", remarks.toMarkdown());
      }
      annotation.addMember("matches", "$L", constraintAnnotation.build());
    }
  }

  private static void applyExpectConstraints(AnnotationSpec.Builder annotation,
      List<? extends IExpectConstraint> constraints) {
    for (IExpectConstraint constraint : constraints) {
      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(Expect.class);

      buildConstraint(Expect.class, constraintAnnotation, constraint);

      constraintAnnotation.addMember("test", "$S", constraint.getTest());

      if (constraint.getMessage() != null) {
        constraintAnnotation.addMember("message", "$S", constraint.getMessage());
      }

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        constraintAnnotation.addMember("remarks", "$S", remarks.toMarkdown());
      }

      annotation.addMember("expect", "$L", constraintAnnotation.build());
    }
  }

  private static void applyIndexConstraints(AnnotationSpec.Builder annotation,
      List<? extends IIndexConstraint> constraints) {
    for (IIndexConstraint constraint : constraints) {
      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(Index.class);

      buildConstraint(Index.class, constraintAnnotation, constraint);

      constraintAnnotation.addMember("name", "$S", constraint.getName());

      buildKeyFields(constraintAnnotation, constraint.getKeyFields());

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        constraintAnnotation.addMember("remarks", "$S", remarks.toMarkdown());
      }

      annotation.addMember("index", "$L", constraintAnnotation.build());
    }
  }

  private static void applyUniqueConstraints(AnnotationSpec.Builder annotation,
      List<? extends IUniqueConstraint> constraints) {
    for (IUniqueConstraint constraint : constraints) {
      AnnotationSpec.Builder constraintAnnotation = ObjectUtils.notNull(AnnotationSpec.builder(IsUnique.class));

      buildConstraint(IsUnique.class, constraintAnnotation, constraint);

      buildKeyFields(constraintAnnotation, constraint.getKeyFields());

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        constraintAnnotation.addMember("remarks", "$S", remarks.toMarkdown());
      }

      annotation.addMember("unique", "$L", constraintAnnotation.build());
    }
  }

  @SuppressWarnings({
      "PMD.GuardLogStatement" // guarded in outer calls
  })
  private static void checkCardinalities(
      @NonNull IAssemblyDefinition definition,
      @NonNull ICardinalityConstraint constraint,
      @NonNull ISequence<? extends IDefinitionNodeItem<?, ?>> instanceSet,
      @NonNull LogBuilder logBuilder) {

    LogBuilder warn = LOGGER.atWarn();
    for (IDefinitionNodeItem<?, ?> item : instanceSet.getValue()) {
      INamedInstance instance = item.getInstance();
      if (instance instanceof INamedModelInstanceAbsolute) {
        INamedModelInstanceAbsolute modelInstance = (INamedModelInstanceAbsolute) instance;

        checkMinOccurs(definition, constraint, modelInstance, logBuilder);
        checkMaxOccurs(definition, constraint, modelInstance, logBuilder);
      } else {
        warn.log(String.format(
            "Definition '%s' has min-occurs=%d cardinality constraint targeting '%s' that is not a model instance",
            definition.getName(), constraint.getMinOccurs(), constraint.getTarget()));
      }
    }
  }

  @SuppressWarnings({
      "PMD.GuardLogStatement" // guarded in outer calls
  })
  private static void checkMinOccurs(
      @NonNull IAssemblyDefinition definition,
      @NonNull ICardinalityConstraint constraint,
      @NonNull INamedModelInstanceAbsolute modelInstance,
      @NonNull LogBuilder logBuilder) {
    Integer minOccurs = constraint.getMinOccurs();
    if (minOccurs != null) {
      if (minOccurs == modelInstance.getMinOccurs()) {
        logBuilder.log(String.format(
            "Definition '%s' has min-occurs=%d cardinality constraint targeting '%s' that is redundant with a"
                + " targeted instance named '%s' that requires min-occurs=%d",
            definition.getName(), minOccurs, constraint.getTarget(),
            modelInstance.getName(),
            modelInstance.getMinOccurs()));
      } else if (minOccurs < modelInstance.getMinOccurs()) {
        logBuilder.log(String.format(
            "Definition '%s' has min-occurs=%d cardinality constraint targeting '%s' that conflicts with a"
                + " targeted instance named '%s' that requires min-occurs=%d",
            definition.getName(), minOccurs, constraint.getTarget(),
            modelInstance.getName(),
            modelInstance.getMinOccurs()));
      }
    }
  }

  @SuppressWarnings({
      "PMD.GuardLogStatement" // guarded in outer calls
  })
  private static void checkMaxOccurs(
      @NonNull IAssemblyDefinition definition,
      @NonNull ICardinalityConstraint constraint,
      @NonNull INamedModelInstanceAbsolute modelInstance,
      @NonNull LogBuilder logBuilder) {
    Integer maxOccurs = constraint.getMaxOccurs();
    if (maxOccurs != null) {
      if (maxOccurs == modelInstance.getMaxOccurs()) {
        logBuilder.log(String.format(
            "Definition '%s' has max-occurs=%d cardinality constraint targeting '%s' that is redundant with a"
                + " targeted instance named '%s' that requires max-occurs=%d",
            definition.getName(), maxOccurs, constraint.getTarget(),
            modelInstance.getName(),
            modelInstance.getMaxOccurs()));
      } else if (maxOccurs < modelInstance.getMaxOccurs()) {
        logBuilder.log(String.format(
            "Definition '%s' has max-occurs=%d cardinality constraint targeting '%s' that conflicts with a"
                + " targeted instance named '%s' that requires max-occurs=%d",
            definition.getName(), maxOccurs, constraint.getTarget(),
            modelInstance.getName(),
            modelInstance.getMaxOccurs()));
      }
    }
  }

  private static void applyHasCardinalityConstraints(
      @NonNull IAssemblyDefinition definition,
      @NonNull AnnotationSpec.Builder annotation,
      @NonNull List<? extends ICardinalityConstraint> constraints) {

    DynamicContext dynamicContext = new DynamicContext();
    dynamicContext.disablePredicateEvaluation();

    for (ICardinalityConstraint constraint : constraints) {

      IAssemblyNodeItem definitionNodeItem
          = INodeItemFactory.instance().newAssemblyNodeItem(definition);

      ISequence<? extends IDefinitionNodeItem<?, ?>> instanceSet
          = constraint.matchTargets(definitionNodeItem, dynamicContext);

      if (LOGGER.isWarnEnabled()) {
        checkCardinalities(definition, constraint, instanceSet, ObjectUtils.notNull(LOGGER.atWarn()));
      }

      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(HasCardinality.class);

      buildConstraint(HasCardinality.class, constraintAnnotation, constraint);

      Integer minOccurs = constraint.getMinOccurs();
      if (minOccurs != null && !minOccurs.equals(getDefaultValue(HasCardinality.class, "minOccurs"))) {
        constraintAnnotation.addMember("minOccurs", "$L", minOccurs);
      }

      Integer maxOccurs = constraint.getMaxOccurs();
      if (maxOccurs != null && !maxOccurs.equals(getDefaultValue(HasCardinality.class, "maxOccurs"))) {
        constraintAnnotation.addMember("maxOccurs", "$L", maxOccurs);
      }

      annotation.addMember("cardinality", "$L", constraintAnnotation.build());

      MarkupMultiline remarks = constraint.getRemarks();
      if (remarks != null) {
        constraintAnnotation.addMember("remarks", "$S", remarks.toMarkdown());
      }
    }
  }
}
