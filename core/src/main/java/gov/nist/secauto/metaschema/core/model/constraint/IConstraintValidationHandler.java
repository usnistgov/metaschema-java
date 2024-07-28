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

import java.util.List;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IConstraintValidationHandler {

  /**
   * Handle a cardinality constraint minimum violation.
   *
   * @param constraint
   *          the constraint that was evaluated
   * @param node
   *          the node the constraint was evaluated as the focus to determine
   *          targets
   * @param targets
   *          the targets of evaluation
   */
  void handleCardinalityMinimumViolation(
      @NonNull ICardinalityConstraint constraint,
      @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets);

  /**
   * Handle a cardinality constraint maximum violation.
   *
   * @param constraint
   *          the constraint that was evaluated
   * @param node
   *          the node the constraint was evaluated as the focus to determine
   *          targets
   * @param targets
   *          the targets of evaluation
   */
  void handleCardinalityMaximumViolation(
      @NonNull ICardinalityConstraint constraint,
      @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets);

  /**
   * Handle a duplicate index violation.
   *
   * @param constraint
   *          the constraint that was evaluated
   * @param node
   *          the node the constraint was evaluated as the focus to determine
   *          targets
   */
  void handleIndexDuplicateViolation(
      @NonNull IIndexConstraint constraint,
      @NonNull INodeItem node);

  /**
   * Handle an index duplicate key violation.
   * <p>
   * This happens when two target nodes have the same key.
   *
   * @param constraint
   *          the constraint that was evaluated
   * @param node
   *          the node the constraint was evaluated as the focus to determine
   *          targets
   * @param oldItem
   *          the node that exists in the index for the related key
   * @param target
   *          the target of evaluation
   */
  void handleIndexDuplicateKeyViolation(
      @NonNull IIndexConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem oldItem,
      @NonNull INodeItem target);

  /**
   * Handle an unique key violation.
   * <p>
   * This happens when two target nodes have the same key.
   *
   * @param constraint
   *          the constraint that was evaluated
   * @param node
   *          the node the constraint was evaluated as the focus to determine
   *          targets
   * @param oldItem
   *          the other node with the same key
   * @param target
   *          the target of evaluation
   */
  void handleUniqueKeyViolation(
      @NonNull IUniqueConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem oldItem,
      @NonNull INodeItem target);

  /**
   * Handle an error that occurred while generating a key.
   *
   * @param constraint
   *          the constraint that was evaluated
   * @param node
   *          the node the constraint was evaluated as the focus to determine
   *          targets
   * @param target
   *          the target of evaluation
   * @param exception
   *          the resulting Metapath exception
   */
  void handleKeyMatchError(
      @NonNull IKeyConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull MetapathException exception);

  /**
   * Handle a missing index violation.
   * <p>
   * This happens when an index-has-key constraint references a missing index.
   *
   * @param constraint
   *          the constraint that was evaluated
   * @param node
   *          the node the constraint was evaluated as the focus to determine
   *          targets
   * @param target
   *          the target of evaluation
   * @param message
   *          the error message
   */
  void handleMissingIndexViolation(
      @NonNull IIndexHasKeyConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String message);

  /**
   * Handle an index lookup key miss violation.
   * <p>
   * This happens when another node references an expected member of an index that
   * does not actually exist in the index.
   *
   * @param constraint
   *          the constraint that was evaluated
   * @param node
   *          the node the constraint was evaluated as the focus to determine
   *          targets
   * @param target
   *          the target of evaluation
   * @param key
   *          the key that was used to lookup the index entry
   */
  void handleIndexMiss(
      @NonNull IIndexHasKeyConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull List<String> key);

  /**
   * Handle a match pattern violation.
   * <p>
   * This happens when the target value does not match the specified pattern.
   *
   * @param constraint
   *          the constraint that was evaluated
   * @param node
   *          the node the constraint was evaluated as the focus to determine
   *          targets
   * @param target
   *          the target of evaluation
   * @param value
   *          the value used for pattern matching
   * @param pattern
   *          the pattern used for pattern matching
   */
  void handleMatchPatternViolation(
      @NonNull IMatchesConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String value,
      @NonNull Pattern pattern);

  /**
   * Handle a match data type violation.
   * <p>
   * This happens when the target value does not conform to the specified data
   * type.
   *
   * @param constraint
   *          the constraint that was evaluated
   * @param node
   *          the node the constraint was evaluated as the focus to determine
   *          targets
   * @param target
   *          the target of evaluation
   * @param value
   *          the value used for data type matching
   * @param adapter
   *          the data type used for data type matching
   * @param cause
   *          the data type exception related to this violation
   */
  void handleMatchDatatypeViolation(
      @NonNull IMatchesConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String value,
      @NonNull IDataTypeAdapter<?> adapter,
      @NonNull IllegalArgumentException cause);

  /**
   * Handle an expect test violation.
   * <p>
   * This happens when the test does not evaluate to true.
   *
   * @param constraint
   *          the constraint that was evaluated
   * @param node
   *          the node the constraint was evaluated as the focus to determine
   *          targets
   * @param target
   *          the target of evaluation
   * @param metapathContext
   *          the Metapath evaluation context
   */
  void handleExpectViolation(
      @NonNull IExpectConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull DynamicContext metapathContext);

  /**
   * Handle an allowed values constraint violation.
   *
   * @param failedConstraints
   *          the allowed values constraints that did not match.
   * @param target
   *          the target of evaluation
   */
  void handleAllowedValuesViolation(
      @NonNull List<IAllowedValuesConstraint> failedConstraints,
      @NonNull INodeItem target);

  /**
   * Handle a constraint that has passed validation.
   *
   * @param constraint
   *          the constraint that was evaluated
   * @param node
   *          the node the constraint was evaluated as the focus to determine
   *          targets
   * @param target
   *          the target of evaluation
   */
  void handlePass(
      @NonNull IConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target);
}
