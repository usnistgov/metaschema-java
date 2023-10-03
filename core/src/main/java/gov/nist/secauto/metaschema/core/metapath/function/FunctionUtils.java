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

package gov.nist.secauto.metaschema.core.metapath.function;

import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.InvalidTypeMetapathException;
import gov.nist.secauto.metaschema.core.metapath.TypeMetapathException;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDecimalItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.math.BigInteger;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class FunctionUtils {
  public static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private FunctionUtils() {
    // disable
  }

  /**
   * Converts a {@link INumericItem} value to an integer value.
   *
   * @param value
   *          the value to convert
   * @return the integer value
   * @throws ArithmeticException
   *           if the provided value will not exactly fit in an {@code int}
   */
  public static int asInteger(@NonNull INumericItem value) {
    return asInteger(value.asInteger());
  }

  /**
   * Converts a {@link BigInteger} value to an integer value.
   *
   * @param value
   *          the value to convert
   * @return the integer value
   * @throws ArithmeticException
   *           if the provided value will not exactly fit in an {@code int}
   */
  public static int asInteger(@NonNull BigInteger value) {
    return value.intValueExact();
  }

  /**
   * Converts a {@link INumericItem} value to a long value.
   *
   * @param value
   *          the value to convert
   * @return the long value
   * @throws ArithmeticException
   *           if the provided value will not exactly fit in an {@code long}
   */
  public static long asLong(@NonNull INumericItem value) {
    return asLong(value.asInteger());
  }

  /**
   * Converts a {@link BigInteger} value to a long value.
   *
   * @param value
   *          the value to convert
   * @return the long value
   * @throws ArithmeticException
   *           if the provided value will not exactly fit in an {@code long}
   */
  public static long asLong(@NonNull BigInteger value) {
    return value.longValueExact();
  }

  /**
   * Retrieves the first item in a sequence. If the sequence is empty, a
   * {@link TypeMetapathException} exception is thrown. If requireSingleton is
   * {@code true} and the sequence contains more than one item, a
   * {@link TypeMetapathException} is thrown.
   *
   * @param <ITEM>
   *          the item type to return derived from the provided sequence
   * @param sequence
   *          the sequence to retrieve the first item from
   * @param requireSingleton
   *          if {@code true} then a {@link TypeMetapathException} is thrown if
   *          the sequence contains more than one item
   * @return {@code null} if the sequence is empty, or the item otherwise
   * @throws TypeMetapathException
   *           if the sequence is empty, or contains more than one item and
   *           requireSingleton is {@code true}
   */
  @NonNull
  public static <ITEM extends IItem> ITEM requireFirstItem(@NonNull ISequence<ITEM> sequence,
      boolean requireSingleton) {
    if (sequence.isEmpty()) {
      throw new InvalidTypeMetapathException(
          null,
          "Expected a non-empty sequence, but sequence was empty.");
    }
    List<ITEM> items = sequence.asList();
    if (requireSingleton && items.size() != 1) {
      throw new InvalidTypeMetapathException(
          null,
          String.format("sequence expected to contain one item, but found '%d'", items.size()));
    }
    return ObjectUtils.notNull(items.iterator().next());
  }

  /**
   * Retrieves the first item in a sequence. If the sequence is empty, a
   * {@code null} result is returned. If requireSingleton is {@code true} and the
   * sequence contains more than one item, a {@link TypeMetapathException} is
   * thrown.
   *
   * @param <ITEM>
   *          the item type to return derived from the provided sequence
   * @param sequence
   *          the sequence to retrieve the first item from
   * @param requireSingleton
   *          if {@code true} then a {@link TypeMetapathException} is thrown if
   *          the sequence contains more than one item
   * @return {@code null} if the sequence is empty, or the item otherwise
   * @throws TypeMetapathException
   *           if the sequence contains more than one item and requireSingleton is
   *           {@code true}
   */
  @Nullable
  public static <ITEM extends IItem> ITEM getFirstItem(@NonNull ISequence<ITEM> sequence, boolean requireSingleton) {
    @Nullable ITEM retval = null;
    if (!sequence.isEmpty()) {
      List<ITEM> items = sequence.asList();
      if (requireSingleton && items.size() != 1) {
        throw new InvalidTypeMetapathException(
            null,
            String.format("sequence expected to contain one item, but found '%d'", items.size()));
      }
      retval = items.iterator().next();
    }
    return retval;
  }

  /**
   * Gets the first item of the provided sequence as a {@link INumericItem} value.
   * If the sequence is empty, then a {@code null} value is returned.
   *
   * @param sequence
   *          a Metapath sequence containing the value to convert
   * @param requireSingleton
   *          if {@code true} then a {@link TypeMetapathException} is thrown if
   *          the sequence contains more than one item
   * @return the numeric item value, or {@code null} if the result is an empty
   *         sequence
   * @throws TypeMetapathException
   *           if the sequence contains more than one item, or the item cannot be
   *           cast to a numeric value
   *
   */
  @Nullable
  public static INumericItem toNumeric(@NonNull ISequence<?> sequence, boolean requireSingleton) {
    IItem item = getFirstItem(sequence, requireSingleton);
    return item == null ? null : toNumeric(item);
  }

  /**
   * Gets the provided item value as a {@link INumericItem} value.
   *
   * @param item
   *          the value to convert
   * @return the numeric item value
   * @throws TypeMetapathException
   *           if the sequence contains more than one item, or the item cannot be
   *           cast to a numeric value
   */
  @NonNull
  public static INumericItem toNumeric(@NonNull IItem item) {
    // atomize
    IAnyAtomicItem atomicItem = FnData.fnDataItem(item);
    return toNumeric(atomicItem);
  }

  /**
   * Gets the provided item value as a {@link INumericItem} value.
   *
   * @param item
   *          the value to convert
   * @return the numeric item value
   * @throws TypeMetapathException
   *           if the item cannot be cast to a numeric value
   */
  @NonNull
  public static INumericItem toNumeric(@NonNull IAnyAtomicItem item) {
    try {
      return IDecimalItem.cast(item);
    } catch (InvalidValueForCastFunctionException ex) {
      throw new InvalidTypeMetapathException(item, ex.getLocalizedMessage(), ex);
    }
  }

  /**
   * Gets the provided item value as a {@link INumericItem} value. If the item is
   * {@code null}, then a {@code null} value is returned.
   *
   * @param item
   *          the value to convert
   * @return the numeric item value
   * @throws TypeMetapathException
   *           if the item cannot be cast to a numeric value
   */
  @Nullable
  public static INumericItem toNumericOrNull(@Nullable IAnyAtomicItem item) {
    return item == null ? null : toNumeric(item);
  }

  /**
   * Casts the provided {@code item} as the result type, if the item is not
   * {@code null}.
   *
   * @param <TYPE>
   *          the Java type to cast to
   * @param item
   *          the value to cast
   * @return the item cast to the required type or {@code null} if the item is
   *         {@code null}
   * @throws ClassCastException
   *           if the item's type is not compatible with the requested type
   */
  @SuppressWarnings("unchecked")
  @Nullable
  public static <TYPE extends IItem> TYPE asTypeOrNull(@Nullable IItem item) {
    return (TYPE) item;
  }

  /**
   * Casts the provided {@code item} as the result type.
   *
   * @param <TYPE>
   *          the Java type to cast to
   * @param item
   *          the value to cast
   * @return the item cast to the required type
   * @throws ClassCastException
   *           if the item's type is not compatible with the requested type
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public static <TYPE extends IItem> TYPE asType(@NonNull IItem item) {
    return (TYPE) item;
  }

  /**
   * Casts the provided {@code item} as the result sequence type.
   *
   * @param <TYPE>
   *          the Java type to cast to
   * @param sequence
   *          the values to cast
   * @return the sequence cast to the required type
   * @throws ClassCastException
   *           if the sequence's type is not compatible with the requested type
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public static <TYPE extends IItem> ISequence<TYPE> asType(@NonNull ISequence<?> sequence) {
    return (ISequence<TYPE>) sequence;
  }

  /**
   * Casts the provided {@code item} as the result type.
   *
   * @param <TYPE>
   *          the Java type to cast to
   * @param clazz
   *          the Java class instance for the requested type
   * @param item
   *          the value to cast
   * @return the item cast to the required type
   * @throws InvalidTypeMetapathException
   *           if the provided item is {@code null} or if the item's type is not
   *           assignment compatible to the requested type
   */
  @NonNull
  public static <TYPE extends IItem> TYPE requireType(Class<TYPE> clazz, IItem item) {
    if (item == null) {
      throw new InvalidTypeMetapathException(
          null,
          String.format("Expected non-null type '%s', but the node was null.",
              clazz.getName()));
    } else if (!clazz.isInstance(item)) {
      throw new InvalidTypeMetapathException(
          item,
          String.format("Expected type '%s', but the node was type '%s'.",
              clazz.getName(),
              item.getClass().getName()));
    }
    return asType(item);
  }

  /**
   * Casts the provided {@code item} as the result type, if the item is not
   * {@code null}.
   *
   * @param <TYPE>
   *          the Java type to cast to
   * @param clazz
   *          the Java class instance for the requested type
   * @param item
   *          the value to cast
   * @return the item cast to the required type or {@code null} if the item is
   *         {@code null}
   * @throws InvalidTypeMetapathException
   *           if the provided item is {@code null} or if the item's type is not
   *           assignment compatible to the requested type
   */
  @Nullable
  public static <TYPE extends IItem> TYPE requireTypeOrNull(Class<TYPE> clazz, @Nullable IItem item) {
    if (item == null || clazz.isInstance(item)) {
      return asTypeOrNull(item);
    }
    throw new InvalidTypeMetapathException(
        item,
        String.format("Expected type '%s', but the node was type '%s'.",
            clazz.getName(),
            item.getClass().getName()));
  }

  /**
   * Get a stream of item data types for the stream of items.
   *
   * @param items
   *          the Metapath items to get the data types for
   * @return a stream of data type classes
   */
  @NonNull
  public static Stream<Class<?>> getTypes(@NonNull Stream<? extends IItem> items) {
    return ObjectUtils.notNull(items.map(item -> item.getClass()));
  }

  /**
   * Count the occurrences of the provided data type item {@code classes} used in
   * the set of provided {@code items}.
   *
   * @param classes
   *          the Metapath item classes to count
   * @param items
   *          the Metapath items to analyze
   * @return a mapping of Metapath item class to count
   */
  @NonNull
  public static Map<Class<? extends IItem>, Integer> countTypes(
      @NonNull Set<Class<? extends IItem>> classes,
      @NonNull List<? extends IItem> items) {
    Map<Class<? extends IItem>, Integer> retval = new HashMap<>();
    for (IItem item : items) {
      Class<? extends IItem> itemClass = item.getClass();
      for (Class<? extends IItem> clazz : classes) {
        if (clazz.isAssignableFrom(itemClass)) {
          retval.compute(clazz, (cl, current) -> current == null ? 1 : current + 1);
        }
      }
    }
    return retval;
  }
}
