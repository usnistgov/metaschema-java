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

package gov.nist.secauto.metaschema.model.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Stream;

public final class ObjectUtils {
  private ObjectUtils() {
    // disable construction
  }

  public static <T> @NotNull T notNull(T obj) {
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
  @NotNull
  public static <T> T requireNonNull(T obj) {
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
  @NotNull
  public static <T> T requireNonNull(T obj, @NotNull String message) {
    if (obj == null) {
      throw new NullPointerException(message); // NOPMD
    }
    return obj;
  }

  @SuppressWarnings("null")
  @NotNull
  public static <T> Stream<@NotNull T> filterNull(T item) {
    return Objects.nonNull(item) ? Stream.of(item) : Stream.empty();
  }
}
