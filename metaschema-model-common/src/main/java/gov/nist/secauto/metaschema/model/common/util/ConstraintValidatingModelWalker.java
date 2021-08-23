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
package gov.nist.secauto.metaschema.model.common.util;

import gov.nist.secauto.metaschema.metapath.ConstraintVisitingModelWalker;
import gov.nist.secauto.metaschema.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.metapath.evaluate.DefaultMetaschemaContext;
import gov.nist.secauto.metaschema.metapath.evaluate.IInstanceSet;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValue;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.instance.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class ConstraintValidatingModelWalker extends ConstraintVisitingModelWalker<ConstraintValidatingModelWalker> {
  private static final Logger logger = LogManager.getLogger(ConstraintValidatingModelWalker.class);

  private final Set<IAssemblyDefinition> seenAssemblies = new HashSet<>();
  private final Deque<IAssemblyDefinition> stack = new LinkedList<>();
  private int depth = 0;

  protected String getPadding() {
    return "  ".repeat(depth);
  }

  @Override
  public void walk(IFlagInstance instance, ConstraintValidatingModelWalker data) {
    ++depth;
    super.walk(instance, data);
    --depth;
  }

  @Override
  public void walk(IFieldInstance instance, ConstraintValidatingModelWalker data) {
    ++depth;
    super.walk(instance, data);
    --depth;
  }

  @Override
  public void walk(IAssemblyInstance instance, ConstraintValidatingModelWalker data) {
    ++depth;
    super.walk(instance, data);
    --depth;
  }

  @Override
  public void walk(IAssemblyDefinition assembly, ConstraintValidatingModelWalker data) {
    if (!seenAssemblies.contains(assembly)) {
      if (stack.contains(assembly)) {
        logger.info(String.format("%sCycle", getPadding()));
      } else {
        stack.push(assembly);
        super.walk(assembly, data);
        stack.pop();
      }
    } else {
      seenAssemblies.add(assembly);
    }
  }

  @Override
  protected boolean visit(IChoiceInstance instance, ConstraintValidatingModelWalker data) {
    return false;
  }

  @Override
  protected boolean visit(IAssemblyDefinition def, ConstraintValidatingModelWalker data) {
    if (def.isRoot()) {
      logger.info(String.format("%sRoot(%s)", getPadding(), def.getName()));
    }
    return super.visit(def, data);
  }

  @Override
  protected boolean visit(IFlagInstance instance, ConstraintValidatingModelWalker data) {
    logger.info(String.format("%sFlag(%s)%s", getPadding(), instance.getName(), instance.getEffectiveName()));
    return false;
  }

  @Override
  protected boolean visit(IFieldInstance instance, ConstraintValidatingModelWalker data) {
    logger.info(String.format("%sField(%s)%s", getPadding(), instance.getName(), instance.getEffectiveName()));
    return true;
  }

  @Override
  protected boolean visit(IAssemblyInstance instance, ConstraintValidatingModelWalker data) {
    logger.info(String.format("%sAssembly(%s)%s", getPadding(), instance.getName(), instance.getEffectiveName()));
    return true;
  }

  private void logAllowedValuesConstraint(IAllowedValuesConstraint constraint, ConstraintValidatingModelWalker data) {
    logger.info(String.format("%s  %s: %s", getPadding(), constraint.getId(), constraint.getTarget().getPath()));
    for (IAllowedValue value : constraint.getAllowedValues().values()) {
      logger.info(String.format("%s    %s: %s", getPadding(), value.getValue(), value.getDescription().toMarkdown()));
    }
  }

  protected void validateConstraintTarget(IConstraint constraint, IInstanceSet instanceSet, ConstraintValidatingModelWalker data) {
    MetapathExpression metapath = constraint.getTarget();
    try {
      if (metapath.evaluateMetaschemaInstance(new DefaultMetaschemaContext(instanceSet)).getInstances().isEmpty()) {
        logger.error(String.format("%s    Path '%s' did not match", getPadding(), metapath.getPath()));
      }
    } catch (RuntimeException ex) {
      logger.error(String.format("%s    Path '%s' failed to evaluate", getPadding(), metapath.getPath()), ex);
    }
  }

  @Override
  protected void visit(IFlagDefinition def, IAllowedValuesConstraint constraint, ConstraintValidatingModelWalker data) {
    logAllowedValuesConstraint(constraint, data);
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IFlagDefinition def, IMatchesConstraint constraint, ConstraintValidatingModelWalker data) {
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IFlagDefinition def, IExpectConstraint constraint, ConstraintValidatingModelWalker data) {
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IFlagDefinition def, IIndexHasKeyConstraint constraint, ConstraintValidatingModelWalker data) {
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IFieldDefinition def, IAllowedValuesConstraint constraint,
      ConstraintValidatingModelWalker data) {
    logAllowedValuesConstraint(constraint, data);
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IFieldDefinition def, IMatchesConstraint constraint, ConstraintValidatingModelWalker data) {
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IFieldDefinition def, IExpectConstraint constraint, ConstraintValidatingModelWalker data) {
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IFieldDefinition def, IIndexHasKeyConstraint constraint, ConstraintValidatingModelWalker data) {
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IAssemblyDefinition def, IAllowedValuesConstraint constraint,
      ConstraintValidatingModelWalker data) {
    logAllowedValuesConstraint(constraint, data);
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IAssemblyDefinition def, IMatchesConstraint constraint, ConstraintValidatingModelWalker data) {
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IAssemblyDefinition def, IExpectConstraint constraint, ConstraintValidatingModelWalker data) {
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IAssemblyDefinition def, IUniqueConstraint constraint, ConstraintValidatingModelWalker data) {
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IAssemblyDefinition def, IIndexConstraint constraint, ConstraintValidatingModelWalker data) {
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IAssemblyDefinition def, IIndexHasKeyConstraint constraint,
      ConstraintValidatingModelWalker data) {
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }

  @Override
  protected void visit(IAssemblyDefinition def, ICardinalityConstraint constraint,
      ConstraintValidatingModelWalker data) {
    validateConstraintTarget(constraint, IInstanceSet.newInstanceSet(def), data);
  }
}
