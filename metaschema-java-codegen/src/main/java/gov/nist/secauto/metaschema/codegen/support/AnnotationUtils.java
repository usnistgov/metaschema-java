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

package gov.nist.secauto.metaschema.codegen.support;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.AnnotationSpec.Builder;

import gov.nist.secauto.metaschema.binding.model.annotations.constraint.AllowedValue;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.AllowedValues;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Expect;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.HasCardinality;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Index;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.IndexHasKey;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.IsUnique;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.KeyField;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Matches;
import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IInstance;
import gov.nist.secauto.metaschema.model.common.IModelInstance;
import gov.nist.secauto.metaschema.model.common.INamedInstance;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValue;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IKeyField;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance.IInstanceSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public final class AnnotationUtils {
  private static final Logger LOGGER = LogManager.getLogger(AnnotationUtils.class);

  private AnnotationUtils() {
    // disable construction
  }

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
      retval = null;
    }
    return retval;
  }

  private static void buildConstraint(Class<?> annotationType, AnnotationSpec.Builder annotation,
      IConstraint constraint) {
    String id = constraint.getId();
    if (id != null) {
      annotation.addMember("id", "$S", id);
    }

    annotation.addMember("level", "$T.$L", IConstraint.Level.class, constraint.getLevel());

    MetapathExpression target = constraint.getTarget();
    String path = target.getPath();
    if (!path.equals(getDefaultValue(annotationType, "target"))) {
      annotation.addMember("target", "$S", path);
    }
  }

  public static void applyAllowedValuesConstraints(AnnotationSpec.Builder annotation,
      List<@NotNull ? extends IAllowedValuesConstraint> constraints) {
    for (IAllowedValuesConstraint constraint : constraints) {
      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(AllowedValues.class);
      buildConstraint(AllowedValues.class, constraintAnnotation, constraint);

      boolean isAllowedOther = constraint.isAllowedOther();
      if (!Boolean.valueOf(isAllowedOther).equals(getDefaultValue(AllowedValues.class, "allowOthers"))) {
        constraintAnnotation.addMember("allowOthers", "$L", isAllowedOther);
      }

      for (IAllowedValue value : constraint.getAllowedValues().values()) {
        AnnotationSpec.Builder valueAnnotation = AnnotationSpec.builder(AllowedValue.class);

        valueAnnotation.addMember("value", "$S", value.getValue());
        valueAnnotation.addMember("description", "$S", value.getDescription().toMarkdown());

        constraintAnnotation.addMember("values", "$L", valueAnnotation.build());
      }
      annotation.addMember("allowedValues", "$L", constraintAnnotation.build());
    }
  }

  public static void applyIndexHasKeyConstraints(AnnotationSpec.Builder annotation,
      List<@NotNull ? extends IIndexHasKeyConstraint> constraints) {
    for (IIndexHasKeyConstraint constraint : constraints) {
      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(IndexHasKey.class);
      buildConstraint(IndexHasKey.class, constraintAnnotation, constraint);

      constraintAnnotation.addMember("indexName", "$S", constraint.getIndexName());

      buildKeyFields(constraintAnnotation, constraint.getKeyFields());

      annotation.addMember("indexHasKey", "$L", constraintAnnotation.build());
    }
  }

  private static void buildKeyFields(@NotNull Builder constraintAnnotation,
      @NotNull List<@NotNull ? extends IKeyField> keyFields) {
    for (IKeyField key : keyFields) {
      AnnotationSpec.Builder keyAnnotation = AnnotationSpec.builder(KeyField.class);

      MetapathExpression target = key.getTarget();
      String path = target.getPath();
      if (!path.equals(getDefaultValue(KeyField.class, "target"))) {
        keyAnnotation.addMember("target", "$S", path);
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

  public static void applyMatchesConstraints(AnnotationSpec.Builder annotation,
      List<@NotNull ? extends IMatchesConstraint> constraints) {
    for (IMatchesConstraint constraint : constraints) {
      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(Matches.class);
      buildConstraint(Matches.class, constraintAnnotation, constraint);

      Pattern pattern = constraint.getPattern();
      if (pattern != null) {
        constraintAnnotation.addMember("pattern", "$S", pattern.pattern());
      }

      IJavaTypeAdapter<?> dataType = constraint.getDataType();
      if (dataType != null) {
        constraintAnnotation.addMember("typeAdapter", "$T.class", dataType.getClass());
      }
      annotation.addMember("matches", "$L", constraintAnnotation.build());
    }
  }

  public static void applyExpectConstraints(AnnotationSpec.Builder annotation,
      List<@NotNull ? extends IExpectConstraint> constraints) {
    for (IExpectConstraint constraint : constraints) {
      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(Expect.class);

      buildConstraint(Expect.class, constraintAnnotation, constraint);

      MetapathExpression test = constraint.getTest();
      constraintAnnotation.addMember("test", "$S", test.getPath());

      if (constraint.getMessage() != null) {
        constraintAnnotation.addMember("message", "$S", constraint.getMessage());
      }

      annotation.addMember("expect", "$L", constraintAnnotation.build());
    }
  }

  public static void applyIndexConstraints(AnnotationSpec.Builder annotation,
      List<@NotNull ? extends IIndexConstraint> constraints) {
    for (IIndexConstraint constraint : constraints) {
      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(Index.class);

      buildConstraint(Index.class, constraintAnnotation, constraint);

      constraintAnnotation.addMember("name", "$S", constraint.getName());

      buildKeyFields(constraintAnnotation, constraint.getKeyFields());

      annotation.addMember("index", "$L", constraintAnnotation.build());
    }
  }

  public static void applyUniqueConstraints(AnnotationSpec.Builder annotation,
      List<@NotNull ? extends IUniqueConstraint> constraints) {
    for (IUniqueConstraint constraint : constraints) {
      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(IsUnique.class);

      buildConstraint(IsUnique.class, constraintAnnotation, constraint);

      buildKeyFields(constraintAnnotation, constraint.getKeyFields());

      annotation.addMember("isUnique", "$L", constraintAnnotation.build());
    }
  }

  public static void applyHasCardinalityConstraints(IAssemblyDefinition definition, AnnotationSpec.Builder annotation,
      List<@NotNull ? extends ICardinalityConstraint> constraints) {
    for (ICardinalityConstraint constraint : constraints) {
      Integer minOccurs = constraint.getMinOccurs();
      Integer maxOccurs = constraint.getMaxOccurs();

      IInstanceSet instanceSet = definition.evaluateMetapathInstances(constraint.getTarget());
      Collection<? extends IInstance> instances = instanceSet.getInstances();
      if (instances.size() == 1) {
        IInstance instance = instances.iterator().next();
        if (instance instanceof IModelInstance) {
          IModelInstance modelInstance = (IModelInstance) instance;

          if (minOccurs != null) {
            if (minOccurs == modelInstance.getMinOccurs()) {
              if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(String.format(
                    "Definition '%s' has min-occurs=%d cardinality constraint targeting '%s' that is redundant with a"
                        + " targeted instance named '%s' that requires min-occurs=%d",
                    definition.getName(), minOccurs, constraint.getTarget().getPath(),
                    modelInstance instanceof INamedInstance ? ((INamedInstance) modelInstance).getName() : "no name",
                    modelInstance.getMinOccurs()));
              }
            } else if (minOccurs < modelInstance.getMinOccurs()) {
              if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(String.format(
                    "Definition '%s' has min-occurs=%d cardinality constraint targeting '%s' that conflicts with a"
                        + " targeted instance named '%s' that requires min-occurs=%d",
                    definition.getName(), minOccurs, constraint.getTarget().getPath(),
                    modelInstance instanceof INamedInstance ? ((INamedInstance) modelInstance).getName() : "no name",
                    modelInstance.getMinOccurs()));
              }
            }
          }

          if (maxOccurs != null) {
            if (maxOccurs == modelInstance.getMaxOccurs()) {
              if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(String.format(
                    "Definition '%s' has max-occurs=%d cardinality constraint targeting '%s' that is redundant with a"
                        + " targeted instance named '%s' that requires max-occurs=%d",
                    definition.getName(), maxOccurs, constraint.getTarget().getPath(),
                    modelInstance instanceof INamedInstance ? ((INamedInstance) modelInstance).getName() : "no name",
                    modelInstance.getMaxOccurs()));
              }
            } else if (maxOccurs < modelInstance.getMaxOccurs()) {
              if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(String.format(
                    "Definition '%s' has max-occurs=%d cardinality constraint targeting '%s' that conflicts with a"
                        + " targeted instance named '%s' that requires max-occurs=%d",
                    definition.getName(), maxOccurs, constraint.getTarget().getPath(),
                    modelInstance instanceof INamedInstance ? ((INamedInstance) modelInstance).getName() : "no name",
                    modelInstance.getMaxOccurs()));
              }
            }
          }
        } else if (LOGGER.isWarnEnabled()) {
          LOGGER.warn(String.format(
              "Definition '%s' has min-occurs=%d cardinality constraint targeting '%s' that is not a model instance",
              definition.getName(), minOccurs, constraint.getTarget().getPath()));
        }
      }

      AnnotationSpec.Builder constraintAnnotation = AnnotationSpec.builder(HasCardinality.class);

      buildConstraint(HasCardinality.class, constraintAnnotation, constraint);

      if (minOccurs != null && !minOccurs.equals(getDefaultValue(HasCardinality.class, "minOccurs"))) {
        constraintAnnotation.addMember("minOccurs", "$L", minOccurs);
      }

      if (maxOccurs != null && !maxOccurs.equals(getDefaultValue(HasCardinality.class, "maxOccurs"))) {
        constraintAnnotation.addMember("maxOccurs", "$L", maxOccurs);
      }

      annotation.addMember("hasCardinality", "$L", constraintAnnotation.build());
    }
  }
}
