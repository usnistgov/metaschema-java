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

package gov.nist.secauto.metaschema.model.common.metapath;

import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IUntypedAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IYearMonthDurationItem;

import edu.umd.cs.findbugs.annotations.NonNull;

class GeneralComparison
    extends AbstractComparison {

  /**
   * Create a new value comparison expression.
   *
   * @param left
   *          the expression to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the expression to compare with
   */
  protected GeneralComparison(@NonNull IExpression left, @NonNull Operator operator, @NonNull IExpression right) {
    super(left, operator, right);
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitGeneralComparison(this, context);
  }

  @Override
  public ISequence<? extends IBooleanItem> accept(DynamicContext dynamicContext, INodeContext context) {
    ISequence<? extends IAnyAtomicItem> leftItems = FnData.fnData(getLeft().accept(dynamicContext, context));
    ISequence<? extends IAnyAtomicItem> rightItems = FnData.fnData(getRight().accept(dynamicContext, context));
    return ISequence.of(valueCompairison(leftItems, getOperator(), rightItems));
  }

  /**
   * Compare the sets of atomic items.
   *
   * @param leftItems
   *          the first set of items to compare
   * @param operator
   *          the comparison operator
   * @param rightItems
   *          the second set of items to compare
   * @return a or an empty {@link ISequence} if either item is {@code null}
   */
  @NonNull
  protected IBooleanItem valueCompairison( // NOPMD - acceptable complexity
      @NonNull ISequence<? extends IAnyAtomicItem> leftItems,
      @NonNull Operator operator,
      @NonNull ISequence<? extends IAnyAtomicItem> rightItems) {

    IBooleanItem retval = IBooleanItem.FALSE;
    for (IAnyAtomicItem left : leftItems.asList()) {
      for (IAnyAtomicItem right : rightItems.asList()) {
        @NonNull IAnyAtomicItem leftCast;
        IAnyAtomicItem rightCast;
        if (left instanceof IUntypedAtomicItem) {
          if (right instanceof IUntypedAtomicItem) {
            leftCast = IStringItem.cast(left);
            rightCast = IStringItem.cast(right);
          } else {
            leftCast = applyGeneralComparisonCast(right, left);
            rightCast = right;
          }
        } else if (right instanceof IUntypedAtomicItem) {
          leftCast = left;
          rightCast = applyGeneralComparisonCast(left, right);
        } else {
          leftCast = left;
          rightCast = right;
        }

        IBooleanItem result = compare(leftCast, operator, rightCast);
        if (IBooleanItem.TRUE.equals(result)) {
          retval = IBooleanItem.TRUE;
        }
      }
    }
    return retval;
  }

  /**
   * Attempts to cast the provided {@code other} item to the type of the {@code item}.
   *
   * @param item
   *          the item whose type the other item is to be cast to
   * @param other
   *          the item to cast
   * @return the casted item
   */
  @NonNull
  protected IAnyAtomicItem applyGeneralComparisonCast(@NonNull IAnyAtomicItem item, @NonNull IAnyAtomicItem other) {
    IAnyAtomicItem retval;
    if (item instanceof INumericItem) {
      retval = IDecimalItem.cast(other);
    } else if (item instanceof IDayTimeDurationItem) {
      retval = IDayTimeDurationItem.cast(other);
    } else if (item instanceof IDayTimeDurationItem) {
      retval = IYearMonthDurationItem.cast(other);
    } else {
      retval = item.getJavaTypeAdapter().cast(other);
    }
    return retval;
  }
}
