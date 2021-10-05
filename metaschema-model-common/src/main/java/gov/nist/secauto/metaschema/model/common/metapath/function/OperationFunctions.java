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

package gov.nist.secauto.metaschema.model.common.metapath.function;

import gov.nist.secauto.metaschema.model.common.metapath.item.IBase64BinaryItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDateItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDateTimeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IYearMonthDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.type.InvalidTypeMetapathException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;

public class OperationFunctions {
  private OperationFunctions() {
    // disable
  }

  public static IDateItem opAddYearMonthDurationToDate(IDateItem arg1, IYearMonthDurationItem arg2) {
    return addDurationToDate(arg1.asZonedDateTime(), arg2.getValue());
  }

  public static IDateItem opAddDayTimeDurationToDate(IDateItem arg1, IDayTimeDurationItem arg2) {
    return addDurationToDate(arg1.asZonedDateTime(), arg2.getValue());
  }

  protected static IDateItem addDurationToDate(ZonedDateTime dateTime, TemporalAmount duration) {
    ZonedDateTime result = dateTime.plus(duration);
    return IDateItem.valueOf(result);
  }

  public static IYearMonthDurationItem opAddYearMonthDurations(IYearMonthDurationItem arg1,
      IYearMonthDurationItem arg2) {
    Period duration1 = arg1.getValue();
    Period duration2 = arg2.getValue();
    return IYearMonthDurationItem.valueOf(duration1.plus(duration2));
  }

  public static IDayTimeDurationItem opAddDayTimeDurations(IDayTimeDurationItem arg1, IDayTimeDurationItem arg2) {
    Duration duration1 = arg1.getValue();
    Duration duration2 = arg2.getValue();
    return IDayTimeDurationItem.valueOf(duration1.plus(duration2));
  }

  public static IDateTimeItem opAddYearMonthDurationToDateTime(IDateTimeItem arg1, IYearMonthDurationItem arg2) {
    return IDateTimeItem.valueOf(arg1.asZonedDateTime().plus(arg2.getValue()));
  }

  public static IDateTimeItem opAddDayTimeDurationToDateTime(IDateTimeItem arg1, IDayTimeDurationItem arg2) {
    return IDateTimeItem.valueOf(arg1.asZonedDateTime().plus(arg2.getValue()));
  }

  public static IDayTimeDurationItem opSubtractDates(IDateItem arg1, IDateItem arg2) {
    return between(arg1.asZonedDateTime(), arg2.asZonedDateTime());
  }

  public static IDateItem opSubtractYearMonthDurationFromDate(IDateItem arg1, IYearMonthDurationItem arg2) {
    return subtractDurationFromDate(arg1.asZonedDateTime(), arg2.getValue());
  }

  public static IDateItem opSubtractDayTimeDurationFromDate(IDateItem arg1, IDayTimeDurationItem arg2) {
    return subtractDurationFromDate(arg1.asZonedDateTime(), arg2.getValue());
  }

  protected static IDateItem subtractDurationFromDate(ZonedDateTime dateTime, TemporalAmount duration) {
    ZonedDateTime result = dateTime.minus(duration);
    return IDateItem.valueOf(result);
  }

  public static IYearMonthDurationItem opSubtractYearMonthDurations(IYearMonthDurationItem arg1,
      IYearMonthDurationItem arg2) {
    Period duration1 = arg1.getValue();
    Period duration2 = arg2.getValue();
    return IYearMonthDurationItem.valueOf(duration1.minus(duration2));
  }

  public static IDayTimeDurationItem opSubtractDayTimeDurations(IDayTimeDurationItem arg1, IDayTimeDurationItem arg2) {
    Duration duration1 = arg1.getValue();
    Duration duration2 = arg2.getValue();
    return IDayTimeDurationItem.valueOf(duration1.minus(duration2));
  }

  public static IDayTimeDurationItem opSubtractDateTimes(IDateTimeItem arg1, IDateTimeItem arg2) {
    return between(arg1.asZonedDateTime(), arg2.asZonedDateTime());
  }

  protected static IDayTimeDurationItem between(ZonedDateTime time1, ZonedDateTime time2) {
    Duration between = Duration.between(time1, time2);
    return IDayTimeDurationItem.valueOf(between);
  }

  public static IDateTimeItem opSubtractYearMonthDurationFromDateTime(IDateTimeItem arg1, IYearMonthDurationItem arg2) {
    return IDateTimeItem.valueOf(arg1.asZonedDateTime().minus(arg2.getValue()));
  }

  public static IDateTimeItem opSubtractDayTimeDurationFromDateTime(IDateTimeItem arg1, IDayTimeDurationItem arg2) {
    return IDateTimeItem.valueOf(arg1.asZonedDateTime().plus(arg2.getValue()));
  }

  public static IYearMonthDurationItem opMultiplyYearMonthDuration(IYearMonthDurationItem arg1, INumericItem arg2)
      throws ArithmeticFunctionException {
    int arg2Int;
    try {
      arg2Int = FunctionUtils.asInteger(XPathFunctions.fnRound(arg2));
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }
    return IYearMonthDurationItem.valueOf(arg1.getValue().multipliedBy(arg2Int));
  }

  public static IDayTimeDurationItem opMultiplyDayTimeDuration(IDayTimeDurationItem arg1, INumericItem arg2)
      throws ArithmeticFunctionException {
    long arg2Long;
    try {
      arg2Long = FunctionUtils.asLong(XPathFunctions.fnRound(arg2));
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }
    return IDayTimeDurationItem.valueOf(arg1.getValue().multipliedBy(arg2Long));
  }

  public static IYearMonthDurationItem opDivideYearMonthDuration(IYearMonthDurationItem arg1, INumericItem arg2)
      throws DateTimeFunctionException {
    IIntegerItem totalMonths = IIntegerItem.valueOf(arg1.getValue().toTotalMonths());
    IIntegerItem result = opNumericIntegerDivide(totalMonths, arg2);
    int months;
    try {
      months = FunctionUtils.asInteger(result.asInteger());
    } catch (ArithmeticException ex) {
      throw new DateTimeFunctionException(DateTimeFunctionException.DURATION_OVERFLOW_UNDERFLOW_ERROR,
          "Overflow/underflow in duration operation.", ex);
    }
    int years = months / 12;
    months = months % 12;
    return IYearMonthDurationItem.valueOf(years, months, 0);
  }

  public static IDayTimeDurationItem opDivideDayTimeDuration(IDayTimeDurationItem arg1, INumericItem arg2)
      throws ArithmeticFunctionException {
    try {
      return IDayTimeDurationItem
          .valueOf(arg1.getValue().dividedBy(FunctionUtils.asLong(XPathFunctions.fnRound(arg2))));
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO, "Division by zero", ex);
    }
  }

  public static IDecimalItem opDivideDayTimeDurationByDayTimeDuration(IDayTimeDurationItem arg1,
      IDayTimeDurationItem arg2) {
    return CastFunctions.castToDecimal(opNumericDivide(IDecimalItem.valueOf(arg1.getValue().toSeconds()),
        IDecimalItem.valueOf(arg1.getValue().toSeconds())));
  }

  public static IBooleanItem opDateEqual(IDateItem arg1, IDateItem arg2) {
    return opDateTimeEqual(CastFunctions.castToDateTime(arg1), CastFunctions.castToDateTime(arg2));
  }

  public static IBooleanItem opDateTimeEqual(IDateTimeItem arg1, IDateTimeItem arg2) {
    return IBooleanItem.valueOf(arg1.asZonedDateTime().equals(arg2.asZonedDateTime()));
  }

  public static IBooleanItem opDurationEqual(IDurationItem arg1, IDurationItem arg2) {
    return IBooleanItem.valueOf(arg1.getValue().equals(arg2.getValue()));
  }

  public static IBooleanItem opBase64BinaryEqual(IBase64BinaryItem arg1, IBase64BinaryItem arg2) {
    return IBooleanItem.valueOf(arg1.getValue().equals(arg2.getValue()));
  }

  public static IBooleanItem opDateGreaterThan(IDateItem arg1, IDateItem arg2) {
    return opDateTimeGreaterThan(CastFunctions.castToDateTime(arg1), CastFunctions.castToDateTime(arg2));
  }

  public static IBooleanItem opDateTimeGreaterThan(IDateTimeItem arg1, IDateTimeItem arg2) {
    return IBooleanItem.valueOf(arg1.asZonedDateTime().compareTo(arg2.asZonedDateTime()) > 0);
  }

  public static IBooleanItem opYearMonthDurationGreaterThan(IYearMonthDurationItem arg1, IYearMonthDurationItem arg2) {
    Period p1 = arg1.getValue();
    Period p2 = arg2.getValue();

    // this is only an approximation
    return IBooleanItem.valueOf(p1.toTotalMonths() > p2.toTotalMonths());
  }

  public static IBooleanItem opDayTimeDurationGreaterThan(IDayTimeDurationItem arg1, IDayTimeDurationItem arg2) {
    return IBooleanItem.valueOf(arg1.getValue().compareTo(arg1.getValue()) > 0);
  }

  public static IBooleanItem opBase64BinaryGreaterThan(IBase64BinaryItem arg1, IBase64BinaryItem arg2) {
    return IBooleanItem.valueOf(arg1.getValue().compareTo(arg1.getValue()) > 0);
  }

  public static IBooleanItem opDateLessThan(IDateItem arg1, IDateItem arg2) {
    return opDateTimeLessThan(CastFunctions.castToDateTime(arg1), CastFunctions.castToDateTime(arg2));
  }

  public static IBooleanItem opDateTimeLessThan(IDateTimeItem arg1, IDateTimeItem arg2) {
    return IBooleanItem.valueOf(arg1.asZonedDateTime().compareTo(arg2.asZonedDateTime()) < 0);
  }

  public static IBooleanItem opYearMonthDurationLessThan(IYearMonthDurationItem arg1, IYearMonthDurationItem arg2) {
    Period p1 = arg1.getValue();
    Period p2 = arg2.getValue();

    // this is only an approximation
    return IBooleanItem.valueOf(p1.toTotalMonths() < p2.toTotalMonths());
  }

  public static IBooleanItem opDayTimeDurationLessThan(IDayTimeDurationItem arg1, IDayTimeDurationItem arg2) {
    return IBooleanItem.valueOf(arg1.getValue().compareTo(arg1.getValue()) < 0);
  }

  public static IBooleanItem opBase64BinaryLessThan(IBase64BinaryItem arg1, IBase64BinaryItem arg2) {
    return IBooleanItem.valueOf(arg1.getValue().compareTo(arg1.getValue()) < 0);
  }

  public static INumericItem opNumericAdd(INumericItem left, INumericItem right) {
    INumericItem retval;
    if (left instanceof IDecimalItem || right instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimalLeft;
      if (left instanceof IIntegerItem) {
        decimalLeft = ((IIntegerItem) left).asDecimal();
      } else if (left instanceof IDecimalItem) {
        decimalLeft = ((IDecimalItem) left).asDecimal();
      } else {
        throw new InvalidTypeMetapathException(left);
      }

      BigDecimal decimalRight;
      if (left instanceof IIntegerItem) {
        decimalRight = ((IIntegerItem) right).asDecimal();
      } else if (left instanceof IDecimalItem) {
        decimalRight = ((IDecimalItem) right).asDecimal();
      } else {
        throw new InvalidTypeMetapathException(right);
      }
      BigDecimal result = decimalLeft.add(decimalRight, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger integerLeft;
      if (left instanceof IIntegerItem) {
        integerLeft = ((IIntegerItem) left).asInteger();
      } else if (left instanceof IDecimalItem) {
        integerLeft = ((IDecimalItem) left).asInteger();
      } else {
        throw new InvalidTypeMetapathException(left);
      }

      BigInteger integerRight;
      if (left instanceof IIntegerItem) {
        integerRight = ((IIntegerItem) right).asInteger();
      } else if (left instanceof IDecimalItem) {
        integerRight = ((IDecimalItem) right).asInteger();
      } else {
        throw new InvalidTypeMetapathException(right);
      }
      BigInteger result = integerLeft.add(integerRight);
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  public static INumericItem opNumericSubtract(INumericItem left, INumericItem right) {
    INumericItem retval;
    if (left instanceof IDecimalItem || right instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimalLeft;
      if (left instanceof IIntegerItem) {
        decimalLeft = ((IIntegerItem) left).asDecimal();
      } else if (left instanceof IDecimalItem) {
        decimalLeft = ((IDecimalItem) left).asDecimal();
      } else {
        throw new InvalidTypeMetapathException(left);
      }

      BigDecimal decimalRight;
      if (left instanceof IIntegerItem) {
        decimalRight = ((IIntegerItem) right).asDecimal();
      } else if (left instanceof IDecimalItem) {
        decimalRight = ((IDecimalItem) right).asDecimal();
      } else {
        throw new InvalidTypeMetapathException(right);
      }
      BigDecimal result = decimalLeft.subtract(decimalRight, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger integerLeft;
      if (left instanceof IIntegerItem) {
        integerLeft = ((IIntegerItem) left).asInteger();
      } else if (left instanceof IDecimalItem) {
        integerLeft = ((IDecimalItem) left).asInteger();
      } else {
        throw new InvalidTypeMetapathException(left);
      }

      BigInteger integerRight;
      if (left instanceof IIntegerItem) {
        integerRight = ((IIntegerItem) right).asInteger();
      } else if (left instanceof IDecimalItem) {
        integerRight = ((IDecimalItem) right).asInteger();
      } else {
        throw new InvalidTypeMetapathException(right);
      }
      BigInteger result = integerLeft.subtract(integerRight);
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  public static INumericItem opNumericMultiply(INumericItem left, INumericItem right) {
    INumericItem retval;
    if (left instanceof IDecimalItem || right instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimalLeft;
      if (left instanceof IIntegerItem) {
        decimalLeft = ((IIntegerItem) left).asDecimal();
      } else if (left instanceof IDecimalItem) {
        decimalLeft = ((IDecimalItem) left).asDecimal();
      } else {
        throw new InvalidTypeMetapathException(left);
      }

      BigDecimal decimalRight;
      if (left instanceof IIntegerItem) {
        decimalRight = ((IIntegerItem) right).asDecimal();
      } else if (left instanceof IDecimalItem) {
        decimalRight = ((IDecimalItem) right).asDecimal();
      } else {
        throw new InvalidTypeMetapathException(right);
      }
      BigDecimal result = decimalLeft.multiply(decimalRight, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger result = left.asInteger().multiply(right.asInteger());
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  public static INumericItem opNumericDivide(INumericItem dividend, INumericItem divisor) {
    INumericItem retval;
    if (dividend instanceof IDecimalItem || divisor instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimalDivisor = divisor.asDecimal();

      if (BigDecimal.ZERO.equals(decimalDivisor)) {
        throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO,
            ArithmeticFunctionException.DIVISION_BY_ZERO_MESSAGE);
      }

      BigDecimal decimalDividend = dividend.asDecimal();
      BigDecimal result = decimalDividend.divide(decimalDivisor, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger integerDivisor = divisor.asInteger();

      if (BigInteger.ZERO.equals(integerDivisor)) {
        throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO,
            ArithmeticFunctionException.DIVISION_BY_ZERO_MESSAGE);
      }

      BigInteger integerDividend = dividend.asInteger();
      BigInteger result = integerDividend.divide(integerDivisor);
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  public static IIntegerItem opNumericIntegerDivide(INumericItem dividend, INumericItem divisor) {
    IIntegerItem retval;
    if (dividend instanceof IDecimalItem || divisor instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimalDivisor = divisor.asDecimal();

      if (BigDecimal.ZERO.equals(decimalDivisor)) {
        throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO,
            ArithmeticFunctionException.DIVISION_BY_ZERO_MESSAGE);
      }

      BigDecimal decimalDividend = dividend.asDecimal();
      BigInteger result
          = decimalDividend.divideToIntegralValue(decimalDivisor, FunctionUtils.MATH_CONTEXT).toBigInteger();
      retval = IIntegerItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger integerDivisor = divisor.asInteger();

      if (BigInteger.ZERO.equals(integerDivisor)) {
        throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO,
            ArithmeticFunctionException.DIVISION_BY_ZERO_MESSAGE);
      }

      BigInteger result = dividend.asInteger().divide(integerDivisor);
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  /**
   * Based on XPath 3.1
   * <a href="https://www.w3.org/TR/xpath-functions-31/#func-numeric-mod">func:numeric-mod</a>.
   * 
   * @param dividend
   *          the number to be divided
   * @param divisor
   *          the number to divide by
   * @return the remainder
   */
  public static INumericItem opNumericMod(INumericItem dividend, INumericItem divisor) {
    BigDecimal decimalDivisor;
    if (divisor instanceof IIntegerItem) {
      decimalDivisor = ((IIntegerItem) divisor).asDecimal();
    } else if (divisor instanceof IDecimalItem) {
      decimalDivisor = ((IDecimalItem) divisor).asDecimal();
    } else {
      throw new InvalidTypeMetapathException(divisor);
    }

    if (BigDecimal.ZERO.equals(decimalDivisor)) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO,
          ArithmeticFunctionException.DIVISION_BY_ZERO_MESSAGE);
    }

    BigDecimal decimalDividend;
    if (dividend instanceof IIntegerItem) {
      decimalDividend = ((IIntegerItem) dividend).asDecimal();
    } else if (dividend instanceof IDecimalItem) {
      decimalDividend = ((IDecimalItem) dividend).asDecimal();
    } else {
      throw new InvalidTypeMetapathException(dividend);
    }

    INumericItem retval;
    if (BigDecimal.ZERO.equals(decimalDividend)) {
      retval = dividend;
    } else {
      BigDecimal result = decimalDividend.remainder(decimalDivisor, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    }
    return retval;
  }

  public static INumericItem opNumericUnaryMinus(INumericItem item) {
    INumericItem retval;
    if (item instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimal = item.asDecimal();
      BigDecimal result = decimal.negate(FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else if (item instanceof IIntegerItem) {
      // create a decimal result
      BigInteger integer = item.asInteger();
      BigInteger result = integer.negate();
      retval = IIntegerItem.valueOf(result);
    } else {
      throw new InvalidTypeMetapathException(item);
    }
    return retval;
  }

  public static IBooleanItem opNumericEqual(INumericItem arg1, INumericItem arg2) {
    IBooleanItem retval;
    if (arg1 == null || arg2 == null) {
      retval = IBooleanItem.FALSE;
    } else if (arg1 instanceof IDecimalItem || arg2 instanceof IDecimalItem) {
      retval = IBooleanItem.valueOf(arg1.asDecimal().equals(arg2.asDecimal()));
    } else {
      retval = IBooleanItem.valueOf(arg1.asInteger().equals(arg2.asInteger()));
    }
    return retval;
  }

  public static IBooleanItem opNumericGreaterThan(INumericItem arg1, INumericItem arg2) {
    IBooleanItem retval;
    if (arg1 == null || arg2 == null) {
      retval = IBooleanItem.FALSE;
    } else if (arg1 instanceof IDecimalItem || arg2 instanceof IDecimalItem) {
      int result = arg1.asDecimal().compareTo(arg2.asDecimal());
      retval = IBooleanItem.valueOf(result > 0);
    } else {
      int result = arg1.asInteger().compareTo(arg2.asInteger());
      retval = IBooleanItem.valueOf(result > 0);
    }
    return retval;
  }

  public static IBooleanItem opNumericLessThan(INumericItem arg1, INumericItem arg2) {
    IBooleanItem retval;
    if (arg1 == null || arg2 == null) {
      retval = IBooleanItem.FALSE;
    } else if (arg1 instanceof IDecimalItem || arg2 instanceof IDecimalItem) {
      int result = arg1.asDecimal().compareTo(arg2.asDecimal());
      retval = IBooleanItem.valueOf(result < 0);
    } else {
      int result = arg1.asInteger().compareTo(arg2.asInteger());
      retval = IBooleanItem.valueOf(result < 0);
    }
    return retval;
  }

  public static IBooleanItem opBooleanEqual(IBooleanItem arg1, IBooleanItem arg2) {
    if (arg1 == null) {
      arg1 = IBooleanItem.FALSE;
    }
    if (arg2 == null) {
      arg2 = IBooleanItem.FALSE;
    }

    return IBooleanItem.valueOf(arg1.toBoolean() == arg2.toBoolean());
  }

  public static IBooleanItem opBooleanGreaterThan(IBooleanItem arg1, IBooleanItem arg2) {
    if (arg1 == null) {
      arg1 = IBooleanItem.FALSE;
    }
    if (arg2 == null) {
      arg2 = IBooleanItem.FALSE;
    }

    return IBooleanItem.valueOf(arg1.toBoolean() && !arg2.toBoolean());
  }

  public static IBooleanItem opBooleanLessThan(IBooleanItem arg1, IBooleanItem arg2) {
    if (arg1 == null) {
      arg1 = IBooleanItem.FALSE;
    }
    if (arg2 == null) {
      arg2 = IBooleanItem.FALSE;
    }

    return IBooleanItem.valueOf(!arg1.toBoolean() && arg2.toBoolean());
  }
}
