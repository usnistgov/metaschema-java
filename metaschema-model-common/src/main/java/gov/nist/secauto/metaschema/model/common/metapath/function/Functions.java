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

import gov.nist.secauto.metaschema.model.common.metapath.item.IMetapathResult;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.item.InvalidTypeException;
import gov.nist.secauto.metaschema.model.common.metapath.item.MetapathDynamicException;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IAnyUriItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IStringItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IUntypedAtomicItem;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Stream;

public class Functions {
  public static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private Functions() {
    // disable
  }

  /**
   * An implementation of {@link "https://www.w3.org/TR/xpath-functions-31/#func-data"} supporting
   * <a href="https://www.w3.org/TR/xpath-31/#id-atomization">item atomization</a>.
   * 
   * @param sequence
   *          the sequence of items to atomize
   * @return the atomized result
   */
  public static ISequence fnData(ISequence sequence) {
    Stream<? extends IItem> stream = sequence.asStream();
    return ISequence.of(stream.flatMap(x -> {
      return Stream.of((IItem)fnDataItem(x));
    }));
  }

  public static IAnyAtomicItem fnDataItem(IItem item) {
    IAnyAtomicItem retval;
    if (item instanceof IAnyAtomicItem) {
      retval = (IAnyAtomicItem) item;
    } else if (item instanceof INodeItem) {
      retval = item.toAtomicItem();
    } else {
      throw new MetapathDynamicException("err:FOTY0012", String.format("Unrecognized item '%s' during atomization", item.getClass().getName()));
    }
    return retval;
  }

  /**
   * Based on {@link "https://www.w3.org/TR/xpath-functions-31/#func-boolean"}.
   */
  public static IBooleanItem fnBoolean(IMetapathResult result) {
    return IBooleanItem.valueOf(fnBooleanInternal(result));
  }

  private static boolean fnBooleanInternal(IMetapathResult result) {
    boolean retval;
    if (result == null) {
      retval = false;
    } else if (result instanceof ISequence) {
      retval = fnBoolean((ISequence) result);
    } else {
      retval = gov.nist.secauto.metaschema.model.common.metapath.function.Functions.fnBoolean((IItem) result);
    }
    return retval;
  }

  private static boolean fnBoolean(ISequence sequence) {
    boolean retval = false;
    if (!sequence.isEmpty()) {
      List<? extends IItem> items = sequence.asList();
      IItem first = items.iterator().next();
      if (first instanceof INodeItem) {
        retval = true;
      } else if (items.size() == 1) {
        retval = fnBoolean(first);
      }
    }
    return retval;
  }

  public static boolean fnBoolean(IItem item) {
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
      throw new InvalidTypeException(item.getClass());
    }
    return retval;
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
        throw new InvalidTypeException(left.getClass());
      }

      BigDecimal decimalRight;
      if (left instanceof IIntegerItem) {
        decimalRight = ((IIntegerItem) right).asDecimal();
      } else if (left instanceof IDecimalItem) {
        decimalRight = ((IDecimalItem) right).asDecimal();
      } else {
        throw new InvalidTypeException(right.getClass());
      }
      BigDecimal result = decimalLeft.add(decimalRight, MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger integerLeft;
      if (left instanceof IIntegerItem) {
        integerLeft = ((IIntegerItem) left).asInteger();
      } else if (left instanceof IDecimalItem) {
        integerLeft = ((IDecimalItem) left).asInteger();
      } else {
        throw new InvalidTypeException(left.getClass());
      }

      BigInteger integerRight;
      if (left instanceof IIntegerItem) {
        integerRight = ((IIntegerItem) right).asInteger();
      } else if (left instanceof IDecimalItem) {
        integerRight = ((IDecimalItem) right).asInteger();
      } else {
        throw new InvalidTypeException(right.getClass());
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
        throw new InvalidTypeException(left.getClass());
      }

      BigDecimal decimalRight;
      if (left instanceof IIntegerItem) {
        decimalRight = ((IIntegerItem) right).asDecimal();
      } else if (left instanceof IDecimalItem) {
        decimalRight = ((IDecimalItem) right).asDecimal();
      } else {
        throw new InvalidTypeException(right.getClass());
      }
      BigDecimal result = decimalLeft.subtract(decimalRight, MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger integerLeft;
      if (left instanceof IIntegerItem) {
        integerLeft = ((IIntegerItem) left).asInteger();
      } else if (left instanceof IDecimalItem) {
        integerLeft = ((IDecimalItem) left).asInteger();
      } else {
        throw new InvalidTypeException(left.getClass());
      }

      BigInteger integerRight;
      if (left instanceof IIntegerItem) {
        integerRight = ((IIntegerItem) right).asInteger();
      } else if (left instanceof IDecimalItem) {
        integerRight = ((IDecimalItem) right).asInteger();
      } else {
        throw new InvalidTypeException(right.getClass());
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
        throw new InvalidTypeException(left.getClass());
      }

      BigDecimal decimalRight;
      if (left instanceof IIntegerItem) {
        decimalRight = ((IIntegerItem) right).asDecimal();
      } else if (left instanceof IDecimalItem) {
        decimalRight = ((IDecimalItem) right).asDecimal();
      } else {
        throw new InvalidTypeException(right.getClass());
      }
      BigDecimal result = decimalLeft.multiply(decimalRight, MATH_CONTEXT);
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
        throw new MetapathDynamicException("err:FOAR0001", "Division by zero");
      }

      BigDecimal decimalDividend = dividend.asDecimal();
      BigDecimal result = decimalDividend.divide(decimalDivisor, MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger integerDivisor = divisor.asInteger();

      if (BigInteger.ZERO.equals(integerDivisor)) {
        throw new MetapathDynamicException("err:FOAR0001", "Division by zero");
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
        throw new MetapathDynamicException("err:FOAR0001", "Division by zero");
      }

      BigDecimal decimalDividend = dividend.asDecimal();
      BigInteger result = decimalDividend.divideToIntegralValue(decimalDivisor, MATH_CONTEXT).toBigInteger();
      retval = IIntegerItem.valueOf(result);
    } else {
      // create an integer result
      BigInteger integerDivisor = divisor.asInteger();

      if (BigInteger.ZERO.equals(integerDivisor)) {
        throw new MetapathDynamicException("err:FOAR0001", "Division by zero");
      }

      BigInteger result = dividend.asInteger().divide(integerDivisor);
      retval = IIntegerItem.valueOf(result);
    }
    return retval;
  }

  /**
   * Based on {@link "https://www.w3.org/TR/xpath-functions-31/#func-numeric-mod"}.
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
      throw new InvalidTypeException(divisor.getClass());
    }

    if (BigDecimal.ZERO.equals(decimalDivisor)) {
      throw new MetapathDynamicException("err:FOAR0001", "Division by zero");
    }

    BigDecimal decimalDividend;
    if (dividend instanceof IIntegerItem) {
      decimalDividend = ((IIntegerItem) dividend).asDecimal();
    } else if (dividend instanceof IDecimalItem) {
      decimalDividend = ((IDecimalItem) dividend).asDecimal();
    } else {
      throw new InvalidTypeException(dividend.getClass());
    }

    INumericItem retval;
    if (BigDecimal.ZERO.equals(decimalDividend)) {
      retval = dividend;
    } else {
      BigDecimal result = decimalDividend.remainder(decimalDivisor, MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    }
    return retval;
  }

  public static INumericItem opNumericUnaryMinus(INumericItem item) {
    INumericItem retval;
    if (item instanceof IDecimalItem) {
      // create a decimal result
      BigDecimal decimal = item.asDecimal();
      BigDecimal result = decimal.negate(MATH_CONTEXT);
      retval = IDecimalItem.valueOf(result);
    } else if (item instanceof IIntegerItem) {
      // create a decimal result
      BigInteger integer = item.asInteger();
      BigInteger result = integer.negate();
      retval = IIntegerItem.valueOf(result);
    } else {
      throw new InvalidTypeException(item.getClass());
    }
    return retval;
  }

  // TODO: use a cast instead?
  public static INumericItem toNumeric(IItem item) throws InvalidTypeException {
    INumericItem retval;
    if (item == null) {
      retval = null;
    } else {
      // atomize
      IAnyAtomicItem atomicItem = Functions.fnDataItem(item);

      if (atomicItem instanceof INumericItem) {
        retval = (INumericItem) atomicItem;
      } else {
        String value = atomicItem.asString();
        try {
          retval = IDecimalItem.valueOf(value);
        } catch (NumberFormatException ex) {
          throw new InvalidTypeException(String.format("The value '%s' is not a valid decimal value", value), ex);
        }
      }
    }
    return retval;
  }

  /**
   * Casts a result to a numeric value. If the result is a {@link ISequence}, then the first item is
   * used. If the sequence is empty, then a {@link InvalidTypeException} is thrown.
   * 
   * @param result
   *          a Metapath result
   * @param requireSingleton
   *          if {@code true} then a {@link InvalidTypeException} is thrown if the provided result is
   *          sequence that contains more than one item
   * @return the item as a numeric, or {@code null} if the result is an empty sequence
   * @throws InvalidTypeException
   *           if the sequence contains more than one item, or the item cannot be cast to a decimal
   *           value
   * 
   */
  public static INumericItem toNumeric(IMetapathResult result, boolean requireSingleton) {
    IItem item;
    if (result instanceof ISequence) {
      item = getFirstItem((ISequence) result, requireSingleton);
    } else {
      item = (IItem) result;
    }

    return toNumeric(item);
  }

  /**
   * Retrieves the first item in a sequence. If the sequence is empty, a {@code null} result is
   * returned. If requireSingleton is {@code true} and the sequence contains more than one item, a
   * {@link InvalidTypeException} is thrown.
   * 
   * @param sequence
   *          the sequence to retrieve the first item from
   * @param requireSingleton
   *          if {@code true} then a {@link InvalidTypeException} is thrown if the sequence contains
   *          more than one item
   * @return {@code null} if the sequence is empty, or the item otherwise
   * @throws InvalidTypeException
   *           if the sequence contains more than one item and requireSingleton is {@code true}
   */
  public static IItem getFirstItem(ISequence sequence, boolean requireSingleton) throws InvalidTypeException {
    IItem retval = null;
    if (!sequence.isEmpty()) {
      List<? extends IItem> items = sequence.asList();
      if (requireSingleton && items.size() != 1) {
        throw new InvalidTypeException("sequence contains more than one item");
      }
      retval = items.iterator().next();
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

  public static IBooleanItem fnNot(IMetapathResult arg) {
    return IBooleanItem.valueOf(!fnBooleanInternal(arg));
  }
}
