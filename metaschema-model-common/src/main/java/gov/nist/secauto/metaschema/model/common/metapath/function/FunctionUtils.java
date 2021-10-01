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

import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.item.InvalidTypeException;
import gov.nist.secauto.metaschema.model.common.metapath.item.MetapathDynamicException;

import java.math.BigInteger;
import java.math.MathContext;
import java.util.List;

public class FunctionUtils {
  private FunctionUtils() {
    // disable
  }

  public static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  public static int asInteger(INumericItem value) throws MetapathDynamicException {
    return asInteger(value.asInteger());
  }

  public static int asInteger(BigInteger value) throws MetapathDynamicException {
    try {
      return value.intValueExact();
    } catch (ArithmeticException ex) {
      throw new MetapathDynamicException("err:FODT0002", "Overflow/underflow in duration operation. ", ex);
    }
  }

  public static long asLong(INumericItem value) throws MetapathDynamicException {
    return asInteger(value.asInteger());
  }

  public static long asLong(IIntegerItem value) throws MetapathDynamicException {
    return asInteger(value.asInteger());
  }

  public static long asLong(BigInteger value) throws MetapathDynamicException {
    try {
      return value.longValueExact();
    } catch (ArithmeticException ex) {
      throw new MetapathDynamicException("err:FODT0002", "Overflow/underflow in duration operation. ", ex);
    }
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
  public static IItem getFirstItem(ISequence<?> sequence, boolean requireSingleton) throws InvalidTypeException {
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
  // TODO: replace this with a cast
  public static INumericItem toNumeric(ISequence<?> result, boolean requireSingleton) {
    IItem item = getFirstItem(result, requireSingleton);
    return toNumeric(item);
  }

  // TODO: use a cast instead?
  public static INumericItem toNumeric(IItem item) throws InvalidTypeException {
    INumericItem retval;
    if (item == null) {
      retval = null;
    } else {
      // atomize
      IAnyAtomicItem atomicItem = XPathFunctions.fnDataItem(item);

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
}
