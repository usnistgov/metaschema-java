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

package gov.nist.secauto.metaschema.core.metapath;

import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyUriItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDecimalItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IYearMonthDurationItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IArrayItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IMapItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IMapKey;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.net.URI;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class TestUtils {
  /**
   * Create an empty sequence.
   *
   * @param <T>
   *          the item Java type contained within the sequence
   * @return the sequence
   */
  @NonNull
  public static <T extends IItem> ISequence<T> sequence() {
    return ISequence.of();
  }

  /**
   * Create a sequence containing the provided items.
   *
   * @param <T>
   *          the item Java type contained within the sequence
   * @param items
   *          the items to add to the sequence
   * @return the sequence
   */
  @SafeVarargs
  @NonNull
  public static <T extends IItem> ISequence<T> sequence(@NonNull T... items) {
    return ISequence.of(items);
  }

  /**
   * Create an empty array item.
   *
   * @param <T>
   *          the item Java type contained within the array item
   * @return the array item
   */
  @NonNull
  public static <T extends ICollectionValue> IArrayItem<T> array() {
    return IArrayItem.of();
  }

  /**
   * Create a array item containing the provided items.
   *
   * @param <T>
   *          the item Java type contained within the array item
   * @param items
   *          the items to add to the array item
   * @return the array item
   */
  @SafeVarargs
  @NonNull
  public static <T extends ICollectionValue> IArrayItem<T> array(@NonNull T... items) {
    return IArrayItem.of(items);
  }

  /**
   * Create a map item containing the provided entries.
   *
   * @param <T>
   *          the entry value Java type contained within the map item
   * @param entries
   *          the entries to add to the map item
   * @return the map item
   */
  @SafeVarargs
  @NonNull
  public static <T extends ICollectionValue> IMapItem<T> map(@NonNull Map.Entry<IMapKey, T>... entries) {
    return IMapItem.ofEntries(entries);
  }

  /**
   * Create a map entry using the provided key and value.
   *
   * @param <T>
   *          the entry value Java type
   * @param key
   *          the entry key
   * @param value
   *          the entry value
   * @return the map entry
   */
  @NonNull
  public static <T extends ICollectionValue> Map.Entry<IMapKey, T> entry(
      @NonNull IAnyAtomicItem key,
      @NonNull T value) {
    return IMapItem.entry(key, value);
  }

  /**
   * Create a boolean item using the provided value.
   *
   * @param value
   *          the boolean value
   * @return the boolean item
   */
  @NonNull
  public static IBooleanItem bool(boolean value) {
    return IBooleanItem.valueOf(value);
  }

  /**
   * Create a decimal item using the provided value.
   *
   * @param value
   *          the decimal value
   * @return the decimal item
   */
  public static IDecimalItem decimal(@NonNull String value) {
    return IDecimalItem.valueOf(new BigDecimal(value, MathContext.DECIMAL64));
  }

  /**
   * Create a decimal item using the provided value.
   *
   * @param value
   *          the decimal value
   * @return the decimal item
   */
  @NonNull
  public static IDecimalItem decimal(int value) {
    return IDecimalItem.valueOf(value);
  }

  /**
   * Create a decimal item using the provided value.
   *
   * @param value
   *          the decimal value
   * @return the decimal item
   */
  @NonNull
  public static IDecimalItem decimal(double value) {
    return IDecimalItem.valueOf(value);
  }

  /**
   * Create an integer item using the provided value.
   *
   * @param value
   *          the integer value
   * @return the integer item
   */
  @NonNull
  public static IIntegerItem integer(int value) {
    return IIntegerItem.valueOf(ObjectUtils.notNull(BigInteger.valueOf(value)));
  }

  /**
   * Create a string item using the provided value.
   *
   * @param value
   *          the string value
   * @return the string item
   */
  @NonNull
  public static IStringItem string(@NonNull String value) {
    return IStringItem.valueOf(value);
  }

  /**
   * Create a uri item using the provided value.
   *
   * @param value
   *          the uri value
   * @return the uri item
   */
  @NonNull
  public static IAnyUriItem uri(@NonNull String value) {
    URI uri = URI.create(value);
    assert uri != null;
    return IAnyUriItem.valueOf(uri);
  }

  /**
   * Create a duration item using the provided value indicating the years, months,
   * and days of the duration.
   *
   * @param value
   *          the duration value
   * @return the duration item
   */
  @NonNull
  public static IYearMonthDurationItem yearMonthDuration(@NonNull String value) {
    return IYearMonthDurationItem.valueOf(value);
  }

  /**
   * Create a duration item using the provided value indicating the seconds and
   * nanoseconds of the duration.
   *
   * @param value
   *          the duration value
   * @return the duration item
   */
  @NonNull
  public static IDayTimeDurationItem dayTimeDuration(@NonNull String value) {
    return IDayTimeDurationItem.valueOf(value);
  }

  private TestUtils() {
    // disable construction
  }
}
