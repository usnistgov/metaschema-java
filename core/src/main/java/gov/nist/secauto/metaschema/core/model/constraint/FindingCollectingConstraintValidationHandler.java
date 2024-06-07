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

package gov.nist.secauto.metaschema.core.model.constraint;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.Level;
import gov.nist.secauto.metaschema.core.model.validation.IValidationResult;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import java.util.LinkedList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public class FindingCollectingConstraintValidationHandler
    extends AbstractConstraintValidationHandler
    implements IValidationResult {
  @NonNull
  private final List<ConstraintValidationFinding> findings = new LinkedList<>();
  @NonNull
  private Level highestLevel = IConstraint.Level.INFORMATIONAL;

  @Override
  @NonNull
  public List<ConstraintValidationFinding> getFindings() {
    return CollectionUtil.unmodifiableList(findings);
  }

  @Override
  @NonNull
  public Level getHighestSeverity() {
    return highestLevel;
  }

  /**
   * Add a finding to the collection of findings maintained by this instance.
   *
   * @param finding
   *          the finding to add
   */
  protected void addFinding(@NonNull ConstraintValidationFinding finding) {
    findings.add(finding);

    Level severity = finding.getSeverity();
    if (severity.ordinal() > highestLevel.ordinal()) {
      highestLevel = severity;
    }
  }

  @Override
  public void handleCardinalityMinimumViolation(
      @NonNull ICardinalityConstraint constraint,
      @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    addFinding(ConstraintValidationFinding.builder(constraint, node)
        .targets(targets.getValue())
        .message(newCardinalityMinimumViolationMessage(constraint, node, targets))
        .build());
  }

  @Override
  public void handleCardinalityMaximumViolation(
      @NonNull ICardinalityConstraint constraint,
      @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    addFinding(ConstraintValidationFinding.builder(constraint, node)
        .targets(targets.getValue())
        .message(newCardinalityMaximumViolationMessage(constraint, node, targets))
        .build());
  }

  @Override
  public void handleIndexDuplicateKeyViolation(
      @NonNull IIndexConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem oldItem,
      @NonNull INodeItem target) {
    addFinding(ConstraintValidationFinding.builder(constraint, node)
        .target(target)
        .message(newIndexDuplicateKeyViolationMessage(constraint, node, oldItem, target))
        .build());
  }

  @Override
  public void handleUniqueKeyViolation(
      @NonNull IUniqueConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem oldItem,
      @NonNull INodeItem target) {
    addFinding(ConstraintValidationFinding.builder(constraint, node)
        .target(target)
        .message(newUniqueKeyViolationMessage(constraint, node, oldItem, target))
        .build());
  }

  @SuppressWarnings("null")
  @Override
  public void handleKeyMatchError(
      @NonNull IKeyConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull MetapathException cause) {
    addFinding(ConstraintValidationFinding.builder(constraint, node)
        .target(target)
        .message(cause.getLocalizedMessage())
        .cause(cause)
        .build());
  }

  @Override
  public void handleMatchPatternViolation(
      @NonNull IMatchesConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String value) {
    addFinding(ConstraintValidationFinding.builder(constraint, node)
        .target(target)
        .message(newMatchPatternViolationMessage(constraint, node, target, value))
        .build());
  }

  @Override
  public void handleMatchDatatypeViolation(
      @NonNull IMatchesConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String value,
      @NonNull IllegalArgumentException cause) {
    addFinding(ConstraintValidationFinding.builder(constraint, node)
        .target(target)
        .message(newMatchDatatypeViolationMessage(constraint, node, target, value))
        .cause(cause)
        .build());
  }

  @Override
  public void handleExpectViolation(
      @NonNull IExpectConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull DynamicContext dynamicContext) {
    addFinding(ConstraintValidationFinding.builder(constraint, node)
        .target(target)
        .message(newExpectViolationMessage(constraint, node, target, dynamicContext))
        .build());
  }

  @Override
  public void handleAllowedValuesViolation(@NonNull List<IAllowedValuesConstraint> failedConstraints,
      @NonNull INodeItem target) {
    addFinding(ConstraintValidationFinding.builder(failedConstraints, target)
        .target(target)
        .message(newAllowedValuesViolationMessage(failedConstraints, target))
        .build());
  }

  @Override
  public void handleIndexDuplicateViolation(IIndexConstraint constraint, INodeItem node) {
    addFinding(ConstraintValidationFinding.builder(constraint, node)
        .message(newIndexDuplicateViolationMessage(constraint, node))
        .severity(Level.CRITICAL)
        .build());
  }

  @Override
  public void handleIndexMiss(IIndexHasKeyConstraint constraint, INodeItem node, INodeItem target, List<String> key) {
    addFinding(ConstraintValidationFinding.builder(constraint, node)
        .message(newIndexMissMessage(constraint, node, target, key))
        .build());
  }

  @Override
  public void handleGenericValidationViolation(IConstraint constraint, INodeItem node, INodeItem target,
      String message) {
    addFinding(ConstraintValidationFinding.builder(constraint, node)
        .message(newGenericValidationViolationMessage(constraint, node, target, message))
        .build());
  }
}
