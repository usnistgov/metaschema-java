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

import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class FindingCollectingConstraintValidationHandler
    extends AbstractConstraintValidationHandler {
  @NotNull
  private IPathFormatter pathFormatter = IPathFormatter.METAPATH_PATH_FORMATER;
  @NotNull
  private final List<Finding> findings = new LinkedList<>();
  private IConstraint.Level highestLevel = IConstraint.Level.INFORMATIONAL;

  @NotNull
  public IPathFormatter getPathFormatter() {
    return pathFormatter;
  }

  @SuppressWarnings("null")
  public void setPathFormatter(@NotNull IPathFormatter pathFormatter) {
    this.pathFormatter = Objects.requireNonNull(pathFormatter, "pathFormatter");
  }

  public List<Finding> getFindings() {
    return Collections.unmodifiableList(findings);
  }

  public IConstraint.Level getHighestLevel() {
    return highestLevel;
  }

  @SuppressWarnings("null")
  private void newFinding(
      @NotNull IConstraint constraint,
      @NotNull INodeItem node,
      @NotNull INodeItem target,
      @NotNull CharSequence message,
      Throwable cause) {
    newFinding(constraint, node, Collections.singletonList(target), message, cause);
  }

  private void newFinding(
      @NotNull IConstraint constraint,
      @NotNull INodeItem node,
      @NotNull List<? extends INodeItem> targets,
      @NotNull CharSequence message,
      Throwable cause) {
    findings.add(new Finding(constraint, message, cause, node, targets));

    if (constraint.getLevel().ordinal() > highestLevel.ordinal()) {
      highestLevel = constraint.getLevel();
    }
  }

  @Override
  public void handleCardinalityMinimumViolation(
      @NotNull ICardinalityConstraint constraint,
      @NotNull INodeItem node,
      @NotNull ISequence<? extends INodeItem> targets) {
    newFinding(constraint, node, Collections.unmodifiableList(targets.asList()),
        newCardinalityMinimumViolationMessage(constraint, node, targets), null);
  }

  @Override
  public void handleCardinalityMaximumViolation(
      @NotNull ICardinalityConstraint constraint,
      @NotNull INodeItem node,
      @NotNull ISequence<? extends INodeItem> targets) {
    newFinding(constraint, node, Collections.unmodifiableList(targets.asList()),
        newCardinalityMaximumViolationMessage(constraint, node, targets), null);
  }

  @Override
  public void handleIndexDuplicateKeyViolation(
      @NotNull IIndexConstraint constraint,
      @NotNull INodeItem node,
      @NotNull INodeItem oldItem,
      @NotNull INodeItem target) {
    newFinding(constraint, node, target, newIndexDuplicateKeyViolationMessage(constraint, node, oldItem, target), null);
  }

  @Override
  public void handleUniqueKeyViolation(
      @NotNull IUniqueConstraint constraint,
      @NotNull INodeItem node,
      @NotNull INodeItem oldItem,
      @NotNull INodeItem target) {
    newFinding(constraint, node, target, newUniqueKeyViolationMessage(constraint, node, oldItem, target), null);
  }

  @SuppressWarnings("null")
  @Override
  public void handleKeyMatchError(
      @NotNull IKeyConstraint constraint,
      @NotNull INodeItem node,
      @NotNull INodeItem target,
      @NotNull MetapathException cause) {
    newFinding(constraint, node, target, cause.getLocalizedMessage(), cause);
  }

  @Override
  public void handleMatchPatternViolation(
      @NotNull IMatchesConstraint constraint,
      @NotNull INodeItem node,
      @NotNull INodeItem target,
      @NotNull String value) {
    newFinding(constraint, node, target, newMatchPatternViolationMessage(constraint, node, target, value), null);
  }

  @Override
  public void handleMatchDatatypeViolation(
      @NotNull IMatchesConstraint constraint,
      @NotNull INodeItem node,
      @NotNull INodeItem target,
      @NotNull String value,
      @NotNull IllegalArgumentException cause) {
    newFinding(constraint, node, target, newMatchDatatypeViolationMessage(constraint, node, target, value), cause);
  }

  @Override
  public void handleExpectViolation(
      @NotNull IExpectConstraint constraint,
      @NotNull INodeItem node,
      @NotNull INodeItem target,
      @NotNull DynamicContext dynamicContext) {
    newFinding(constraint, node, target, newExpectViolationMessage(constraint, node, target, dynamicContext), null);
  }

  public static class Finding {
    @NotNull
    private final IConstraint constraint;
    @NotNull
    private final CharSequence message;
    @NotNull
    private final INodeItem node;
    @NotNull
    private final List<? extends INodeItem> targets;
    private final Throwable cause;

    @SuppressWarnings("null")
    public Finding(
        @NotNull IConstraint constraint,
        @NotNull CharSequence message,
        Throwable cause,
        @NotNull INodeItem node,
        @NotNull List<? extends INodeItem> targets) {
      this.constraint = Objects.requireNonNull(constraint, "constraint");
      this.message = Objects.requireNonNull(message, "message");
      this.cause = cause;
      this.node = Objects.requireNonNull(node, "node");
      this.targets = Objects.requireNonNull(targets, "targets");
    }

    public IConstraint getConstraint() {
      return constraint;
    }

    public CharSequence getMessage() {
      return message;
    }

    public INodeItem getNode() {
      return node;
    }

    public List<? extends INodeItem> getTargets() {
      return targets;
    }

    public Throwable getCause() {
      return cause;
    }

  }
}
