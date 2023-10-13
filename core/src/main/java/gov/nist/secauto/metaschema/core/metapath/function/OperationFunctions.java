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

package gov.nist.secauto.metaschema.core.metapath.function; // NOPMD - intentional

import gov.nist.secauto.metaschema.core.metapath.InvalidTypeMetapathException;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBase64BinaryItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDateItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDateTimeItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDecimalItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDurationItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IYearMonthDurationItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class OperationFunctions { // NOPMD - intentional
  @NonNull
  public static final Set<Class<? extends IAnyAtomicItem>> AGGREGATE_MATH_TYPES = ObjectUtils.notNull(Set.of(
      IDayTimeDurationItem.class,
      IYearMonthDurationItem.class,
      INumericItem.class));

  private OperationFunctions() {
    // disable
  }

  @NonNull
  public static IDateItem opAddYearMonthDurationToDate(@NonNull IDateItem arg1, @NonNull IYearMonthDurationItem arg2) {
    return addDurationToDate(arg1.asZonedDateTime(), arg2.asPeriod());
  }

  @NonNull
  public static IDateItem opAddDayTimeDurationToDate(@NonNull IDateItem arg1, @NonNull IDayTimeDurationItem arg2) {
    return addDurationToDate(arg1.asZonedDateTime(), arg2.asDuration());
  }

  @NonNull
  private static IDateItem addDurationToDate(@NonNull ZonedDateTime dateTime, @NonNull TemporalAmount duration) {
    ZonedDateTime result;
    try {
      result = dateTime.plus(duration);
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }
    assert result != null;
    return IDateItem.valueOf(result);
  }

  @NonNull
  public static IYearMonthDurationItem opAddYearMonthDurations(
      @NonNull IYearMonthDurationItem arg1,
      @NonNull IYearMonthDurationItem arg2) {
    Period duration1 = arg1.asPeriod();
    Period duration2 = arg2.asPeriod();

    Period result;
    try {
      result = duration1.plus(duration2);
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }
    assert result != null;
    return IYearMonthDurationItem.valueOf(result);
  }

  @NonNull
  public static IDayTimeDurationItem opAddDayTimeDurations(
      @NonNull IDayTimeDurationItem arg1,
      @NonNull IDayTimeDurationItem arg2) {
    Duration duration1 = arg1.asDuration();
    Duration duration2 = arg2.asDuration();

    Duration result;
    try {
      result = duration1.plus(duration2);
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }
    assert result != null;
    return IDayTimeDurationItem.valueOf(result);
  }

  @NonNull
  public static IDateTimeItem opAddYearMonthDurationToDateTime(
      @NonNull IDateTimeItem arg1,
      @NonNull IYearMonthDurationItem arg2) {
    ZonedDateTime result;
    try {
      result = arg1.asZonedDateTime().plus(arg2.asPeriod());
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }
    assert result != null;
    return IDateTimeItem.valueOf(result);
  }

  @NonNull
  public static IDateTimeItem opAddDayTimeDurationToDateTime(
      @NonNull IDateTimeItem arg1,
      @NonNull IDayTimeDurationItem arg2) {
    ZonedDateTime result;
    try {
      result = arg1.asZonedDateTime().plus(arg2.asDuration());
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }
    assert result != null;
    return IDateTimeItem.valueOf(result);
  }

  @NonNull
  public static IDayTimeDurationItem opSubtractDates(@NonNull IDateItem arg1, @NonNull IDateItem arg2) {
    return between(arg1.asZonedDateTime(), arg2.asZonedDateTime());
  }

  @NonNull
  public static IDateItem opSubtractYearMonthDurationFromDate(
      @NonNull IDateItem arg1,
      @NonNull IYearMonthDurationItem arg2) {
    return subtractDurationFromDate(arg1.asZonedDateTime(), arg2.asPeriod());
  }

  @NonNull
  public static IDateItem opSubtractDayTimeDurationFromDate(
      @NonNull IDateItem arg1,
      @NonNull IDayTimeDurationItem arg2) {
    return subtractDurationFromDate(arg1.asZonedDateTime(), arg2.asDuration());
  }

  @NonNull
  private static IDateItem subtractDurationFromDate(
      @NonNull ZonedDateTime dateTime,
      @NonNull TemporalAmount duration) {
    @SuppressWarnings("null")
    @NonNull ZonedDateTime result = dateTime.minus(duration);
    return IDateItem.valueOf(result);
  }

  @NonNull
  public static IYearMonthDurationItem opSubtractYearMonthDurations(
      @NonNull IYearMonthDurationItem arg1,
      @NonNull IYearMonthDurationItem arg2) {
    Period duration1 = arg1.asPeriod();
    Period duration2 = arg2.asPeriod();

    @SuppressWarnings("null")
    @NonNull Period duration = duration1.minus(duration2);
    return IYearMonthDurationItem.valueOf(duration);
  }

  @NonNull
  public static IDayTimeDurationItem opSubtractDayTimeDurations(
      @NonNull IDayTimeDurationItem arg1,
      @NonNull IDayTimeDurationItem arg2) {
    Duration duration1 = arg1.asDuration();
    Duration duration2 = arg2.asDuration();

    @SuppressWarnings("null")
    @NonNull Duration duration = duration1.minus(duration2);
    return IDayTimeDurationItem.valueOf(duration);
  }

  @NonNull
  public static IDayTimeDurationItem opSubtractDateTimes(@NonNull IDateTimeItem arg1, @NonNull IDateTimeItem arg2) {
    return between(arg1.asZonedDateTime(), arg2.asZonedDateTime());
  }

  @NonNull
  private static IDayTimeDurationItem between(@NonNull ZonedDateTime time1, @NonNull ZonedDateTime time2) {
    @SuppressWarnings("null")
    @NonNull Duration between = Duration.between(time1, time2);
    return IDayTimeDurationItem.valueOf(between);
  }

  @NonNull
  public static IDateTimeItem opSubtractYearMonthDurationFromDateTime(
      @NonNull IDateTimeItem arg1,
      @NonNull IYearMonthDurationItem arg2) {
    @SuppressWarnings("null")
    @NonNull ZonedDateTime dateTime = arg1.asZonedDateTime().minus(arg2.asPeriod());
    return IDateTimeItem.valueOf(dateTime);
  }

  @NonNull
  public static IDateTimeItem opSubtractDayTimeDurationFromDateTime(
      @NonNull IDateTimeItem arg1,
      @NonNull IDayTimeDurationItem arg2) {

    @SuppressWarnings("null")
    @NonNull ZonedDateTime dateTime = arg1.asZonedDateTime().plus(arg2.asDuration());
    return IDateTimeItem.valueOf(dateTime);
  }

  @NonNull
  public static IYearMonthDurationItem opMultiplyYearMonthDuration(
      @NonNull IYearMonthDurationItem arg1,
      @NonNull INumericItem arg2) {
    int arg2Int;
    try {
      arg2Int = FunctionUtils.asInteger(arg2.round());
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }

    @SuppressWarnings("null")
    @NonNull Period period = arg1.asPeriod().multipliedBy(arg2Int);
    return IYearMonthDurationItem.valueOf(period);
  }

  @NonNull
  public static IDayTimeDurationItem opMultiplyDayTimeDuration(
      @NonNull IDayTimeDurationItem arg1,
      @NonNull INumericItem arg2) {
    long arg2Long;
    try {
      arg2Long = FunctionUtils.asLong(arg2.round());
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }

    @SuppressWarnings("null")
    @NonNull Duration duration = arg1.asDuration().multipliedBy(arg2Long);
    return IDayTimeDurationItem.valueOf(duration);
  }

  @NonNull
  public static IYearMonthDurationItem opDivideYearMonthDuration(
      @NonNull IYearMonthDurationItem arg1,
      @NonNull INumericItem arg2) {
    IIntegerItem totalMonths = IIntegerItem.valueOf(arg1.asPeriod().toTotalMonths());
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
    return IYearMonthDurationItem.valueOf(years, months);
  }

  @NonNull
  public static IDayTimeDurationItem opDivideDayTimeDuration(
      @NonNull IDayTimeDurationItem arg1,
      @NonNull INumericItem arg2) {
    try {
      @SuppressWarnings("null")
      @NonNull Duration duration = arg1.asDuration().dividedBy(FunctionUtils.asLong(arg2.round()));
      return IDayTimeDurationItem
          .valueOf(duration);
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO, "Division by zero", ex);
    }
  }

  @NonNull
  public static IDecimalItem opDivideDayTimeDurationByDayTimeDuration(
      @NonNull IDayTimeDurationItem arg1,
      @NonNull IDayTimeDurationItem arg2) {
    return IDecimalItem.cast(
        opNumericDivide(
            IDecimalItem.valueOf(arg1.asSeconds()),
            IDecimalItem.valueOf(arg2.asSeconds())));
  }

  @NonNull
  public static IBooleanItem opDateEqual(@NonNull IDateItem arg1, @NonNull IDateItem arg2) {
    // TODO: avoid cast?
    return opDateTimeEqual(IDateTimeItem.cast(arg1), IDateTimeItem.cast(arg2));
  }

  @NonNull
  public static IBooleanItem opDateTimeEqual(@NonNull IDateTimeItem arg1, @NonNull IDateTimeItem arg2) {
    return IBooleanItem.valueOf(arg1.asZonedDateTime().equals(arg2.asZonedDateTime()));
  }

  @NonNull
  public static IBooleanItem opDurationEqual(@NonNull IDurationItem arg1, @NonNull IDurationItem arg2) {
    return IBooleanItem.valueOf(arg1.compareTo(arg2) == 0);
  }

  @NonNull
  public static IBooleanItem opBase64BinaryEqual(@NonNull IBase64BinaryItem arg1, @NonNull IBase64BinaryItem arg2) {
    return IBooleanItem.valueOf(arg1.asByteBuffer().equals(arg2.asByteBuffer()));
  }

  @NonNull
  public static IBooleanItem opDateGreaterThan(@NonNull IDateItem arg1, @NonNull IDateItem arg2) {
    // TODO: avoid cast?
    return opDateTimeGreaterThan(IDateTimeItem.cast(arg1), IDateTimeItem.cast(arg2));
  }

  @NonNull
  public static IBooleanItem opDateTimeGreaterThan(@NonNull IDateTimeItem arg1, @NonNull IDateTimeItem arg2) {
    return IBooleanItem.valueOf(arg1.asZonedDateTime().compareTo(arg2.asZonedDateTime()) > 0);
  }

  @NonNull
  public static IBooleanItem opYearMonthDurationGreaterThan(
      @NonNull IYearMonthDurationItem arg1,
      @NonNull IYearMonthDurationItem arg2) {
    Period p1 = arg1.asPeriod();
    Period p2 = arg2.asPeriod();

    // this is only an approximation
    return IBooleanItem.valueOf(p1.toTotalMonths() > p2.toTotalMonths());
  }

  @NonNull
  public static IBooleanItem opDayTimeDurationGreaterThan(
      @NonNull IDayTimeDurationItem arg1,
      @NonNull IDayTimeDurationItem arg2) {
    return IBooleanItem.valueOf(arg1.compareTo(arg2) > 0);
  }

  @NonNull
  public static IBooleanItem opBase64BinaryGreaterThan(
      @NonNull IBase64BinaryItem arg1,
      @NonNull IBase64BinaryItem arg2) {
    return IBooleanItem.valueOf(arg1.compareTo(arg2) > 0);
  }

  @NonNull
  public static IBooleanItem opDateLessThan(
      @NonNull IDateItem arg1,
      @NonNull IDateItem arg2) {
    return opDateTimeLessThan(IDateTimeItem.cast(arg1), IDateTimeItem.cast(arg2));
  }

  @NonNull
  public static IBooleanItem opDateTimeLessThan(
      @NonNull IDateTimeItem arg1,
      @NonNull IDateTimeItem arg2) {
    return IBooleanItem.valueOf(arg1.asZonedDateTime().compareTo(arg2.asZonedDateTime()) < 0);
  }

  @NonNull
  public static IBooleanItem opYearMonthDurationLessThan(@NonNull IYearMonthDurationItem arg1,
      @NonNull IYearMonthDurationItem arg2) {
    Period p1 = arg1.asPeriod();
    Period p2 = arg2.asPeriod();

    // this is only an approximation
    return IBooleanItem.valueOf(p1.toTotalMonths() < p2.toTotalMonths());
  }

  @NonNull
  public static IBooleanItem opDayTimeDurationLessThan(
      @NonNull IDayTimeDurationItem arg1,
      @NonNull IDayTimeDurationItem arg2) {
    return IBooleanItem.valueOf(arg1.compareTo(arg2) < 0);
  }

  @NonNull
  public static IBooleanItem opBase64BinaryLessThan(
      @NonNull IBase64BinaryItem arg1,
      @NonNull IBase64BinaryItem arg2) {
    return IBooleanItem.valueOf(arg1.compareTo(arg2) < 0);
  }

  @NonNull
  public static INumericItem opNumericAdd(@NonNull INumericItem left, @NonNull INumericItem right) {
    INumericItem retval;
    if (left instanceof IDecimalItem || right instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimalLeft = left.asDecimal();
      BigDecimal decimalRight = right.asDecimal();

      @SuppressWarnings("null")
      @NonNull BigDecimal result = decimalLeft.add(decimalRight, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger integerLeft = left.asInteger();
      BigInteger integerRight = right.asInteger();

      @SuppressWarnings("null")
      @NonNull BigInteger result = integerLeft.add(integerRight);
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  @NonNull
  public static INumericItem opNumericSubtract(@NonNull INumericItem left, @NonNull INumericItem right) {
    INumericItem retval;
    if (left instanceof IDecimalItem || right instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimalLeft = left.asDecimal();
      BigDecimal decimalRight = right.asDecimal();

      @SuppressWarnings("null")
      @NonNull BigDecimal result = decimalLeft.subtract(decimalRight, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger integerLeft = left.asInteger();
      BigInteger integerRight = right.asInteger();

      @SuppressWarnings("null")
      @NonNull BigInteger result = integerLeft.subtract(integerRight);
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  @NonNull
  public static INumericItem opNumericMultiply(@NonNull INumericItem left, @NonNull INumericItem right) {
    INumericItem retval;
    if (left instanceof IDecimalItem || right instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimalLeft = left.asDecimal();
      BigDecimal decimalRight = right.asDecimal();

      @SuppressWarnings("null")
      @NonNull BigDecimal result = decimalLeft.multiply(decimalRight, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      // create an integer result
      @SuppressWarnings("null")
      @NonNull BigInteger result = left.asInteger().multiply(right.asInteger());
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  @NonNull
  public static IDecimalItem opNumericDivide(@NonNull INumericItem dividend, @NonNull INumericItem divisor) {
    // create a decimal result
    BigDecimal decimalDivisor = divisor.asDecimal();

    if (BigDecimal.ZERO.equals(decimalDivisor)) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO,
          ArithmeticFunctionException.DIVISION_BY_ZERO_MESSAGE);
    }

    BigDecimal decimalDividend = dividend.asDecimal();

    @SuppressWarnings("null")
    @NonNull BigDecimal result = decimalDividend.divide(decimalDivisor, FunctionUtils.MATH_CONTEXT);
    return IDecimalItem.valueOf(result);
  }

  @NonNull
  public static IIntegerItem opNumericIntegerDivide(@NonNull INumericItem dividend, @NonNull INumericItem divisor) {
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
      @NonNull BigInteger result
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
      @NonNull BigInteger result = dividend.asInteger().divide(integerDivisor);
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-numeric-mod">func:numeric-mod</a>.
   *
   * @param dividend
   *          the number to be divided
   * @param divisor
   *          the number to divide by
   * @return the remainder
   */
  @NonNull
  public static INumericItem opNumericMod(@NonNull INumericItem dividend, @NonNull INumericItem divisor) {
    BigDecimal decimalDivisor = divisor.asDecimal();

    if (BigDecimal.ZERO.equals(decimalDivisor)) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO,
          ArithmeticFunctionException.DIVISION_BY_ZERO_MESSAGE);
    }

    BigDecimal decimalDividend = dividend.asDecimal();

    INumericItem retval;
    if (BigDecimal.ZERO.equals(decimalDividend)) {
      retval = dividend;
    } else {
      @SuppressWarnings("null")
      @NonNull BigDecimal result = decimalDividend.remainder(decimalDivisor, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    }
    return retval;
  }

  @NonNull
  public static INumericItem opNumericUnaryMinus(@NonNull INumericItem item) {
    INumericItem retval;
    if (item instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimal = item.asDecimal();

      @SuppressWarnings("null")
      @NonNull BigDecimal result = decimal.negate(FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else if (item instanceof IIntegerItem) {
      // create a decimal result
      BigInteger integer = item.asInteger();

      @SuppressWarnings("null")
      @NonNull BigInteger result = integer.negate();
      retval = IIntegerItem.valueOf(result);
    } else {
      throw new InvalidTypeMetapathException(item);
    }
    return retval;
  }

  @NonNull
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

  @NonNull
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

  @NonNull
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

  @NonNull
  public static IBooleanItem opBooleanEqual(@Nullable IBooleanItem arg1, @Nullable IBooleanItem arg2) {
    boolean left = arg1 != null && arg1.toBoolean();
    boolean right = arg2 != null && arg2.toBoolean();

    return IBooleanItem.valueOf(left == right);
  }

  @NonNull
  public static IBooleanItem opBooleanGreaterThan(@Nullable IBooleanItem arg1, @Nullable IBooleanItem arg2) {
    boolean left = arg1 != null && arg1.toBoolean();
    boolean right = arg2 != null && arg2.toBoolean();

    return IBooleanItem.valueOf(left && !right);
  }

  @NonNull
  public static IBooleanItem opBooleanLessThan(@Nullable IBooleanItem arg1, @Nullable IBooleanItem arg2) {
    boolean left = arg1 != null && arg1.toBoolean();
    boolean right = arg2 != null && arg2.toBoolean();

    return IBooleanItem.valueOf(!left && right);
  }
}
