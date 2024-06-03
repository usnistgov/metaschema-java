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

package gov.nist.secauto.metaschema.core.util;

import java.util.Objects;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public final class ObjectUtils {
  private ObjectUtils() {
    // disable construction
  }

  /**
   * Assert that the provided object is not {@code null}.
   * <p>
   * This method sets the expectation that the provided object is not {@code null}
   * in cases where a non-null value is required.
   *
   * @param <T>
   *          the object type
   * @param obj
   *          the object
   * @return the object
   */
  @NonNull
  public static <T> T notNull(@Nullable T obj) {
    assert obj != null;
    return obj;
  }

  /**
   * Require a non-null value.
   *
   * @param <T>
   *          the type of the reference
   * @param obj
   *          the object reference to check for nullity
   * @return {@code obj} if not {@code null}
   * @throws NullPointerException
   *           if {@code obj} is {@code null}
   */
  @NonNull
  @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
  public static <T> T requireNonNull(@Nullable T obj) {
    if (obj == null) {
      throw new NullPointerException(); // NOPMD
    }
    return obj;
  }

  /**
   * Require a non-null value.
   *
   * @param <T>
   *          the type of the reference
   * @param obj
   *          the object reference to check for nullity
   * @param message
   *          detail message to be used in the event that a {@code
   *                NullPointerException} is thrown
   * @return {@code obj} if not {@code null}
   * @throws NullPointerException
   *           if {@code obj} is {@code null}
   */
  @NonNull
  @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
  public static <T> T requireNonNull(@Nullable T obj, @NonNull String message) {
    if (obj == null) {
      throw new NullPointerException(message); // NOPMD
    }
    return obj;
  }

  /**
   * A filter used to remove null items from a stream.
   *
   * @param <T>
   *          the item type
   * @param item
   *          the item to filter
   * @return the item as a steam or an empty stream if the item is {@code null}
   */
  @SuppressWarnings("null")
  @NonNull
  public static <T> Stream<T> filterNull(T item) {
    return Objects.nonNull(item) ? Stream.of(item) : Stream.empty();
  }

  /**
   * Cast the provided object as the requested return type.
   *
   * @param <T>
   *          the Java type to cast the object to
   * @param obj
   *          the object to cast
   * @return the object cast to the requested type
   * @throws ClassCastException
   *           if the object cannot be cast to the requested type
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public static <T> T asType(@NonNull Object obj) {
    return (T) obj;
  }

  /**
   * Cast the provided object as the requested return type.
   * <p>
   * If the object is {@code null}, the returned value will be {@code null}.
   *
   * @param <T>
   *          the Java type to cast the object to
   * @param obj
   *          the object to cast, which may be {@code null}
   * @return the object cast to the requested type, or {@code null} if the
   *         provided object is {@code null}
   * @throws ClassCastException
   *           if the object cannot be cast to the requested type
   */
  @SuppressWarnings("unchecked")
  @Nullable
  public static <T> T asNullableType(@Nullable Object obj) {
    return (T) obj;
  }
}
