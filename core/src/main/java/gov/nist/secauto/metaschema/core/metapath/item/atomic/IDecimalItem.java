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

import java.math.BigDecimal;
import java.math.RoundingMode;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IDecimalItem extends INumericItem {
  @SuppressWarnings("null")
  @NonNull
  IDecimalItem ZERO = valueOf(BigDecimal.ZERO);

  /**
   * Construct a new decimal item using the provided string {@code value}.
   *
   * @param value
   *          a string representing a decimal value
   * @return the new item
   */
  @NonNull
  static IDecimalItem valueOf(@NonNull String value) {
    try {
      return valueOf(MetaschemaDataTypeProvider.DECIMAL.parse(value));
    } catch (IllegalArgumentException ex) {
      throw new InvalidValueForCastFunctionException(String.format("Unable to parse string value '%s'", value),
          ex);
    }
  }

  /**
   * Construct a new decimal item using the provided {@code value}.
   *
   * @param value
   *          a long value
   * @return the new item
   */
  @NonNull
  static IDecimalItem valueOf(long value) {
    return valueOf(ObjectUtils.notNull(BigDecimal.valueOf(value)));
  }

  /**
   * Construct a new decimal item using the provided {@code value}.
   *
   * @param value
   *          a double value
   * @return the new item
   */
  @NonNull
  static IDecimalItem valueOf(double value) {
    return valueOf(ObjectUtils.notNull(Double.toString(value)));
  }

  /**
   * Construct a new decimal item using the provided {@code value}.
   *
   * @param value
   *          a decimal value
   * @return the new item
   */
  @NonNull
  static IDecimalItem valueOf(@NonNull BigDecimal value) {
    return new DecimalItemImpl(value);
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
  static IDecimalItem cast(@Nullable IAnyAtomicItem item) {
    return MetaschemaDataTypeProvider.DECIMAL.cast(item);
  }

  @Override
  default IDecimalItem castAsType(IAnyAtomicItem item) {
    return valueOf(cast(item).asDecimal());
  }

  @Override
  default boolean toEffectiveBoolean() {
    return !BigDecimal.ZERO.equals(asDecimal());
  }

  @SuppressWarnings("null")
  @Override
  default INumericItem abs() {
    return new DecimalItemImpl(asDecimal().abs());
  }

  @SuppressWarnings("null")
  @Override
  default IIntegerItem ceiling() {
    return IIntegerItem.valueOf(asDecimal().setScale(0, RoundingMode.CEILING).toBigIntegerExact());
  }

  @SuppressWarnings("null")
  @Override
  default IIntegerItem floor() {
    return IIntegerItem.valueOf(asDecimal().setScale(0, RoundingMode.FLOOR).toBigIntegerExact());
  }

  /**
   * Compares this value with the argument.
   *
   * @param item
   *          the item to compare with this value
   * @return a negative integer, zero, or a positive integer if this value is less
   *         than, equal to, or greater than the {@code item}.
   */
  default int compareTo(@NonNull IDecimalItem item) {
    return asDecimal().compareTo(item.asDecimal());
  }

  @Override
  default int compareTo(IAnyAtomicItem item) {
    return compareTo(cast(item));
  }
}
