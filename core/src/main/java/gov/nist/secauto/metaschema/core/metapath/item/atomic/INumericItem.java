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
import gov.nist.secauto.metaschema.core.metapath.function.ArithmeticFunctionException;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidValueForCastFunctionException;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface INumericItem extends IAnyAtomicItem {

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
  static INumericItem cast(@NonNull IAnyAtomicItem item) {
    return MetaschemaDataTypeProvider.DECIMAL.cast(item);
  }

  /**
   * Get this item's value as a decimal.
   *
   * @return the equivalent decimal value
   */
  @NonNull
  BigDecimal asDecimal();

  /**
   * Get this item's value as an integer.
   *
   * @return the equivalent integer value
   */
  @NonNull
  BigInteger asInteger();

  /**
   * Get the effective boolean value of this item based on
   * <a href="https://www.w3.org/TR/xpath-31/#id-ebv">XPath 3.1</a>.
   *
   * @return the effective boolean value
   */
  boolean toEffectiveBoolean();

  @Override
  INumericItem castAsType(IAnyAtomicItem item);

  /**
   * Get the absolute value of the item.
   *
   * @return this item negated if this item is negative, or the item otherwise
   */
  @NonNull
  INumericItem abs();

  /**
   * Round the value to the whole number closest to positive infinity.
   *
   * @return the rounded value
   */
  @NonNull
  IIntegerItem ceiling();

  /**
   * Round the value to the whole number closest to negative infinity.
   *
   * @return the rounded value
   */
  @NonNull
  IIntegerItem floor();

  /**
   * Round the item's value with zero precision.
   * <p>
   * This is the same as calling {@link #round(IIntegerItem)} with a precision of
   * {@code 0}.
   *
   * @return the rounded value
   */
  @NonNull
  default INumericItem round() {
    return round(IIntegerItem.ZERO);
  }

  /**
   * Round the item's value with the specified precision.
   * <p>
   * This is the same as calling {@link #round(IIntegerItem)} with a precision of
   * {@code 0}.
   *
   * @param precisionItem
   *          the precision indicating the number of digits to round to before
   *          (negative value} or after (positive value) the decimal point.
   * @return the rounded value
   */
  @NonNull
  default INumericItem round(@NonNull IIntegerItem precisionItem) {
    int precision;
    try {
      precision = FunctionUtils.asInteger(precisionItem);
    } catch (ArithmeticException ex) {
      throw new ArithmeticFunctionException(ArithmeticFunctionException.OVERFLOW_UNDERFLOW_ERROR,
          "Numeric operation overflow/underflow.", ex);
    }
    INumericItem retval;
    if (precision >= 0) {
      // round to precision decimal places
      if (this instanceof IIntegerItem) {
        retval = this;
      } else {
        // IDecimalItem
        BigDecimal value = asDecimal();
        if (value.signum() == -1) {
          retval = IDecimalItem.valueOf(
              ObjectUtils.notNull(
                  value.round(new MathContext(precision + value.precision() - value.scale(), RoundingMode.HALF_DOWN))));
        } else {
          retval = IDecimalItem.valueOf(
              ObjectUtils.notNull(
                  value.round(new MathContext(precision + value.precision() - value.scale(), RoundingMode.HALF_UP))));
        }

        // cast result to original type
        retval = castAsType(retval);
      }
    } else {
      // round to a power of 10
      BigInteger value = asInteger();
      BigInteger divisor = BigInteger.TEN.pow(0 - precision);

      @NonNull BigInteger result;
      if (divisor.compareTo(value.abs()) > 0) {
        result = ObjectUtils.notNull(BigInteger.ZERO);
      } else {
        BigInteger remainder = value.mod(divisor);
        BigInteger lessRemainder = value.subtract(remainder);
        BigInteger halfDivisor = divisor.divide(BigInteger.TWO);
        result = ObjectUtils.notNull(
            remainder.compareTo(halfDivisor) >= 0 ? lessRemainder.add(divisor) : lessRemainder);
      }
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }
}
