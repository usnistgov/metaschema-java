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
import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides messaging for constraint violations.
 */
public abstract class AbstractConstraintValidationHandler implements IConstraintValidationHandler {
  @NonNull
  private IPathFormatter pathFormatter = IPathFormatter.METAPATH_PATH_FORMATER;

  /**
   * Get the formatter used to generate content paths for validation issue
   * locations.
   *
   * @return the formatter
   */
  @NonNull
  public IPathFormatter getPathFormatter() {
    return pathFormatter;
  }

  /**
   * Set the path formatter to use when generating contextual paths in validation
   * messages.
   *
   * @param formatter
   *          the path formatter to use
   */
  public void setPathFormatter(@NonNull IPathFormatter formatter) {
    this.pathFormatter = Objects.requireNonNull(formatter, "pathFormatter");
  }

  /**
   * Get the path of the provided item using the configured path formatter.
   *
   * @param item
   *          the node item to generate the path for
   * @return the path
   * @see #getPathFormatter()
   */
  protected String toPath(@NonNull INodeItem item) {
    return item.toPath(getPathFormatter());
  }

  /**
   * Construct a new violation message for the provided {@code constraint} applied
   * to the {@code node}.
   *
   * @param constraint
   *          the constraint the requested message pertains to
   * @param node
   *          the item the constraint targeted
   * @param targets
   *          the targets matching the constraint
   * @return the new message
   */
  @SuppressWarnings("null")
  @NonNull
  protected String newCardinalityMinimumViolationMessage(
      @NonNull ICardinalityConstraint constraint,
      @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    return String.format(
        "The cardinality '%d' is below the required minimum '%d' for items matching '%s'.",
        targets.size(),
        constraint.getMinOccurs(),
        constraint.getTarget());
  }

  /**
   * Construct a new violation message for the provided {@code constraint} applied
   * to the {@code node}.
   *
   * @param constraint
   *          the constraint the requested message pertains to
   * @param node
   *          the item the constraint targeted
   * @param targets
   *          the targets matching the constraint
   * @return the new message
   */
  @SuppressWarnings("null")
  @NonNull
  protected String newCardinalityMaximumViolationMessage(
      @NonNull ICardinalityConstraint constraint,
      @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    return String.format(
        "The cardinality '%d' is greater than the required maximum '%d' at: %s.",
        targets.size(),
        constraint.getMinOccurs(),
        targets.safeStream()
            .map(item -> new StringBuilder(12)
                .append('\'')
                .append(toPath(item))
                .append('\'')
                .toString())
            .collect(CustomCollectors.joiningWithOxfordComma("and")));
  }

  /**
   * Construct a new violation message for the provided {@code constraint} applied
   * to the {@code node}.
   *
   * @param constraint
   *          the constraint the requested message pertains to
   * @param node
   *          the item the constraint targeted
   * @param oldItem
   *          the original item matching the constraint
   * @param newItem
   *          the new item matching the constraint
   * @return the new message
   */
  @SuppressWarnings("null")
  @NonNull
  protected String newIndexDuplicateKeyViolationMessage(
      @NonNull IIndexConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem oldItem,
      @NonNull INodeItem newItem) {
    // TODO: render the key paths
    return String.format("Index '%s' has duplicate key for items at paths '%s' and '%s'",
        constraint.getName(),
        toPath(oldItem),
        toPath(newItem));
  }

  /**
   * Construct a new violation message for the provided {@code constraint} applied
   * to the {@code node}.
   *
   * @param constraint
   *          the constraint the requested message pertains to
   * @param node
   *          the item the constraint targeted
   * @param oldItem
   *          the original item matching the constraint
   * @param newItem
   *          the new item matching the constraint
   * @return the new message
   */
  @SuppressWarnings("null")
  @NonNull
  protected String newUniqueKeyViolationMessage(
      @NonNull IUniqueConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem oldItem,
      @NonNull INodeItem newItem) {
    return String.format("Unique constraint violation at paths '%s' and '%s'",
        toPath(oldItem),
        toPath(newItem));
  }

  /**
   * Construct a new violation message for the provided {@code constraint} applied
   * to the {@code node}.
   *
   * @param constraint
   *          the constraint the requested message pertains to
   * @param node
   *          the item the constraint targeted
   * @param target
   *          the target matching the constraint
   * @param value
   *          the target's value
   * @return the new message
   */
  @SuppressWarnings("null")
  @NonNull
  protected String newMatchPatternViolationMessage(
      @NonNull IMatchesConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String value) {
    return String.format("Value '%s' did not match the pattern '%s' at path '%s'",
        value,
        constraint.getPattern().pattern(),
        toPath(target));
  }

  /**
   * Construct a new violation message for the provided {@code constraint} applied
   * to the {@code node}.
   *
   * @param constraint
   *          the constraint the requested message pertains to
   * @param node
   *          the item the constraint targeted
   * @param target
   *          the target matching the constraint
   * @param value
   *          the target's value
   * @return the new message
   */
  @SuppressWarnings("null")
  @NonNull
  protected String newMatchDatatypeViolationMessage(
      @NonNull IMatchesConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String value) {
    IDataTypeAdapter<?> adapter = constraint.getDataType();
    return String.format("Value '%s' did not conform to the data type '%s' at path '%s'", value,
        adapter.getPreferredName(), toPath(target));
  }

  /**
   * Construct a new violation message for the provided {@code constraint} applied
   * to the {@code node}.
   *
   * @param constraint
   *          the constraint the requested message pertains to
   * @param node
   *          the item the constraint targeted
   * @param target
   *          the target matching the constraint
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   * @return the new message
   */
  @SuppressWarnings("null")
  @NonNull
  protected CharSequence newExpectViolationMessage(
      @NonNull IExpectConstraint constraint,
      @SuppressWarnings("unused") @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull DynamicContext dynamicContext) {
    CharSequence message;
    if (constraint.getMessage() != null) {
      message = constraint.generateMessage(target, dynamicContext);
    } else {
      message = String.format("Expect constraint '%s' did not match the data at path '%s'",
          constraint.getTest(),
          toPath(target));
    }
    return message;
  }

  /**
   * Construct a new violation message for the provided {@code constraint} applied
   * to the {@code node}.
   *
   * @param constraints
   *          the constraints the requested message pertains to
   * @param target
   *          the target matching the constraint
   * @return the new message
   */
  @SuppressWarnings("null")
  @NonNull
  protected CharSequence newAllowedValuesViolationMessage(
      @NonNull List<IAllowedValuesConstraint> constraints,
      @NonNull INodeItem target) {

    String allowedValues = constraints.stream()
        .flatMap(constraint -> constraint.getAllowedValues().values().stream())
        .map(IAllowedValue::getValue)
        .sorted()
        .distinct()
        .collect(CustomCollectors.joiningWithOxfordComma("or"));

    return String.format("Value '%s' doesn't match one of '%s' at path '%s'",
        FnData.fnDataItem(target).asString(),
        allowedValues,
        toPath(target));
  }

  /**
   * Construct a new violation message for the provided {@code constraint} applied
   * to the {@code node}.
   *
   * @param constraint
   *          the constraint the requested message pertains to
   * @param node
   *          the item the constraint targeted
   * @return the new message
   */
  @SuppressWarnings("null")
  @NonNull
  protected CharSequence newIndexDuplicateViolationMessage(
      @NonNull IIndexConstraint constraint,
      @NonNull INodeItem node) {
    return String.format("Duplicate index named '%s' found at path '%s'",
        constraint.getName(),
        node.getMetapath());
  }

  /**
   * Construct a new violation message for the provided {@code constraint} applied
   * to the {@code node}.
   *
   * @param constraint
   *          the constraint the requested message pertains to
   * @param node
   *          the item the constraint targeted
   * @param target
   *          the target matching the constraint
   * @param key
   *          the key derived from the target that failed to be found in the index
   * @return the new message
   */
  @SuppressWarnings("null")
  @NonNull
  protected CharSequence newIndexMissMessage(
      @NonNull IIndexHasKeyConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull List<String> key) {
    String keyValues = key.stream()
        .collect(Collectors.joining(","));

    return String.format("Key reference [%s] not found in index '%s' for item at path '%s'",
        keyValues,
        constraint.getIndexName(),
        target.getMetapath());
  }

  /**
   * Construct a new generic violation message for the provided {@code constraint}
   * applied to the {@code node}.
   *
   * @param constraint
   *          the constraint the requested message pertains to
   * @param node
   *          the item the constraint targeted
   * @param target
   *          the target matching the constraint
   * @param message
   *          the message to be added before information about the target path
   * @return the new message
   */
  @SuppressWarnings("null")
  @NonNull
  protected CharSequence newGenericValidationViolationMessage(
      @NonNull IConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String message) {
    return String.format("%s for constraint '%s' for item at path '%s'",
        message,
        Objects.requireNonNullElse(constraint.getId(), "?"),
        target.getMetapath());
  }
}
