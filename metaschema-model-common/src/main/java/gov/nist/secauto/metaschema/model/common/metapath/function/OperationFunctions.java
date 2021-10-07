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

import gov.nist.secauto.metaschema.model.common.datatype.adapter.IBase64BinaryItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDateItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDateTimeItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDurationItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.INumericItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IYearMonthDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.type.InvalidTypeMetapathException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  @NotNull
  public static IDateItem opAddYearMonthDurationToDate(@NotNull IDateItem arg1, @NotNull IYearMonthDurationItem arg2) {
    return addDurationToDate(arg1.asZonedDateTime(), arg2.getValue());
  }

  @NotNull
  public static IDateItem opAddDayTimeDurationToDate(@NotNull IDateItem arg1, @NotNull IDayTimeDurationItem arg2) {
    return addDurationToDate(arg1.asZonedDateTime(), arg2.getValue());
  }

  @NotNull
  protected static IDateItem addDurationToDate(@NotNull ZonedDateTime dateTime, @NotNull TemporalAmount duration) {

    @SuppressWarnings("null")
    @NotNull ZonedDateTime result = dateTime.plus(duration);
    return IDateItem.valueOf(result);
  }

  @NotNull
  public static IYearMonthDurationItem opAddYearMonthDurations(@NotNull IYearMonthDurationItem arg1,
      IYearMonthDurationItem arg2) {
    Period duration1 = arg1.getValue();
    Period duration2 = arg2.getValue();

    @SuppressWarnings("null") IYearMonthDurationItem retval = IYearMonthDurationItem.valueOf(duration1.plus(duration2));
    return retval;
  }

  @NotNull
  public static IDayTimeDurationItem opAddDayTimeDurations(@NotNull IDayTimeDurationItem arg1,
      @NotNull IDayTimeDurationItem arg2) {
    Duration duration1 = arg1.getValue();
    Duration duration2 = arg2.getValue();

    @SuppressWarnings("null") IDayTimeDurationItem retval = IDayTimeDurationItem.valueOf(duration1.plus(duration2));
    return retval;
  }

  @NotNull
  public static IDateTimeItem opAddYearMonthDurationToDateTime(@NotNull IDateTimeItem arg1,
      @NotNull IYearMonthDurationItem arg2) {
    @SuppressWarnings("null") IDateTimeItem retval
        = IDateTimeItem.valueOf(arg1.asZonedDateTime().plus(arg2.getValue()));
    return retval;
  }

  @NotNull
  public static IDateTimeItem opAddDayTimeDurationToDateTime(@NotNull IDateTimeItem arg1,
      @NotNull IDayTimeDurationItem arg2) {
    @SuppressWarnings("null") IDateTimeItem retval
        = IDateTimeItem.valueOf(arg1.asZonedDateTime().plus(arg2.getValue()));
    return retval;
  }

  @NotNull
  public static IDayTimeDurationItem opSubtractDates(@NotNull IDateItem arg1, @NotNull IDateItem arg2) {
    return between(arg1.asZonedDateTime(), arg2.asZonedDateTime());
  }

  @NotNull
  public static IDateItem opSubtractYearMonthDurationFromDate(@NotNull IDateItem arg1,
      @NotNull IYearMonthDurationItem arg2) {
    return subtractDurationFromDate(arg1.asZonedDateTime(), arg2.getValue());
  }

  @NotNull
  public static IDateItem opSubtractDayTimeDurationFromDate(@NotNull IDateItem arg1,
      @NotNull IDayTimeDurationItem arg2) {
    return subtractDurationFromDate(arg1.asZonedDateTime(), arg2.getValue());
  }

  @NotNull
  protected static IDateItem subtractDurationFromDate(@NotNull ZonedDateTime dateTime,
      @NotNull TemporalAmount duration) {
    @SuppressWarnings("null")
    @NotNull ZonedDateTime result = dateTime.minus(duration);
    return IDateItem.valueOf(result);
  }

  @NotNull
  public static IYearMonthDurationItem opSubtractYearMonthDurations(@NotNull IYearMonthDurationItem arg1,
      IYearMonthDurationItem arg2) {
    Period duration1 = arg1.getValue();
    Period duration2 = arg2.getValue();

    @SuppressWarnings("null")
    @NotNull Period duration = duration1.minus(duration2);
    return IYearMonthDurationItem.valueOf(duration);
  }

  @NotNull
  public static IDayTimeDurationItem opSubtractDayTimeDurations(@NotNull IDayTimeDurationItem arg1,
      @NotNull IDayTimeDurationItem arg2) {
    Duration duration1 = arg1.getValue();
    Duration duration2 = arg2.getValue();

    @SuppressWarnings("null")
    @NotNull Duration duration = duration1.minus(duration2);
    return IDayTimeDurationItem.valueOf(duration);
  }

  @NotNull
  public static IDayTimeDurationItem opSubtractDateTimes(@NotNull IDateTimeItem arg1, @NotNull IDateTimeItem arg2) {
    return between(arg1.asZonedDateTime(), arg2.asZonedDateTime());
  }

  @NotNull
  protected static IDayTimeDurationItem between(@NotNull ZonedDateTime time1, @NotNull ZonedDateTime time2) {
    @SuppressWarnings("null")
    @NotNull Duration between = Duration.between(time1, time2);
    return IDayTimeDurationItem.valueOf(between);
  }

  @NotNull
  public static IDateTimeItem opSubtractYearMonthDurationFromDateTime(@NotNull IDateTimeItem arg1,
      @NotNull IYearMonthDurationItem arg2) {
    @SuppressWarnings("null")
    @NotNull ZonedDateTime dateTime = arg1.asZonedDateTime().minus(arg2.getValue());
    return IDateTimeItem.valueOf(dateTime);
  }

  @NotNull
  public static IDateTimeItem opSubtractDayTimeDurationFromDateTime(@NotNull IDateTimeItem arg1,
      @NotNull IDayTimeDurationItem arg2) {

    @SuppressWarnings("null")
    @NotNull ZonedDateTime dateTime = arg1.asZonedDateTime().plus(arg2.getValue());
    return IDateTimeItem.valueOf(dateTime);
  }

  @NotNull
  public static IYearMonthDurationItem opMultiplyYearMonthDuration(@NotNull IYearMonthDurationItem arg1,
      @NotNull INumericItem arg2)
      throws ArithmeticFunctionException {
    int arg2Int;
    try {
      arg2Int = FunctionUtils.asInteger(XPathFunctions.fnRound(arg2));
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }

    @SuppressWarnings("null")
    @NotNull Period period = arg1.getValue().multipliedBy(arg2Int);
    return IYearMonthDurationItem.valueOf(period);
  }

  @NotNull
  public static IDayTimeDurationItem opMultiplyDayTimeDuration(@NotNull IDayTimeDurationItem arg1,
      @NotNull INumericItem arg2)
      throws ArithmeticFunctionException {
    long arg2Long;
    try {
      arg2Long = FunctionUtils.asLong(XPathFunctions.fnRound(arg2));
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }

    @SuppressWarnings("null")
    @NotNull Duration duration = arg1.getValue().multipliedBy(arg2Long);
    return IDayTimeDurationItem.valueOf(duration);
  }

  @NotNull
  public static IYearMonthDurationItem opDivideYearMonthDuration(@NotNull IYearMonthDurationItem arg1,
      @NotNull INumericItem arg2)
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

  @NotNull
  public static IDayTimeDurationItem opDivideDayTimeDuration(@NotNull IDayTimeDurationItem arg1,
      @NotNull INumericItem arg2)
      throws ArithmeticFunctionException {
    try {
      @SuppressWarnings("null")
      @NotNull Duration duration = arg1.getValue().dividedBy(FunctionUtils.asLong(XPathFunctions.fnRound(arg2)));
      return IDayTimeDurationItem
          .valueOf(duration);
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO, "Division by zero", ex);
    }
  }

  @NotNull
  public static IDecimalItem opDivideDayTimeDurationByDayTimeDuration(@NotNull IDayTimeDurationItem arg1,
      IDayTimeDurationItem arg2) {
    return IDecimalItem.cast(opNumericDivide(IDecimalItem.valueOf(arg1.getValue().toSeconds()),
        IDecimalItem.valueOf(arg1.getValue().toSeconds())));
  }

  @NotNull
  public static IBooleanItem opDateEqual(@NotNull IDateItem arg1, @NotNull IDateItem arg2) {
    return opDateTimeEqual(IDateTimeItem.cast(arg1), IDateTimeItem.cast(arg2));
  }

  @NotNull
  public static IBooleanItem opDateTimeEqual(@NotNull IDateTimeItem arg1, @NotNull IDateTimeItem arg2) {
    return IBooleanItem.valueOf(arg1.asZonedDateTime().equals(arg2.asZonedDateTime()));
  }

  @NotNull
  public static IBooleanItem opDurationEqual(@NotNull IDurationItem arg1, @NotNull IDurationItem arg2) {
    return IBooleanItem.valueOf(arg1.getValue().equals(arg2.getValue()));
  }

  @NotNull
  public static IBooleanItem opBase64BinaryEqual(@NotNull IBase64BinaryItem arg1, @NotNull IBase64BinaryItem arg2) {
    return IBooleanItem.valueOf(arg1.getValue().equals(arg2.getValue()));
  }

  @NotNull
  public static IBooleanItem opDateGreaterThan(@NotNull IDateItem arg1, @NotNull IDateItem arg2) {
    return opDateTimeGreaterThan(IDateTimeItem.cast(arg1), IDateTimeItem.cast(arg2));
  }

  @NotNull
  public static IBooleanItem opDateTimeGreaterThan(@NotNull IDateTimeItem arg1, @NotNull IDateTimeItem arg2) {
    return IBooleanItem.valueOf(arg1.asZonedDateTime().compareTo(arg2.asZonedDateTime()) > 0);
  }

  @NotNull
  public static IBooleanItem opYearMonthDurationGreaterThan(@NotNull IYearMonthDurationItem arg1,
      @NotNull IYearMonthDurationItem arg2) {
    Period p1 = arg1.getValue();
    Period p2 = arg2.getValue();

    // this is only an approximation
    return IBooleanItem.valueOf(p1.toTotalMonths() > p2.toTotalMonths());
  }

  @NotNull
  public static IBooleanItem opDayTimeDurationGreaterThan(@NotNull IDayTimeDurationItem arg1,
      @NotNull IDayTimeDurationItem arg2) {
    return IBooleanItem.valueOf(arg1.getValue().compareTo(arg1.getValue()) > 0);
  }

  @NotNull
  public static IBooleanItem opBase64BinaryGreaterThan(@NotNull IBase64BinaryItem arg1,
      @NotNull IBase64BinaryItem arg2) {
    return IBooleanItem.valueOf(arg1.getValue().compareTo(arg1.getValue()) > 0);
  }

  @NotNull
  public static IBooleanItem opDateLessThan(@NotNull IDateItem arg1, @NotNull IDateItem arg2) {
    return opDateTimeLessThan(IDateTimeItem.cast(arg1), IDateTimeItem.cast(arg2));
  }

  @NotNull
  public static IBooleanItem opDateTimeLessThan(@NotNull IDateTimeItem arg1, @NotNull IDateTimeItem arg2) {
    return IBooleanItem.valueOf(arg1.asZonedDateTime().compareTo(arg2.asZonedDateTime()) < 0);
  }

  @NotNull
  public static IBooleanItem opYearMonthDurationLessThan(@NotNull IYearMonthDurationItem arg1,
      @NotNull IYearMonthDurationItem arg2) {
    Period p1 = arg1.getValue();
    Period p2 = arg2.getValue();

    // this is only an approximation
    return IBooleanItem.valueOf(p1.toTotalMonths() < p2.toTotalMonths());
  }

  @NotNull
  public static IBooleanItem opDayTimeDurationLessThan(@NotNull IDayTimeDurationItem arg1,
      @NotNull IDayTimeDurationItem arg2) {
    return IBooleanItem.valueOf(arg1.getValue().compareTo(arg1.getValue()) < 0);
  }

  @NotNull
  public static IBooleanItem opBase64BinaryLessThan(@NotNull IBase64BinaryItem arg1, @NotNull IBase64BinaryItem arg2) {
    return IBooleanItem.valueOf(arg1.getValue().compareTo(arg1.getValue()) < 0);
  }

  @NotNull
  public static INumericItem opNumericAdd(@NotNull INumericItem left, @NotNull INumericItem right) {
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

      @SuppressWarnings("null")
      @NotNull BigDecimal result = decimalLeft.add(decimalRight, FunctionUtils.MATH_CONTEXT);
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

      @SuppressWarnings("null")
      @NotNull BigInteger result = integerLeft.add(integerRight);
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  @NotNull
  public static INumericItem opNumericSubtract(@NotNull INumericItem left, @NotNull INumericItem right) {
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

      @SuppressWarnings("null")
      @NotNull BigDecimal result = decimalLeft.subtract(decimalRight, FunctionUtils.MATH_CONTEXT);
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

      @SuppressWarnings("null")
      @NotNull BigInteger result = integerLeft.subtract(integerRight);
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  @NotNull
  public static INumericItem opNumericMultiply(@NotNull INumericItem left, @NotNull INumericItem right) {
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

      @SuppressWarnings("null")
      @NotNull BigDecimal result = decimalLeft.multiply(decimalRight, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      // create an integer result
      @SuppressWarnings("null")
      @NotNull BigInteger result = left.asInteger().multiply(right.asInteger());
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  @NotNull
  public static INumericItem opNumericDivide(@NotNull INumericItem dividend, @NotNull INumericItem divisor) {
    INumericItem retval;
    if (dividend instanceof IDecimalItem || divisor instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimalDivisor = divisor.asDecimal();

      if (BigDecimal.ZERO.equals(decimalDivisor)) {
        throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO,
            ArithmeticFunctionException.DIVISION_BY_ZERO_MESSAGE);
      }

      BigDecimal decimalDividend = dividend.asDecimal();

      @SuppressWarnings("null")
      @NotNull BigDecimal result = decimalDividend.divide(decimalDivisor, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger integerDivisor = divisor.asInteger();

      if (BigInteger.ZERO.equals(integerDivisor)) {
        throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO,
            ArithmeticFunctionException.DIVISION_BY_ZERO_MESSAGE);
      }

      BigInteger integerDividend = dividend.asInteger();

      @SuppressWarnings("null")
      @NotNull BigInteger result = integerDividend.divide(integerDivisor);
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  @NotNull
  public static IIntegerItem opNumericIntegerDivide(@NotNull INumericItem dividend, @NotNull INumericItem divisor) {
    IIntegerItem retval;
    if (dividend instanceof IDecimalItem || divisor instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimalDivisor = divisor.asDecimal();

      if (BigDecimal.ZERO.equals(decimalDivisor)) {
        throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO,
            ArithmeticFunctionException.DIVISION_BY_ZERO_MESSAGE);
      }

      BigDecimal decimalDividend = dividend.asDecimal();

      @SuppressWarnings("null")
      @NotNull BigInteger result
          = decimalDividend.divideToIntegralValue(decimalDivisor, FunctionUtils.MATH_CONTEXT).toBigInteger();
      retval = IIntegerItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger integerDivisor = divisor.asInteger();

      if (BigInteger.ZERO.equals(integerDivisor)) {
        throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO,
            ArithmeticFunctionException.DIVISION_BY_ZERO_MESSAGE);
      }

      @SuppressWarnings("null")
      @NotNull BigInteger result = dividend.asInteger().divide(integerDivisor);
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
  @NotNull
  public static INumericItem opNumericMod(@NotNull INumericItem dividend, @NotNull INumericItem divisor) {
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
      @SuppressWarnings("null")
      @NotNull BigDecimal result = decimalDividend.remainder(decimalDivisor, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    }
    return retval;
  }

  @NotNull
  public static INumericItem opNumericUnaryMinus(@NotNull INumericItem item) {
    INumericItem retval;
    if (item instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimal = item.asDecimal();

      @SuppressWarnings("null")
      @NotNull BigDecimal result = decimal.negate(FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else if (item instanceof IIntegerItem) {
      // create a decimal result
      BigInteger integer = item.asInteger();

      @SuppressWarnings("null")
      @NotNull BigInteger result = integer.negate();
      retval = IIntegerItem.valueOf(result);
    } else {
      throw new InvalidTypeMetapathException(item);
    }
    return retval;
  }

  @NotNull
  public static IBooleanItem opNumericEqual(@Nullable INumericItem arg1, @Nullable INumericItem arg2) {
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

  @NotNull
  public static IBooleanItem opNumericGreaterThan(@Nullable INumericItem arg1, @Nullable INumericItem arg2) {
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

  @NotNull
  public static IBooleanItem opNumericLessThan(@Nullable INumericItem arg1, @Nullable INumericItem arg2) {
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

  @NotNull
  public static IBooleanItem opBooleanEqual(@Nullable IBooleanItem arg1, @Nullable IBooleanItem arg2) {
    if (arg1 == null) {
      arg1 = IBooleanItem.FALSE;
    }
    if (arg2 == null) {
      arg2 = IBooleanItem.FALSE;
    }

    return IBooleanItem.valueOf(arg1.toBoolean() == arg2.toBoolean());
  }

  @NotNull
  public static IBooleanItem opBooleanGreaterThan(@Nullable IBooleanItem arg1, @Nullable IBooleanItem arg2) {
    if (arg1 == null) {
      arg1 = IBooleanItem.FALSE;
    }
    if (arg2 == null) {
      arg2 = IBooleanItem.FALSE;
    }

    return IBooleanItem.valueOf(arg1.toBoolean() && !arg2.toBoolean());
  }

  @NotNull
  public static IBooleanItem opBooleanLessThan(@Nullable IBooleanItem arg1, @Nullable IBooleanItem arg2) {
    if (arg1 == null) {
      arg1 = IBooleanItem.FALSE;
    }
    if (arg2 == null) {
      arg2 = IBooleanItem.FALSE;
    }

    return IBooleanItem.valueOf(!arg1.toBoolean() && arg2.toBoolean());
  }
}
