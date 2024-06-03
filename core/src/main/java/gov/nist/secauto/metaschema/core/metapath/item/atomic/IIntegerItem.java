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
import gov.nist.secauto.metaschema.core.metapath.InvalidTypeMetapathException;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidValueForCastFunctionException;

import java.math.BigInteger;

import edu.umd.cs.findbugs.annotations.NonNull;

// TODO: extends IDecimalItem instead
public interface IIntegerItem extends IDecimalItem {

  @SuppressWarnings("null")
  @NonNull
  IIntegerItem ONE = valueOf(BigInteger.ONE);
  @SuppressWarnings("null")
  @NonNull
  IIntegerItem ZERO = valueOf(BigInteger.ZERO);
  @SuppressWarnings("null")
  @NonNull
  IIntegerItem NEGATIVE_ONE = valueOf(BigInteger.ONE.negate());

  /**
   * Create an item from an existing integer value.
   *
   * @param value
   *          a string representing an integer value
   * @return the item
   * @throws InvalidTypeMetapathException
   *           if the provided value is not an integer
   */
  @NonNull
  static IIntegerItem valueOf(@NonNull String value) {
    try {
      return valueOf(new BigInteger(value));
    } catch (NumberFormatException ex) {
      throw new InvalidTypeMetapathException(null,
          ex.getMessage(),
          ex);
    }
  }

  /**
   * Construct a new integer item using the provided {@code value}.
   *
   * @param value
   *          a long value
   * @return the new item
   */
  @NonNull
  static IIntegerItem valueOf(int value) {
    @SuppressWarnings("null")
    @NonNull BigInteger bigInteger = BigInteger.valueOf(value);
    return valueOf(bigInteger);
  }

  /**
   * Construct a new integer item using the provided {@code value}.
   *
   * @param value
   *          a long value
   * @return the new item
   */
  @NonNull
  static IIntegerItem valueOf(long value) {
    @SuppressWarnings("null")
    @NonNull BigInteger bigInteger = BigInteger.valueOf(value);
    return valueOf(bigInteger);
  }

  /**
   * Construct a new integer item using the provided {@code value}.
   *
   * @param value
   *          an integer value
   * @return the new item
   */
  @NonNull
  static IIntegerItem valueOf(@NonNull BigInteger value) {
    int signum = value.signum();

    IIntegerItem retval;
    if (signum == -1) { // negative
      retval = new IntegerItemImpl(value);
    } else if (signum == 0) { // zero
      retval = INonNegativeIntegerItem.valueOf(value);
    } else { // positive
      retval = IPositiveIntegerItem.valueOf(value);
    }
    return retval;
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
  static IIntegerItem cast(@NonNull IAnyAtomicItem item) {
    return MetaschemaDataTypeProvider.INTEGER.cast(item);
  }

  @Override
  IIntegerItem abs();

  @Override
  default IIntegerItem ceiling() {
    return this;
  }

  @Override
  default IIntegerItem floor() {
    return this;
  }

  @Override
  default IIntegerItem castAsType(IAnyAtomicItem item) {
    return valueOf(cast(item).asInteger());
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
  default int compareTo(IIntegerItem item) {
    return asInteger().compareTo(item.asInteger());
  }
}
