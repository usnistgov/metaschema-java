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

import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.OperationFunctions;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IDateItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IDateTimeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IYearMonthDurationItem;

import edu.umd.cs.findbugs.annotations.NonNull;

class Addition
    extends AbstractBasicArithmeticExpression {

  /**
   * An expression that sums two atomic data items.
   *
   * @param left
   *          an expression whose result is summed
   * @param right
   *          an expression whose result is summed
   */
  protected Addition(@NonNull IExpression left, @NonNull IExpression right) {
    super(left, right);
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitAddition(this, context);
  }

  /**
   * Get the sum of two atomic items.
   *
   * @param left
   *          the first item to sum
   * @param right
   *          the second item to sum
   * @return the sum of both items or an empty {@link ISequence} if either item is {@code null}
   */
  @Override
  protected IAnyAtomicItem operation(@NonNull IAnyAtomicItem left, @NonNull IAnyAtomicItem right) {
    return sum(left, right);
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
  @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.CognitiveComplexity" })
  @NonNull
  public static IAnyAtomicItem sum(
      @NonNull IAnyAtomicItem leftItem, // NOPMD - intentional
      @NonNull IAnyAtomicItem rightItem) {
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
          String.format("The expression '%s + %s' is not supported", leftItem.getClass().getName(),
              rightItem.getClass().getName()));
    }
    return retval;
  }
}
