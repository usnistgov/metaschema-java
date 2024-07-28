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

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.Level;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class LoggingConstraintValidationHandler
    extends AbstractConstraintValidationHandler {
  private static final Logger LOGGER = LogManager.getLogger(DefaultConstraintValidator.class);

  private static LogBuilder getLogBuilder(@NonNull Level level) {
    LogBuilder retval;
    switch (level) {
    case CRITICAL:
      retval = LOGGER.atFatal();
      break;
    case ERROR:
      retval = LOGGER.atError();
      break;
    case WARNING:
      retval = LOGGER.atWarn();
      break;
    case INFORMATIONAL:
      retval = LOGGER.atInfo();
      break;
    default:
      throw new UnsupportedOperationException(String.format("unsupported level '%s'", level));
    }
    return retval;
  }

  @Override
  protected String toPath(@NonNull INodeItem nodeItem) {
    return nodeItem.toPath(getPathFormatter());
  }

  /**
   * Determine if a failure to validate a constraint at the given severity level
   * should be logged.
   *
   * @param level
   *          the severity level to check
   * @return {@code true} if the severity level should be logged, or {@code false}
   *         otherwise
   */
  private static boolean isLogged(@NonNull Level level) {
    boolean retval;
    switch (level) {
    case CRITICAL:
      retval = LOGGER.isFatalEnabled();
      break;
    case ERROR:
      retval = LOGGER.isErrorEnabled();
      break;
    case WARNING:
      retval = LOGGER.isWarnEnabled();
      break;
    case INFORMATIONAL:
      retval = LOGGER.isInfoEnabled();
      break;
    default:
      throw new UnsupportedOperationException(String.format("unsupported level '%s'", level));
    }
    return retval;
  }

  private void logConstraint(
      @NonNull Level level,
      @NonNull INodeItem node,
      @NonNull CharSequence message,
      @Nullable Throwable cause) {
    LogBuilder builder = getLogBuilder(level);
    if (cause != null) {
      builder.withThrowable(cause);
    }

    builder.log("{}: ({}) {}", level.name(), toPath(node), message);
  }

  @Override
  public void handleCardinalityMinimumViolation(
      @NonNull ICardinalityConstraint constraint,
      @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    Level level = constraint.getLevel();
    if (isLogged(level)) {
      logConstraint(level, node, newCardinalityMinimumViolationMessage(constraint, node, targets), null);
    }
  }

  @Override
  public void handleCardinalityMaximumViolation(
      @NonNull ICardinalityConstraint constraint,
      @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    Level level = constraint.getLevel();
    if (isLogged(level)) {
      logConstraint(level, node, newCardinalityMaximumViolationMessage(constraint, node, targets), null);
    }
  }

  @Override
  public void handleIndexDuplicateKeyViolation(
      @NonNull IIndexConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem oldItem,
      @NonNull INodeItem target) {
    Level level = constraint.getLevel();
    if (isLogged(level)) {
      logConstraint(level, target, newIndexDuplicateKeyViolationMessage(constraint, node, oldItem, target), null);
    }
  }

  @Override
  public void handleUniqueKeyViolation(
      @NonNull IUniqueConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem oldItem,
      @NonNull INodeItem target) {
    Level level = constraint.getLevel();
    if (isLogged(level)) {
      logConstraint(level, target, newUniqueKeyViolationMessage(constraint, node, oldItem, target), null);
    }
  }

  @SuppressWarnings("null")
  @Override
  public void handleKeyMatchError(
      @NonNull IKeyConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull MetapathException cause) {
    Level level = constraint.getLevel();
    if (isLogged(level)) {
      logConstraint(level, target, cause.getLocalizedMessage(), cause);
    }
  }

  @Override
  public void handleMatchPatternViolation(
      @NonNull IMatchesConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String value,
      @NonNull Pattern pattern) {
    Level level = constraint.getLevel();
    if (isLogged(level)) {
      logConstraint(level, target, newMatchPatternViolationMessage(constraint, node, target, value, pattern), null);
    }
  }

  @Override
  public void handleMatchDatatypeViolation(
      @NonNull IMatchesConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String value,
      @NonNull IDataTypeAdapter<?> adapter,
      @NonNull IllegalArgumentException cause) {
    Level level = constraint.getLevel();
    if (isLogged(level)) {
      logConstraint(level, target, newMatchDatatypeViolationMessage(constraint, node, target, value, adapter), cause);
    }
  }

  @Override
  public void handleExpectViolation(
      @NonNull IExpectConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull DynamicContext dynamicContext) {
    Level level = constraint.getLevel();
    if (isLogged(level)) {
      logConstraint(level, target, newExpectViolationMessage(constraint, node, target, dynamicContext), null);
    }
  }

  @Override
  public void handleAllowedValuesViolation(@NonNull List<IAllowedValuesConstraint> failedConstraints,
      @NonNull INodeItem target) {

    Level level = ObjectUtils.notNull(failedConstraints.stream()
        .map(IConstraint::getLevel)
        .max(Comparator.comparing(Level::ordinal))
        .get());
    if (isLogged(level)) {
      logConstraint(level, target, newAllowedValuesViolationMessage(failedConstraints, target), null);
    }
  }

  @Override
  public void handleIndexDuplicateViolation(IIndexConstraint constraint, INodeItem node) {
    Level level = Level.CRITICAL;
    if (isLogged(level)) {
      logConstraint(level, node, newIndexDuplicateViolationMessage(constraint, node), null);
    }
  }

  @Override
  public void handleIndexMiss(IIndexHasKeyConstraint constraint, INodeItem node, INodeItem target, List<String> key) {
    Level level = constraint.getLevel();
    if (isLogged(level)) {
      logConstraint(level, node, newIndexMissMessage(constraint, node, target, key), null);
    }
  }

  @Override
  public void handleMissingIndexViolation(IIndexHasKeyConstraint constraint, INodeItem node, INodeItem target,
      String message) {
    Level level = constraint.getLevel();
    if (isLogged(level)) {
      logConstraint(level, node, newMissingIndexViolationMessage(constraint, node, target, message), null);
    }
  }

  @Override
  public void handlePass(IConstraint constraint, INodeItem node, INodeItem target) {
    // do nothing
  }
}
