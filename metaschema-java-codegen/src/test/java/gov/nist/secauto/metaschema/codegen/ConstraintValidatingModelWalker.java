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

package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValue;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IKeyField;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.instance.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.metapath.ConstraintVisitingModelWalker;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance.DefaultMetaschemaContext;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance.IInstanceSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

public class ConstraintValidatingModelWalker
    extends ConstraintVisitingModelWalker<Void> {
  private static final Logger LOGGER = LogManager.getLogger(ConstraintValidatingModelWalker.class);

  private final Set<IAssemblyDefinition> seenAssemblies = new HashSet<>();
  private final Deque<IAssemblyDefinition> stack = new LinkedList<>();
  private int depth; // 0;

  @Override
  protected Void getDefaultData() {
    return null;
  }

  protected String getPadding() {
    return "  ".repeat(depth);
  }

  @Override
  public void walk(IFlagInstance instance, Void data) {
    ++depth;
    super.walk(instance, data);
    --depth;
  }

  @Override
  public void walk(IFieldInstance instance, Void data) {
    ++depth;
    super.walk(instance, data);
    --depth;
  }

  @Override
  public void walk(IAssemblyInstance instance, Void data) {
    ++depth;
    super.walk(instance, data);
    --depth;
  }

  @Override
  public void walk(IAssemblyDefinition assembly, Void data) {
    if (seenAssemblies.contains(assembly)) {
      seenAssemblies.add(assembly);
    } else {
      if (stack.contains(assembly)) {
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info(String.format("%sCycle", getPadding()));
        }
      } else {
        stack.push(assembly);
        super.walk(assembly, data);
        stack.pop();
      }
    }
  }

  @Override
  protected boolean visit(IChoiceInstance instance, Void data) {
    return false;
  }

  @Override
  protected boolean visit(IAssemblyDefinition def, Void data) {
    if (def.isRoot() && LOGGER.isDebugEnabled()) {
      LOGGER.debug(String.format("%sRoot(%s)", getPadding(), def.getName()));
    }
    return super.visit(def, data);
  }

  @Override
  protected boolean visit(IFlagInstance instance, Void data) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(String.format("%sFlag(%s)%s", getPadding(), instance.getName(), instance.getEffectiveName()));
    }
    return false;
  }

  @Override
  protected boolean visit(IFieldInstance instance, Void data) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(String.format("%sField(%s)%s", getPadding(), instance.getName(), instance.getEffectiveName()));
    }
    return true;
  }

  @Override
  protected boolean visit(IAssemblyInstance instance, Void data) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(String.format("%sAssembly(%s)%s", getPadding(), instance.getName(), instance.getEffectiveName()));
    }
    return true;
  }

  @Override
  protected void visit(IFlagDefinition def, IAllowedValuesConstraint constraint, Void data) {
    logAllowedValuesConstraint(constraint);
    validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
  }

  @Override
  protected void visit(IFlagDefinition def, IMatchesConstraint constraint, Void data) {
    validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
  }

  @Override
  protected void visit(IFlagDefinition def, IExpectConstraint constraint, Void data) {
    IInstanceSet result
        = validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
    validateExpectTest(def, constraint, result);
  }

  @Override
  protected void visit(IFlagDefinition def, IIndexHasKeyConstraint constraint, Void data) {
    IInstanceSet result
        = validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
    validateKeys(def, constraint, result);
  }

  @Override
  protected void visit(IFieldDefinition def, IAllowedValuesConstraint constraint, Void data) {
    logAllowedValuesConstraint(constraint);
    validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
  }

  @Override
  protected void visit(IFieldDefinition def, IMatchesConstraint constraint, Void data) {
    validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
  }

  @Override
  protected void visit(IFieldDefinition def, IExpectConstraint constraint, Void data) {
    IInstanceSet result
        = validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
    validateExpectTest(def, constraint, result);
  }

  @Override
  protected void visit(IFieldDefinition def, IIndexHasKeyConstraint constraint, Void data) {
    IInstanceSet result
        = validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
    validateKeys(def, constraint, result);
  }

  @Override
  protected void visit(IAssemblyDefinition def, IAllowedValuesConstraint constraint, Void data) {
    logAllowedValuesConstraint(constraint);
    validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
  }

  @Override
  protected void visit(IAssemblyDefinition def, IMatchesConstraint constraint, Void data) {
    validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
  }

  @Override
  protected void visit(IAssemblyDefinition def, IExpectConstraint constraint, Void data) {
    IInstanceSet result
        = validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
    validateExpectTest(def, constraint, result);
  }

  @Override
  protected void visit(IAssemblyDefinition def, IUniqueConstraint constraint, Void data) {
    IInstanceSet result
        = validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
    validateKeys(def, constraint, result);
  }

  @Override
  protected void visit(IAssemblyDefinition def, IIndexConstraint constraint, Void data) {
    IInstanceSet result
        = validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
    validateKeys(def, constraint, result);
  }

  @Override
  protected void visit(IAssemblyDefinition def, IIndexHasKeyConstraint constraint, Void data) {
    IInstanceSet result
        = validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
    validateKeys(def, constraint, result);
  }

  @Override
  protected void visit(IAssemblyDefinition def, ICardinalityConstraint constraint, Void data) {
    validateConstraintTarget(def, constraint, IInstanceSet.newInstanceSet(def));
  }

  private void logAllowedValuesConstraint(IAllowedValuesConstraint constraint) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(String.format("%s  allowed-values(%s:%s): %s", getPadding(), constraint.getId(),
          constraint.isAllowedOther(), constraint.getTarget().getPath()));
    }
    if (LOGGER.isTraceEnabled()) {
      for (IAllowedValue value : constraint.getAllowedValues().values()) {
        LOGGER
            .trace(String.format("%s    %s: %s", getPadding(), value.getValue(), value.getDescription().toMarkdown()));
      }
    }
  }

  protected IInstanceSet validateConstraintTarget(IDefinition definition, IConstraint constraint,
      IInstanceSet instanceSet) {
    MetapathExpression metapath = constraint.getTarget();
    IInstanceSet retval;
    try {
      IInstanceSet result = metapath.evaluateMetaschemaInstance(new DefaultMetaschemaContext(instanceSet));
      if (result.getInstances().isEmpty() && LOGGER.isErrorEnabled()) {
        LOGGER.error(String.format("Path '%s' did not match in %s definition '%s'", metapath.getPath(),
            definition.getModelType().name().toLowerCase(Locale.ROOT), definition.getName()));
      }
      retval = result;
    } catch (RuntimeException ex) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error(String.format("Path '%s' failed to evaluate in %s definition '%s'", metapath.getPath(),
            definition.getModelType().name().toLowerCase(Locale.ROOT), definition.getName()), ex);
      }
      retval = IInstanceSet.EMPTY_INSTANCE_SET;
    }
    return retval;
  }

  private void validateExpectTest(IDefinition definition, IExpectConstraint constraint, IInstanceSet instanceSet) {
    MetapathExpression test = constraint.getTest();
    try {
      IInstanceSet result = test.evaluateMetaschemaInstance(new DefaultMetaschemaContext(instanceSet));
      if (result.getInstances().isEmpty() && LOGGER.isErrorEnabled()) {
        LOGGER.error(String.format("Expect test path '%s' did not match in %s definition '%s'", test.getPath(),
            definition.getModelType().name().toLowerCase(Locale.ROOT), definition.getName()));
      }
    } catch (RuntimeException ex) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error(String.format("Expect test path '%s' failed to evaluate in %s definition '%s'", test.getPath(),
            definition.getModelType().name().toLowerCase(Locale.ROOT), definition.getName()), ex);
      }
    }
  }

  private void validateKeys(IDefinition definition, IKeyConstraint constraint, IInstanceSet targets) {
    for (IKeyField keyField : constraint.getKeyFields()) {
      MetapathExpression keyTarget = keyField.getTarget();
      try {
        IInstanceSet result = keyTarget.evaluateMetaschemaInstance(
            new DefaultMetaschemaContext(targets)); // NOPMD - intentional
        if (result.getInstances().isEmpty() && LOGGER.isErrorEnabled()) {
          LOGGER.error(String.format("KeyField path '%s' did not match in %s definition '%s'", keyTarget.getPath(),
              definition.getModelType().name().toLowerCase(Locale.ROOT), definition.getName()));
        }
      } catch (RuntimeException ex) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error(String.format("KeyField path '%s' failed to evaluate in %s definition '%s'", keyTarget.getPath(),
              definition.getModelType().name().toLowerCase(Locale.ROOT), definition.getName()), ex);
        }
      }
    }

  }

}
