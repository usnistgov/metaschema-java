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

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-add-yearMonthDuration-to-date">op:add-yearMonthDuration-to-date</a>.
   *
   * @param instant
   *          a point in time
   * @param duration
   *          the duration to add
   * @return the result of adding the duration to the date
   */
  @NonNull
  public static IDateItem opAddYearMonthDurationToDate(@NonNull IDateItem instant,
      @NonNull IYearMonthDurationItem duration) {
    return addDurationToDate(instant.asZonedDateTime(), duration.asPeriod());
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-add-dayTimeDuration-to-date">op:add-dayTimeDuration-to-date</a>.
   *
   * @param instant
   *          a point in time
   * @param duration
   *          the duration to add
   * @return the result of adding the duration to the date
   */
  @NonNull
  public static IDateItem opAddDayTimeDurationToDate(@NonNull IDateItem instant,
      @NonNull IDayTimeDurationItem duration) {
    return addDurationToDate(instant.asZonedDateTime(), duration.asDuration());
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

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-add-yearMonthDurations">op:add-yearMonthDurations</a>.
   *
   * @param arg1
   *          the first duration
   * @param arg2
   *          the second duration
   * @return the sum of two duration values
   */
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

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-add-dayTimeDurations">op:add-dayTimeDurations</a>.
   *
   * @param arg1
   *          the first duration
   * @param arg2
   *          the second duration
   * @return the sum of two duration values
   */
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

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-add-yearMonthDuration-to-dateTime">op:add-yearMonthDuration-to-dateTime</a>.
   *
   * @param instant
   *          a point in time
   * @param duration
   *          the duration to add
   * @return the result of adding the duration to the date
   */
  @NonNull
  public static IDateTimeItem opAddYearMonthDurationToDateTime(
      @NonNull IDateTimeItem instant,
      @NonNull IYearMonthDurationItem duration) {
    ZonedDateTime result;
    try {
      result = instant.asZonedDateTime().plus(duration.asPeriod());
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }
    assert result != null;
    return IDateTimeItem.valueOf(result);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-add-dayTimeDuration-to-dateTime">op:add-dayTimeDuration-to-dateTime</a>.
   *
   * @param instant
   *          a point in time
   * @param duration
   *          the duration to add
   * @return the result of adding the duration to the date
   */
  @NonNull
  public static IDateTimeItem opAddDayTimeDurationToDateTime(
      @NonNull IDateTimeItem instant,
      @NonNull IDayTimeDurationItem duration) {
    ZonedDateTime result;
    try {
      result = instant.asZonedDateTime().plus(duration.asDuration());
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR, ex);
    }
    assert result != null;
    return IDateTimeItem.valueOf(result);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-subtract-dates">op:subtract-dates</a>.
   *
   * @param date1
   *          the first point in time
   * @param date2
   *          the second point in time
   * @return the elapsed time between the starting instant and ending instant
   */
  @NonNull
  public static IDayTimeDurationItem opSubtractDates(@NonNull IDateItem date1, @NonNull IDateItem date2) {
    return between(date1.asZonedDateTime(), date2.asZonedDateTime());
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-subtract-yearMonthDuration-from-date">op:subtract-yearMonthDuration-from-date</a>.
   *
   * @param date
   *          a point in time
   * @param duration
   *          the duration to subtract
   * @return the result of subtracting the duration from the date
   */
  @NonNull
  public static IDateItem opSubtractYearMonthDurationFromDate(
      @NonNull IDateItem date,
      @NonNull IYearMonthDurationItem duration) {
    return subtractDurationFromDate(date.asZonedDateTime(), duration.asPeriod());
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-subtract-dayTimeDuration-from-date">op:subtract-dayTimeDuration-from-date</a>.
   *
   * @param date
   *          a point in time
   * @param duration
   *          the duration to subtract
   * @return the result of subtracting the duration from the date
   */
  @NonNull
  public static IDateItem opSubtractDayTimeDurationFromDate(
      @NonNull IDateItem date,
      @NonNull IDayTimeDurationItem duration) {
    return subtractDurationFromDate(date.asZonedDateTime(), duration.asDuration());
  }

  @NonNull
  private static IDateItem subtractDurationFromDate(
      @NonNull ZonedDateTime dateTime,
      @NonNull TemporalAmount duration) {
    @SuppressWarnings("null")
    @NonNull ZonedDateTime result = dateTime.minus(duration);
    return IDateItem.valueOf(result);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-subtract-yearMonthDurations">op:subtract-yearMonthDurations</a>.
   *
   * @param arg1
   *          the first duration
   * @param arg2
   *          the second duration
   * @return the result of subtracting the second duration from the first
   */
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

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-subtract-dayTimeDurations">op:subtract-dayTimeDurations</a>.
   *
   * @param arg1
   *          the first duration
   * @param arg2
   *          the second duration
   * @return the result of subtracting the second duration from the first
   */
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

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-subtract-dateTimes">op:subtract-dateTimes</a>.
   *
   * @param time1
   *          the first point in time
   * @param time2
   *          the second point in time
   * @return the duration the occurred between the two points in time
   */
  @NonNull
  public static IDayTimeDurationItem opSubtractDateTimes(@NonNull IDateTimeItem time1, @NonNull IDateTimeItem time2) {
    return between(time1.asZonedDateTime(), time2.asZonedDateTime());
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-subtract-dateTimes">op:subtract-dateTimes</a>.
   *
   * @param time1
   *          the first point in time
   * @param time2
   *          the second point in time
   * @return the duration the occurred between the two points in time
   */
  @NonNull
  private static IDayTimeDurationItem between(@NonNull ZonedDateTime time1, @NonNull ZonedDateTime time2) {
    @SuppressWarnings("null")
    @NonNull Duration between = Duration.between(time1, time2);
    return IDayTimeDurationItem.valueOf(between);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-subtract-yearMonthDuration-from-dateTime">op:subtract-yearMonthDuration-from-dateTime</a>.
   *
   * @param moment
   *          a point in time
   * @param duration
   *          the duration to subtract
   * @return the result of subtracting the duration from a point in time
   */
  @NonNull
  public static IDateTimeItem opSubtractYearMonthDurationFromDateTime(
      @NonNull IDateTimeItem moment,
      @NonNull IYearMonthDurationItem duration) {
    @SuppressWarnings("null")
    @NonNull ZonedDateTime dateTime = moment.asZonedDateTime().minus(duration.asPeriod());
    return IDateTimeItem.valueOf(dateTime);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-subtract-dayTimeDuration-from-dateTime">op:subtract-dayTimeDuration-from-dateTime</a>.
   *
   * @param moment
   *          a point in time
   * @param duration
   *          the duration to subtract
   * @return the result of subtracting the duration from a point in time
   */
  @NonNull
  public static IDateTimeItem opSubtractDayTimeDurationFromDateTime(
      @NonNull IDateTimeItem moment,
      @NonNull IDayTimeDurationItem duration) {

    @SuppressWarnings("null")
    @NonNull ZonedDateTime dateTime = moment.asZonedDateTime().plus(duration.asDuration());
    return IDateTimeItem.valueOf(dateTime);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-multiply-yearMonthDuration">op:multiply-yearMonthDuration</a>.
   *
   * @param arg1
   *          the duration value
   * @param arg2
   *          the number to multiply by
   * @return the result of multiplying a {@link IYearMonthDurationItem} by a
   *         number
   */
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

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-multiply-dayTimeDuration">op:multiply-dayTimeDuration</a>.
   *
   * @param arg1
   *          the duration value
   * @param arg2
   *          the number to multiply by
   * @return the result of multiplying a {@link IDayTimeDurationItem} by a number
   */
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

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-divide-yearMonthDuration">op:divide-yearMonthDuration</a>.
   *
   * @param arg1
   *          the duration value
   * @param arg2
   *          the number to divide by
   * @return the result of dividing a {@link IYearMonthDurationItem} by a number
   */
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

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-divide-dayTimeDuration">op:divide-dayTimeDuration</a>.
   *
   * @param arg1
   *          the duration value
   * @param arg2
   *          the number to divide by
   * @return the result of dividing a {@link IDayTimeDurationItem} by a number
   */
  @NonNull
  public static IDayTimeDurationItem opDivideDayTimeDuration(
      @NonNull IDayTimeDurationItem arg1,
      @NonNull INumericItem arg2) {
    try {
      @SuppressWarnings("null")
      @NonNull Duration duration = arg1.asDuration().dividedBy(FunctionUtils.asLong(arg2.round()));
      return IDayTimeDurationItem.valueOf(duration);
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO, "Division by zero", ex);
    }
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-divide-dayTimeDuration-by-dayTimeDuration">op:divide-dayTimeDuration-by-dayTimeDuration</a>.
   *
   * @param arg1
   *          the first duration value
   * @param arg2
   *          the second duration value
   * @return the ratio of two {@link IDayTimeDurationItem} values, as a decimal
   *         number
   */
  @NonNull
  public static IDecimalItem opDivideDayTimeDurationByDayTimeDuration(
      @NonNull IDayTimeDurationItem arg1,
      @NonNull IDayTimeDurationItem arg2) {
    return IDecimalItem.cast(
        opNumericDivide(
            IDecimalItem.valueOf(arg1.asSeconds()),
            IDecimalItem.valueOf(arg2.asSeconds())));
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-date-equal">op:date-equal</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is the same instant in time as the
   *         second, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opDateEqual(@NonNull IDateItem arg1, @NonNull IDateItem arg2) {
    // TODO: avoid cast?
    return opDateTimeEqual(IDateTimeItem.cast(arg1), IDateTimeItem.cast(arg2));
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-dateTime-equal">op:dateTime-equal</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is the same instant in time as the
   *         second, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opDateTimeEqual(@NonNull IDateTimeItem arg1, @NonNull IDateTimeItem arg2) {
    return IBooleanItem.valueOf(arg1.asZonedDateTime().equals(arg2.asZonedDateTime()));
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-duration-equal">op:duration-equal</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is the same duration as the
   *         second, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opDurationEqual(@NonNull IDurationItem arg1, @NonNull IDurationItem arg2) {
    return IBooleanItem.valueOf(arg1.compareTo(arg2) == 0);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-base64Binary-equal">op:base64Binary-equal</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is equal to the second, or
   *         {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opBase64BinaryEqual(@NonNull IBase64BinaryItem arg1, @NonNull IBase64BinaryItem arg2) {
    return IBooleanItem.valueOf(arg1.asByteBuffer().equals(arg2.asByteBuffer()));
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-date-greater-than">op:date-greater-than</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is a later instant in time than
   *         the second, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opDateGreaterThan(@NonNull IDateItem arg1, @NonNull IDateItem arg2) {
    // TODO: avoid cast?
    return opDateTimeGreaterThan(IDateTimeItem.cast(arg1), IDateTimeItem.cast(arg2));
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-dateTime-greater-than">op:dateTime-greater-than</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is a later instant in time than
   *         the second, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opDateTimeGreaterThan(@NonNull IDateTimeItem arg1, @NonNull IDateTimeItem arg2) {
    return IBooleanItem.valueOf(arg1.asZonedDateTime().compareTo(arg2.asZonedDateTime()) > 0);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-yearMonthDuration-greater-than">op:yearMonthDuration-greater-than</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is a longer duration than the
   *         second, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opYearMonthDurationGreaterThan(
      @NonNull IYearMonthDurationItem arg1,
      @NonNull IYearMonthDurationItem arg2) {
    Period p1 = arg1.asPeriod();
    Period p2 = arg2.asPeriod();

    // this is only an approximation
    return IBooleanItem.valueOf(p1.toTotalMonths() > p2.toTotalMonths());
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-dayTimeDuration-greater-than">op:dayTimeDuration-greater-than</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is a longer duration than the
   *         second, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opDayTimeDurationGreaterThan(
      @NonNull IDayTimeDurationItem arg1,
      @NonNull IDayTimeDurationItem arg2) {
    return IBooleanItem.valueOf(arg1.compareTo(arg2) > 0);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-base64Binary-greater-than">op:base64Binary-greater-than</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is greater than the second, or
   *         {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opBase64BinaryGreaterThan(
      @NonNull IBase64BinaryItem arg1,
      @NonNull IBase64BinaryItem arg2) {
    return IBooleanItem.valueOf(arg1.compareTo(arg2) > 0);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-date-less-than">op:date-less-than</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is an earlier instant in time than
   *         the second, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opDateLessThan(
      @NonNull IDateItem arg1,
      @NonNull IDateItem arg2) {
    return opDateTimeLessThan(IDateTimeItem.cast(arg1), IDateTimeItem.cast(arg2));
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-dateTime-less-than">op:dateTime-less-than</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is an earlier instant in time than
   *         the second, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opDateTimeLessThan(
      @NonNull IDateTimeItem arg1,
      @NonNull IDateTimeItem arg2) {
    return IBooleanItem.valueOf(arg1.asZonedDateTime().compareTo(arg2.asZonedDateTime()) < 0);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-yearMonthDuration-less-than">op:yearMonthDuration-less-than</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is a shorter duration than the
   *         second, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opYearMonthDurationLessThan(@NonNull IYearMonthDurationItem arg1,
      @NonNull IYearMonthDurationItem arg2) {
    Period p1 = arg1.asPeriod();
    Period p2 = arg2.asPeriod();

    // this is only an approximation
    return IBooleanItem.valueOf(p1.toTotalMonths() < p2.toTotalMonths());
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-dayTimeDuration-less-than">op:dayTimeDuration-less-than</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is a shorter duration than the
   *         second, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opDayTimeDurationLessThan(
      @NonNull IDayTimeDurationItem arg1,
      @NonNull IDayTimeDurationItem arg2) {
    return IBooleanItem.valueOf(arg1.compareTo(arg2) < 0);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-base64Binary-less-than">op:base64Binary-less-than</a>.
   *
   * @param arg1
   *          the first value
   * @param arg2
   *          the second value
   * @return {@code true} if the first argument is less than the second, or
   *         {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opBase64BinaryLessThan(
      @NonNull IBase64BinaryItem arg1,
      @NonNull IBase64BinaryItem arg2) {
    return IBooleanItem.valueOf(arg1.compareTo(arg2) < 0);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-numeric-add">op:numeric-add</a>.
   *
   * @param left
   *          the first number
   * @param right
   *          the second number
   * @return the result of adding the second number to the first number
   */
  @NonNull
  public static INumericItem opNumericAdd(@NonNull INumericItem left, @NonNull INumericItem right) {
    INumericItem retval;
    if (left instanceof IIntegerItem || right instanceof IIntegerItem) {
      // create an integer result
      BigInteger integerLeft = left.asInteger();
      BigInteger integerRight = right.asInteger();

      @SuppressWarnings("null")
      @NonNull BigInteger result = integerLeft.add(integerRight);
      retval = IIntegerItem.valueOf(result);
    } else {
      // create a decimal result
      BigDecimal decimalLeft = left.asDecimal();
      BigDecimal decimalRight = right.asDecimal();

      @SuppressWarnings("null")
      @NonNull BigDecimal result = decimalLeft.add(decimalRight, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    }
    return retval;
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-numeric-subtract">op:numeric-subtract</a>.
   *
   * @param left
   *          the first number
   * @param right
   *          the second number
   * @return the result of subtracting the second number from the first number
   */
  @NonNull
  public static INumericItem opNumericSubtract(@NonNull INumericItem left, @NonNull INumericItem right) {
    INumericItem retval;
    if (left instanceof IIntegerItem || right instanceof IIntegerItem) {
      // create an integer result
      BigInteger integerLeft = left.asInteger();
      BigInteger integerRight = right.asInteger();

      @SuppressWarnings("null")
      @NonNull BigInteger result = integerLeft.subtract(integerRight);
      retval = IIntegerItem.valueOf(result);
    } else {
      // create a decimal result
      BigDecimal decimalLeft = left.asDecimal();
      BigDecimal decimalRight = right.asDecimal();

      @SuppressWarnings("null")
      @NonNull BigDecimal result = decimalLeft.subtract(decimalRight, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    }
    return retval;
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-numeric-multiply">op:numeric-multiply</a>.
   *
   * @param left
   *          the first number
   * @param right
   *          the second number
   * @return the result of multiplying the first number by the second number
   */
  @NonNull
  public static INumericItem opNumericMultiply(@NonNull INumericItem left, @NonNull INumericItem right) {
    INumericItem retval;
    if (left instanceof IIntegerItem || right instanceof IIntegerItem) {
      // create an integer result
      @SuppressWarnings("null")
      @NonNull BigInteger result = left.asInteger().multiply(right.asInteger());
      retval = IIntegerItem.valueOf(result);
    } else {
      // create a decimal result
      BigDecimal decimalLeft = left.asDecimal();
      BigDecimal decimalRight = right.asDecimal();

      @SuppressWarnings("null")
      @NonNull BigDecimal result = decimalLeft.multiply(decimalRight, FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    }
    return retval;
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-numeric-divide">op:numeric-divide</a>.
   *
   * @param dividend
   *          the number to be divided
   * @param divisor
   *          the number to divide by
   * @return the quotient
   */
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

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-numeric-integer-divide">op:numeric-integer-divide</a>.
   *
   * @param dividend
   *          the number to be divided
   * @param divisor
   *          the number to divide by
   * @return the quotient
   */
  @NonNull
  public static IIntegerItem opNumericIntegerDivide(@NonNull INumericItem dividend, @NonNull INumericItem divisor) {
    IIntegerItem retval;
    if (dividend instanceof IIntegerItem || divisor instanceof IIntegerItem) {
      // create an integer result
      BigInteger integerDivisor = divisor.asInteger();

      if (BigInteger.ZERO.equals(integerDivisor)) {
        throw new ArithmeticFunctionException(ArithmeticFunctionException.DIVISION_BY_ZERO,
            ArithmeticFunctionException.DIVISION_BY_ZERO_MESSAGE);
      }

      @SuppressWarnings("null")
      @NonNull BigInteger result = dividend.asInteger().divide(integerDivisor);
      retval = IIntegerItem.valueOf(result);
    } else {
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
    }
    return retval;
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-numeric-mod">op:numeric-mod</a>.
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

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-numeric-unary-minus">op:numeric-unary-minus</a>.
   *
   * @param item
   *          the number whose sign is to be reversed
   * @return the number with a reversed sign
   */
  @NonNull
  public static INumericItem opNumericUnaryMinus(@NonNull INumericItem item) {
    INumericItem retval;
    if (item instanceof IIntegerItem) {
      // create a decimal result
      BigInteger integer = item.asInteger();

      @SuppressWarnings("null")
      @NonNull BigInteger result = integer.negate();
      retval = IIntegerItem.valueOf(result);
    } else if (item instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimal = item.asDecimal();

      @SuppressWarnings("null")
      @NonNull BigDecimal result = decimal.negate(FunctionUtils.MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      throw new InvalidTypeMetapathException(item);
    }
    return retval;
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-numeric-equal">op:numeric-equal</a>.
   *
   * @param arg1
   *          the first number to check for equality
   * @param arg2
   *          the second number to check for equality
   * @return {@code true} if the numbers are numerically equal or {@code false}
   *         otherwise
   */
  @NonNull
  public static IBooleanItem opNumericEqual(@Nullable INumericItem arg1, @Nullable INumericItem arg2) {
    IBooleanItem retval;
    if (arg1 == null || arg2 == null) {
      retval = IBooleanItem.FALSE;
    } else if (arg1 instanceof IIntegerItem || arg2 instanceof IIntegerItem) {
      retval = IBooleanItem.valueOf(arg1.asInteger().equals(arg2.asInteger()));
    } else {
      retval = IBooleanItem.valueOf(arg1.asDecimal().equals(arg2.asDecimal()));
    }
    return retval;
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-numeric-greater-than">op:numeric-greater-than</a>.
   *
   * @param arg1
   *          the first number to check
   * @param arg2
   *          the second number to check
   * @return {@code true} if the first number is greater than or equal to the
   *         second, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opNumericGreaterThan(@Nullable INumericItem arg1, @Nullable INumericItem arg2) {
    IBooleanItem retval;
    if (arg1 == null || arg2 == null) {
      retval = IBooleanItem.FALSE;
    } else if (arg1 instanceof IIntegerItem || arg2 instanceof IIntegerItem) {
      int result = arg1.asInteger().compareTo(arg2.asInteger());
      retval = IBooleanItem.valueOf(result > 0);
    } else {
      int result = arg1.asDecimal().compareTo(arg2.asDecimal());
      retval = IBooleanItem.valueOf(result > 0);
    }
    return retval;
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-numeric-less-than">op:numeric-less-than</a>.
   *
   * @param arg1
   *          the first number to check
   * @param arg2
   *          the second number to check
   * @return {@code true} if the first number is less than or equal to the second,
   *         or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opNumericLessThan(@Nullable INumericItem arg1, @Nullable INumericItem arg2) {
    IBooleanItem retval;
    if (arg1 == null || arg2 == null) {
      retval = IBooleanItem.FALSE;
    } else if (arg1 instanceof IIntegerItem || arg2 instanceof IIntegerItem) {
      int result = arg1.asInteger().compareTo(arg2.asInteger());
      retval = IBooleanItem.valueOf(result < 0);
    } else {
      int result = arg1.asDecimal().compareTo(arg2.asDecimal());
      retval = IBooleanItem.valueOf(result < 0);
    }
    return retval;
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-boolean-equal">op:boolean-equal</a>.
   *
   * @param arg1
   *          the first boolean to check
   * @param arg2
   *          the second boolean to check
   * @return {@code true} if the first boolean is equal to the second, or
   *         {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opBooleanEqual(@Nullable IBooleanItem arg1, @Nullable IBooleanItem arg2) {
    boolean left = arg1 != null && arg1.toBoolean();
    boolean right = arg2 != null && arg2.toBoolean();

    return IBooleanItem.valueOf(left == right);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-boolean-greater-than">op:boolean-greater-than</a>.
   *
   * @param arg1
   *          the first boolean to check
   * @param arg2
   *          the second boolean to check
   * @return {@code true} if the first argument is {@link IBooleanItem#TRUE} and
   *         the second is {@link IBooleanItem#FALSE}, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opBooleanGreaterThan(@Nullable IBooleanItem arg1, @Nullable IBooleanItem arg2) {
    boolean left = arg1 != null && arg1.toBoolean();
    boolean right = arg2 != null && arg2.toBoolean();

    return IBooleanItem.valueOf(left && !right);
  }

  /**
   * Based on XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-boolean-less-than">op:boolean-less-than</a>.
   *
   * @param arg1
   *          the first boolean to check
   * @param arg2
   *          the second boolean to check
   * @return {@code true} if the first argument is {@link IBooleanItem#FALSE} and
   *         the second is {@link IBooleanItem#TRUE}, or {@code false} otherwise
   */
  @NonNull
  public static IBooleanItem opBooleanLessThan(@Nullable IBooleanItem arg1, @Nullable IBooleanItem arg2) {
    boolean left = arg1 != null && arg1.toBoolean();
    boolean right = arg2 != null && arg2.toBoolean();

    return IBooleanItem.valueOf(!left && right);
  }
}
