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

package gov.nist.secauto.metaschema.model.common.metapath.ast;

import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.OperationFunctions;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDateItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDateTimeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IYearMonthDurationItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Addition
    extends AbstractArithmeticExpression<IAnyAtomicItem> {

  /**
   * An expression that sums two atomic data items.
   * 
   * @param left
   *          an expression whose result is summed
   * @param right
   *          an expression whose result is summed
   */
  public Addition(@NotNull IExpression left, @NotNull IExpression right) {
    super(left, right, IAnyAtomicItem.class);
  }

  @Override
  public Class<@NotNull IAnyAtomicItem> getBaseResultType() {
    return IAnyAtomicItem.class;
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitAddition(this, context);
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> accept(DynamicContext dynamicContext, INodeContext context) {
    IAnyAtomicItem leftItem = getFirstDataItem(getLeft().accept(dynamicContext, context), true);
    IAnyAtomicItem rightItem = getFirstDataItem(getRight().accept(dynamicContext, context), true);

    return resultOrEmpty(leftItem, rightItem);
  }

  /**
   * Get the sum of two atomic items.
   * 
   * @param leftItem
   *          the first item to sum
   * @param rightItem
   *          the second item to sum
   * @return the sum of both items or an empty {@link ISequence} if either item is {@code null}
   */
  @NotNull
  protected static ISequence<? extends IAnyAtomicItem> resultOrEmpty(@Nullable IAnyAtomicItem leftItem,
      @Nullable IAnyAtomicItem rightItem) {
    ISequence<? extends IAnyAtomicItem> retval;
    if (leftItem == null || rightItem == null) {
      retval = ISequence.empty();
    } else {
      IAnyAtomicItem result = sum(leftItem, rightItem);
      retval = ISequence.of(result);
    }
    return retval;
  }

  /**
   * Get the sum of two atomic items.
   * 
   * @param leftItem
   *          the first item to sum
   * @param rightItem
   *          the second item to sum
   * @return the sum of both items
   */
  @NotNull
  public static IAnyAtomicItem sum(@NotNull IAnyAtomicItem leftItem,
      @NotNull IAnyAtomicItem rightItem) {
    IAnyAtomicItem retval = null;
    if (leftItem instanceof IDateItem) {
      IDateItem left = (IDateItem) leftItem;
      if (rightItem instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opAddYearMonthDurationToDate(left, (IYearMonthDurationItem) rightItem);
      } else if (rightItem instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opAddDayTimeDurationToDate(left, (IDayTimeDurationItem) rightItem);
      }
    } else if (leftItem instanceof IDateTimeItem) {
      IDateTimeItem left = (IDateTimeItem) leftItem;
      if (rightItem instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opAddYearMonthDurationToDateTime(left, (IYearMonthDurationItem) rightItem);
      } else if (rightItem instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opAddDayTimeDurationToDateTime(left, (IDayTimeDurationItem) rightItem);
      }
    } else if (leftItem instanceof IYearMonthDurationItem) {
      IYearMonthDurationItem left = (IYearMonthDurationItem) leftItem;
      if (rightItem instanceof IDateItem) {
        retval = OperationFunctions.opAddYearMonthDurationToDate((IDateItem) rightItem, left);
      } else if (rightItem instanceof IDateTimeItem) {
        retval = OperationFunctions.opAddYearMonthDurationToDateTime((IDateTimeItem) rightItem, left);
      } else if (rightItem instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opSubtractYearMonthDurations(left, (IYearMonthDurationItem) rightItem);
      }
    } else if (leftItem instanceof IDayTimeDurationItem) {
      IDayTimeDurationItem left = (IDayTimeDurationItem) leftItem;
      if (rightItem instanceof IDateItem) {
        retval = OperationFunctions.opAddDayTimeDurationToDate((IDateItem) rightItem, left);
      } else if (rightItem instanceof IDateTimeItem) {
        retval = OperationFunctions.opAddDayTimeDurationToDateTime((IDateTimeItem) rightItem, left);
      } else if (rightItem instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opAddDayTimeDurations(left, (IDayTimeDurationItem) rightItem);
      }
    } else {
      // handle as numeric
      INumericItem left = FunctionUtils.toNumeric(leftItem);
      INumericItem right = FunctionUtils.toNumeric(rightItem);
      retval = OperationFunctions.opNumericAdd(left, right);
    }
    if (retval == null) {
      throw new UnsupportedOperationException(
          String.format("The expression '%s + %s' is not supported", leftItem.getClass().getName(), rightItem.getClass().getName()));
    }
    return retval;
  }
}
