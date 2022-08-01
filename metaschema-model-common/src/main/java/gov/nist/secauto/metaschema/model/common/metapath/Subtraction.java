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
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDateItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDateTimeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IYearMonthDurationItem;

import edu.umd.cs.findbugs.annotations.NonNull;

class Subtraction
    extends AbstractBasicArithmeticExpression {

  /**
   * An expression that gets the difference of two atomic data items.
   * 
   * @param minuend
   *          an expression whose result is the value being subtracted from
   * @param subtrahend
   *          an expression whose result is the value being subtracted
   */
  protected Subtraction(@NonNull IExpression minuend, @NonNull IExpression subtrahend) {
    super(minuend, subtrahend);
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitSubtraction(this, context);
  }

  /**
   * Get the difference of two atomic items.
   * 
   * @param minuend
   *          the item being subtracted from
   * @param subtrahend
   *          the item being subtracted
   * @return the difference of the items or an empty {@link ISequence} if either item is {@code null}
   */
  @Override
  @NonNull
  protected IAnyAtomicItem operation(@NonNull IAnyAtomicItem minuend, @NonNull IAnyAtomicItem subtrahend) {
    return subtract(minuend, subtrahend);
  }

  /**
   * Get the difference of two atomic items.
   * 
   * @param minuend
   *          the item being subtracted from
   * @param subtrahend
   *          the item being subtracted
   * @return the difference of the items
   */
  @NonNull
  public static IAnyAtomicItem subtract(@NonNull IAnyAtomicItem minuend, // NOPMD - intentional
      @NonNull IAnyAtomicItem subtrahend) {

    IAnyAtomicItem retval = null;
    if (minuend instanceof IDateItem) {
      IDateItem left = (IDateItem) minuend;

      if (subtrahend instanceof IDateItem) {
        retval = OperationFunctions.opSubtractDates(left, (IDateItem) subtrahend);
      } else if (subtrahend instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opSubtractYearMonthDurationFromDate(left, (IYearMonthDurationItem) subtrahend);
      } else if (subtrahend instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opSubtractDayTimeDurationFromDate(left, (IDayTimeDurationItem) subtrahend);
      }
    } else if (minuend instanceof IDateTimeItem) {
      IDateTimeItem left = (IDateTimeItem) minuend;
      if (subtrahend instanceof IDateTimeItem) {
        retval = OperationFunctions.opSubtractDateTimes(left, (IDateTimeItem) subtrahend);
      } else if (subtrahend instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opSubtractYearMonthDurationFromDateTime(left, (IYearMonthDurationItem) subtrahend);
      } else if (subtrahend instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opSubtractDayTimeDurationFromDateTime(left, (IDayTimeDurationItem) subtrahend);
      }
    } else if (minuend instanceof IYearMonthDurationItem) {
      IYearMonthDurationItem left = (IYearMonthDurationItem) minuend;
      if (subtrahend instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opSubtractYearMonthDurations(left, (IYearMonthDurationItem) subtrahend);
      }
    } else if (minuend instanceof IDayTimeDurationItem) {
      IDayTimeDurationItem left = (IDayTimeDurationItem) minuend;
      if (subtrahend instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opSubtractDayTimeDurations(left, (IDayTimeDurationItem) subtrahend);
      }
    } else {
      // handle as numeric
      INumericItem left = FunctionUtils.toNumeric(minuend);
      INumericItem right = FunctionUtils.toNumeric(subtrahend);
      retval = OperationFunctions.opNumericSubtract(left, right);
    }
    if (retval == null) {
      throw new InvalidTypeMetapathException(
          null,
          String.format("The expression '%s - %s' is not supported", minuend.getClass().getName(),
              subtrahend.getClass().getName()));
    }
    return retval;
  }
}
