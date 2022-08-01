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

package gov.nist.secauto.metaschema.model.common.constraint;

import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.Level;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.validation.IValidationResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;

public class FindingCollectingConstraintValidationHandler
    extends AbstractConstraintValidationHandler
    implements IValidationResult {
  @NonNull
  private final List<ConstraintValidationFinding> findings = new LinkedList<>();
  @NonNull
  private IPathFormatter pathFormatter = IPathFormatter.METAPATH_PATH_FORMATER;
  @NonNull
  private Level highestLevel = IConstraint.Level.INFORMATIONAL;

  @Override
  @NonNull
  public IPathFormatter getPathFormatter() {
    return pathFormatter;
  }

  @SuppressWarnings("null")
  public void setPathFormatter(@NonNull IPathFormatter pathFormatter) {
    this.pathFormatter = Objects.requireNonNull(pathFormatter, "pathFormatter");
  }

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

  protected void newFinding(
      @NonNull IConstraint constraint,
      @NonNull INodeItem node,
      @NonNull List<? extends INodeItem> targets,
      @NonNull CharSequence message,
      Throwable cause) {
    newFinding(
        CollectionUtil.singletonList(constraint),
        node,
        targets,
        message,
        cause);
  }

  protected void newFinding(
      @NonNull List<? extends IConstraint> constraints,
      @NonNull INodeItem node,
      @NonNull List<? extends INodeItem> targets,
      @NonNull CharSequence message,
      Throwable cause) {

    ConstraintValidationFinding finding = new ConstraintValidationFinding(constraints, message, cause, node, targets);
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
    newFinding(constraint, node, CollectionUtil.unmodifiableList(targets.asList()),
        newCardinalityMinimumViolationMessage(constraint, node, targets), null);
  }

  @Override
  public void handleCardinalityMaximumViolation(
      @NonNull ICardinalityConstraint constraint,
      @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    newFinding(constraint, node, CollectionUtil.unmodifiableList(targets.asList()),
        newCardinalityMaximumViolationMessage(constraint, node, targets), null);
  }

  @Override
  public void handleIndexDuplicateKeyViolation(
      @NonNull IIndexConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem oldItem,
      @NonNull INodeItem target) {
    newFinding(constraint, node, CollectionUtil.singletonList(target),
        newIndexDuplicateKeyViolationMessage(constraint, node, oldItem, target), null);
  }

  @Override
  public void handleUniqueKeyViolation(
      @NonNull IUniqueConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem oldItem,
      @NonNull INodeItem target) {
    newFinding(constraint, node, CollectionUtil.singletonList(target),
        newUniqueKeyViolationMessage(constraint, node, oldItem, target), null);
  }

  @SuppressWarnings("null")
  @Override
  public void handleKeyMatchError(
      @NonNull IKeyConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull MetapathException cause) {
    newFinding(constraint, node, CollectionUtil.singletonList(target), cause.getLocalizedMessage(), cause);
  }

  @Override
  public void handleMatchPatternViolation(
      @NonNull IMatchesConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String value) {
    newFinding(constraint, node, CollectionUtil.singletonList(target),
        newMatchPatternViolationMessage(constraint, node, target, value), null);
  }

  @Override
  public void handleMatchDatatypeViolation(
      @NonNull IMatchesConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String value,
      @NonNull IllegalArgumentException cause) {
    newFinding(constraint, node, CollectionUtil.singletonList(target),
        newMatchDatatypeViolationMessage(constraint, node, target, value), cause);
  }

  @Override
  public void handleExpectViolation(
      @NonNull IExpectConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull DynamicContext dynamicContext) {
    newFinding(constraint, node, CollectionUtil.singletonList(target),
        newExpectViolationMessage(constraint, node, target, dynamicContext), null);
  }

  @Override
  public void handleAllowedValuesViolation(@NonNull List<IAllowedValuesConstraint> failedConstraints,
      @NonNull INodeItem target) {
    newFinding(failedConstraints, target, CollectionUtil.singletonList(target),
        newAllowedValuesViolationMessage(failedConstraints, target), null);
  }

}
