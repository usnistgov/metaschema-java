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

import gov.nist.secauto.metaschema.model.common.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractConstraintValidationHandler implements IConstraintValidationHandler {
  @NonNull
  public abstract IPathFormatter getPathFormatter();

  protected String toPath(@NonNull INodeItem nodeItem) {
    return nodeItem.toPath(getPathFormatter());
  }

  @SuppressWarnings("null")
  @NonNull
  protected String newCardinalityMinimumViolationMessage(
      @NonNull ICardinalityConstraint constraint,
      @SuppressWarnings("unused") @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    // TODO: render the item paths instead of the expression
    return String.format(
        "The cardinality '%d' is below the required minimum '%d' for items matching the expression '%s'.",
        targets.size(), constraint.getMinOccurs(), constraint.getTarget().getPath());
  }

  @SuppressWarnings("null")
  @NonNull
  protected String newCardinalityMaximumViolationMessage(
      @NonNull ICardinalityConstraint constraint,
      @SuppressWarnings("unused") @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    // TODO: render the item paths instead of the expression
    return String.format(
        "The cardinality '%d' is greater than the required maximum '%d' for items matching the expression '%s'.",
        targets.size(), constraint.getMinOccurs(), constraint.getTarget().getPath());
  }

  @SuppressWarnings("null")
  @NonNull
  protected String newIndexDuplicateKeyViolationMessage(
      @NonNull IIndexConstraint constraint,
      @SuppressWarnings("unused") @NonNull INodeItem node,
      @NonNull INodeItem oldItem,
      @NonNull INodeItem target) {
    // TODO: render the key paths
    return String.format("Index '%s' has duplicate key for items at paths '%s' and '%s'", constraint.getName(),
        toPath(oldItem), toPath(target));
  }

  @SuppressWarnings("null")
  @NonNull
  protected String newUniqueKeyViolationMessage(
      @SuppressWarnings("unused") @NonNull IUniqueConstraint constraint,
      @SuppressWarnings("unused") @NonNull INodeItem node,
      @NonNull INodeItem oldItem,
      @NonNull INodeItem target) {
    // TODO: render the key paths
    return String.format("Unique constraint violation at paths '%s' and '%s'",
        toPath(oldItem), toPath(target));
  }

  @SuppressWarnings("null")
  @NonNull
  protected String newMatchPatternViolationMessage(
      @NonNull IMatchesConstraint constraint,
      @SuppressWarnings("unused") @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String value) {
    return String.format("Value '%s' did not match the pattern '%s' at path '%s'",
        value,
        constraint.getPattern().pattern(),
        toPath(target));
  }

  @SuppressWarnings("null")
  @NonNull
  protected String newMatchDatatypeViolationMessage(
      @NonNull IMatchesConstraint constraint,
      @SuppressWarnings("unused") @NonNull INodeItem node,
      @NonNull INodeItem target,
      @NonNull String value) {
    IDataTypeAdapter<?> adapter = constraint.getDataType();
    return String.format("Value '%s' did not conform to the data type '%s' at path '%s'", value,
        adapter.getPreferredName(), toPath(target));
  }

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
          constraint.getTest().getPath(),
          toPath(target));
    }
    return message;
  }

  @SuppressWarnings("null")
  @NonNull
  protected CharSequence newAllowedValuesViolationMessage(
      @NonNull List<IAllowedValuesConstraint> constraints,
      @NonNull INodeItem target) {

    String allowedValues = constraints.stream()
        .flatMap(constraint -> constraint.getAllowedValues().values().stream())
        .map(allowedValue -> allowedValue.getValue())
        .sorted()
        .distinct()
        .collect(CustomCollectors.joiningWithOxfordComma("or"));

    return String.format("Value '%s' doesn't match one of '%s' at path '%s'",
        FnData.fnDataItem(target).asString(),
        allowedValues,
        toPath(target));
  }

  @SuppressWarnings("null")
  @NonNull
  protected CharSequence newIndexDuplicateViolationMessage(
      @NonNull IIndexConstraint constraint,
      @NonNull INodeItem node) {
    return String.format("Duplicate index named '%s' found at path '%s'",
        constraint.getName(),
        node.getMetapath());
  }

  @SuppressWarnings("null")
  @NonNull
  protected CharSequence newIndexMissMessage(
      @NonNull IIndexHasKeyConstraint constraint,
      @SuppressWarnings("unused") @NonNull INodeItem node,
      @NonNull INodeItem target) {
    return String.format("Key reference not found in index '%s' for item at path '%s'",
        constraint.getIndexName(),
        target.getMetapath());
  }

}
