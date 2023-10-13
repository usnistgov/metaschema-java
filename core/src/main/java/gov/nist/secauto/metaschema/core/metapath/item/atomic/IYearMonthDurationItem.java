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

package gov.nist.secauto.metaschema.core.metapath.item.atomic;

import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidValueForCastFunctionException;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.time.Period;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IYearMonthDurationItem extends IDurationItem {
  /**
   * Construct a new year month day duration item using the provided string
   * {@code value}.
   *
   * @param value
   *          a string representing a year month day duration
   * @return the new item
   */
  @NonNull
  static IYearMonthDurationItem valueOf(@NonNull String value) {
    try {
      Period period = ObjectUtils.notNull(MetaschemaDataTypeProvider.YEAR_MONTH_DURATION.parse(value).withDays(0));
      return valueOf(period);
    } catch (IllegalArgumentException ex) {
      throw new InvalidValueForCastFunctionException(String.format("Unable to parse string value '%s'", value),
          ex);
    }
  }

  /**
   * Construct a new year month day duration item using the provided
   * {@code value}.
   *
   * @param value
   *          a duration
   * @return the new item
   */
  @NonNull
  static IYearMonthDurationItem valueOf(@NonNull Period value) {
    return new YearMonthDurationItemImpl(ObjectUtils.notNull(value.withDays(0)));
  }

  /**
   * Construct a new year month day duration item using the provided values.
   *
   * @param years
   *          the number of years in the period
   * @param months
   *          the number of months in the period
   * @return the new item
   */
  @SuppressWarnings("null")
  @NonNull
  static IYearMonthDurationItem valueOf(int years, int months) {
    return valueOf(Period.of(years, months, 0));
  }

  /**
   * Cast the provided type to this item type.
   *
   * @param item
   *          the item to cast
   * @return the original item if it is already this type, otherwise a new item
   *         cast to this type
   * @throws InvalidValueForCastFunctionException
   *           if the provided {@code item} cannot be cast to this type
   */
  @NonNull
  static IYearMonthDurationItem cast(@NonNull IAnyAtomicItem item) {
    return MetaschemaDataTypeProvider.YEAR_MONTH_DURATION.cast(item);
  }

  /**
   * Get the "wrapped" duration value.
   *
   * @return the underlying duration value
   */
  @NonNull
  Period asPeriod();

  @Override
  default IYearMonthDurationItem castAsType(IAnyAtomicItem item) {
    return cast(item);
  }

  @Override
  default int compareTo(IAnyAtomicItem item) {
    return compareTo(cast(item));
  }

  /**
   * Compares this value with the argument.
   *
   * @param item
   *          the item to compare with this value
   * @return a negative integer, zero, or a positive integer if this value is less
   *         than, equal to, or greater than the {@code item}.
   */
  default int compareTo(IYearMonthDurationItem item) {
    Period thisPeriod = asPeriod().normalized();
    Period thatPeriod = item.asPeriod().normalized();

    int result = Integer.compare(thisPeriod.getYears(), thatPeriod.getYears());
    return result == 0 ? Integer.compare(thisPeriod.getMonths(), thatPeriod.getMonths()) : result;
  }
}
