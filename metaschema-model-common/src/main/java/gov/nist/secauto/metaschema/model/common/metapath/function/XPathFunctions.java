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

import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyUriItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAtomicValuedItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IUntypedAtomicItem;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

public class XPathFunctions {
  private XPathFunctions() {
    // disable
  }

  /**
   * An implementation of XPath 3.1
   * <a href="https://www.w3.org/TR/xpath-functions-31/#func-data">func:data</a> supporting
   * <a href="https://www.w3.org/TR/xpath-31/#id-atomization">item atomization</a>.
   * 
   * @param sequence
   *          the sequence of items to atomize
   * @return the atomized result
   */
  @SuppressWarnings("null")
  @NotNull
  public static ISequence<IAnyAtomicItem> fnData(@NotNull ISequence<?> sequence) {
    @NotNull
    Stream<? extends IItem> stream = sequence.asStream();
    return ISequence.of(stream.flatMap(x -> {
      return Stream.of((IAnyAtomicItem) fnDataItem(x));
    }));
  }

  @NotNull
  public static IAnyAtomicItem fnDataItem(@NotNull IItem item) {
    IAnyAtomicItem retval;
    if (item instanceof IAnyAtomicItem) {
      retval = (IAnyAtomicItem) item;
    } else if (item instanceof IAtomicValuedItem) {
      retval = ((IAtomicValuedItem) item).toAtomicItem();
    } else {
      throw new InvalidTypeFunctionMetapathException(InvalidTypeFunctionMetapathException.NODE_HAS_NO_TYPED_VALUE,
          String.format("Item '%s' has no typed value", item.getClass().getName()));
    }
    return retval;
  }

  /**
   * Get the effective boolean value of the provided sequence.
   * <p>
   * Based on the XPath 3.1
   * <a href="https://www.w3.org/TR/xpath-functions-31/#func-boolean">fn:boolean</a> function.
   * 
   * @param sequence
   *          the sequence to evaluate
   * @return the effective boolean value of the sequence
   */
  public static IBooleanItem fnBoolean(ISequence<?> sequence) {
    IBooleanItem retval;
    if (sequence == null) {
      retval = IBooleanItem.FALSE;
    } else {
      retval = IBooleanItem.valueOf(fnBooleanAsPrimative(sequence));
    }
    return retval;
  }

  public static boolean fnBooleanAsPrimative(ISequence<?> sequence) {
    boolean retval = false;
    if (!sequence.isEmpty()) {
      List<? extends IItem> items = sequence.asList();
      IItem first = items.iterator().next();
      if (first instanceof INodeItem) {
        retval = true;
      } else if (items.size() == 1) {
        retval = fnBooleanAsPrimative(first);
      }
    }
    return retval;
  }

  public static boolean fnBooleanAsPrimative(IItem item) {
    boolean retval = false;
    if (item instanceof IBooleanItem) {
      retval = ((IBooleanItem) item).toBoolean();
    } else if (item instanceof IStringItem) {
      String string = ((IStringItem) item).asString();
      retval = !string.isBlank();
    } else if (item instanceof INumericItem) {
      retval = ((INumericItem) item).toEffectiveBoolean();
    } else if (item instanceof IUntypedAtomicItem) {
      String string = ((IUntypedAtomicItem) item).asString();
      retval = !string.isBlank();
    } else if (item instanceof IAnyUriItem) {
      String string = ((IAnyUriItem) item).asString();
      retval = !string.isBlank();
    } else {
      throw new InvalidArgumentFunctionMetapathException(InvalidArgumentFunctionMetapathException.INVALID_ARGUMENT_TYPE,
          String.format("Invalid argument type '%s'", item.getItemName()));
    }
    return retval;
  }

  public static IIntegerItem fnCompare(IStringItem arg1, IStringItem arg2) {
    if (arg1 == null || arg2 == null) {
      return null;
    }

    String leftString = arg1.asString();
    String rightString = arg2.asString();
    return IIntegerItem.valueOf(leftString.compareTo(rightString));
  }

  public static IBooleanItem fnExists(ISequence<?> items) {
    return IBooleanItem.valueOf(!items.isEmpty());
  }

  @NotNull
  public static INumericItem fnRound(@NotNull INumericItem arg) {
    return fnRound(arg, IIntegerItem.ZERO);
  }

  @NotNull
  public static INumericItem fnRound(@NotNull INumericItem arg, @NotNull IIntegerItem precisionItem)
      throws ArithmeticFunctionException {
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
      if (arg instanceof IIntegerItem) {
        retval = arg;
      } else {
        // IDecimalItem
        BigDecimal value = arg.asDecimal();
        if (value.signum() == -1) {
          retval = IDecimalItem.valueOf(
              value.round(new MathContext(precision + value.precision() - value.scale(), RoundingMode.HALF_DOWN)));
        } else {
          retval = IDecimalItem.valueOf(
              value.round(new MathContext(precision + value.precision() - value.scale(), RoundingMode.HALF_UP)));
        }
      }
    } else if (precision < 0) {
      // round to a power of 10
      BigInteger value = arg.asInteger();
      BigInteger divisor = BigInteger.TEN.pow(0 - precision);

      BigInteger result;
      if (divisor.compareTo(value.abs()) > 0) {
        result = BigInteger.ZERO;
      } else {
        BigInteger remainder = value.mod(divisor);
        BigInteger lessRemainder = value.subtract(remainder);
        BigInteger halfDivisor = divisor.divide(BigInteger.TWO);
        result = remainder.compareTo(halfDivisor) >= 0 ? lessRemainder.add(divisor) : lessRemainder;
      }
      retval = IIntegerItem.valueOf(result);
    } else {
      // precision == 0
      if (arg instanceof IIntegerItem) {
        retval = arg;
      } else {
        BigDecimal value = arg.asDecimal();
        retval = IDecimalItem.valueOf(value.round(new MathContext(1, RoundingMode.CEILING)));
      }
    }
    return retval;
  }

}
