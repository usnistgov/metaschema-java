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

package gov.nist.secauto.metaschema.core.model.constraint.impl;

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.constraint.AbstractTargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;

import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides an base implementation for a set of constraints that target a given
 * definition using a target Metapath expression.
 *
 * @param <T>
 *          the Java type of the definition target
 *
 * @param <S>
 *          the Java type of the constraint container
 */
public abstract class AbstractDefinitionTargetedConstraints<
    T extends IDefinition,
    S extends IValueConstrained>
    extends AbstractTargetedConstraints<S> {

  /**
   * Construct a new set of targeted constraints.
   *
   * @param target
   *          the Metapath expression that can be used to find matching targets
   * @param constraints
   *          the constraints to apply to matching targets
   */
  protected AbstractDefinitionTargetedConstraints(
      @NonNull String target,
      @NonNull S constraints) {
    super(target, constraints);
  }

  /**
   * Apply the constraints to the provided {@code definition}.
   * <p>
   * This will be called when a definition is found that matches the target
   * expression.
   *
   * @param definition
   *          the definition to apply the constraints to.
   */
  protected void applyTo(@NonNull T definition) {
    getAllowedValuesConstraints().forEach(definition::addConstraint);
    getMatchesConstraints().forEach(definition::addConstraint);
    getIndexHasKeyConstraints().forEach(definition::addConstraint);
    getExpectConstraints().forEach(definition::addConstraint);
  }

  @Override
  public void target(@NonNull IFlagDefinition definition) {
    throw new IllegalStateException(
        String.format("The targeted definition '%s' from metaschema '%s' is not a %s definition.",
            definition.getEffectiveName(),
            definition.getContainingModule().getQName().toString(),
            definition.getModelType().name().toLowerCase(Locale.ROOT)));
  }

  @Override
  public void target(@NonNull IFieldDefinition definition) {
    throw new IllegalStateException(
        String.format("The targeted definition '%s' from metaschema '%s' is not a %s definition.",
            definition.getEffectiveName(),
            definition.getContainingModule().getQName().toString(),
            definition.getModelType().name().toLowerCase(Locale.ROOT)));
  }

  @Override
  public void target(@NonNull IAssemblyDefinition definition) {
    throw new IllegalStateException(
        String.format("The targeted definition '%s' from metaschema '%s' is not a %s definition.",
            definition.getEffectiveName(),
            definition.getContainingModule().getQName().toString(),
            definition.getModelType().name().toLowerCase(Locale.ROOT)));
  }
}
