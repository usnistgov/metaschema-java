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

package gov.nist.secauto.metaschema.model.common.metapath.item;

import gov.nist.secauto.metaschema.model.common.metapath.function.ArithmeticFunctionException;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.InvalidValueForCastFunctionMetapathException;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public interface INumericItem extends IAnyAtomicItem {

  @NotNull
  public static INumericItem cast(@NotNull IAnyAtomicItem item) throws InvalidValueForCastFunctionMetapathException {
    INumericItem retval;
    if (item instanceof INumericItem) {
      retval = (INumericItem) item;
    } else {
      try {
        retval = IDecimalItem.valueOf(item.asString());
      } catch (NumberFormatException ex) {
        throw new InvalidValueForCastFunctionMetapathException(ex);
      }
    }
    return retval;
  }

  @NotNull
  BigDecimal asDecimal();

  @NotNull
  BigInteger asInteger();

  boolean toEffectiveBoolean();

  /**
   * Get the absolute value of the item.
   * 
   * @return this item negated if this item is negative, or the item otherwise
   */
  @NotNull
  INumericItem abs();

  /**
   * Round the value to the whole number closest to positive infinity.
   * 
   * @return the rounded value
   */
  @NotNull
  IIntegerItem ceiling();

  /**
   * Round the value to the whole number closest to negative infinity.
   * 
   * @return the rounded value
   */
  @NotNull
  IIntegerItem floor();

  @NotNull
  default INumericItem round() {
    return round(IIntegerItem.ZERO);
  }

  @NotNull
  default INumericItem round(@NotNull IIntegerItem precisionItem) {
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
        BigDecimal value = this.asDecimal();
        if (value.signum() == -1) {
          retval = IDecimalItem.valueOf(
              ObjectUtils.notNull(
                  value.round(new MathContext(precision + value.precision() - value.scale(), RoundingMode.HALF_DOWN))));
        } else {
          retval = IDecimalItem.valueOf(
              ObjectUtils.notNull(
                  value.round(new MathContext(precision + value.precision() - value.scale(), RoundingMode.HALF_UP))));
        }
      }
    } else if (precision < 0) {
      // round to a power of 10
      BigInteger value = this.asInteger();
      BigInteger divisor = BigInteger.TEN.pow(0 - precision);

      @NotNull
      BigInteger result;
      if (divisor.compareTo(value.abs()) > 0) {
        result =  ObjectUtils.notNull(BigInteger.ZERO);
      } else {
        BigInteger remainder = value.mod(divisor);
        BigInteger lessRemainder = value.subtract(remainder);
        BigInteger halfDivisor = divisor.divide(BigInteger.TWO);
        result = ObjectUtils.notNull(
            remainder.compareTo(halfDivisor) >= 0 ? lessRemainder.add(divisor) : lessRemainder);
      }
      retval = IIntegerItem.valueOf(result);
    } else {
      // precision == 0
      if (this instanceof IIntegerItem) {
        retval = this;
      } else {
        BigDecimal value = this.asDecimal();
        retval = IDecimalItem.valueOf(
            ObjectUtils.notNull(value.round(new MathContext(1, RoundingMode.CEILING))));
      }
    }
    return retval;
  }
}
