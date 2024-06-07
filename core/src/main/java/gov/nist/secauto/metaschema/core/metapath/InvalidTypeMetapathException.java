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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Provides a convenient way to raise a
 * {@link TypeMetapathException#INVALID_TYPE_ERROR}.
 */
public class InvalidTypeMetapathException
    extends TypeMetapathException {

  /**
   * the serial version UID.
   */
  private static final long serialVersionUID = 1L;

  @Nullable
  private final IItem item;

  /**
   * Constructs a new exception with the provided {@code item} and {@code cause},
   * using a default message.
   *
   * @param item
   *          the item related to the invalid type error
   * @param cause
   *          the original exception cause
   */
  public InvalidTypeMetapathException(@NonNull IItem item, @NonNull Throwable cause) {
    super(INVALID_TYPE_ERROR, String.format("Invalid data type '%s'", item.getClass().getName()),
        cause);
    this.item = item;
  }

  /**
   * Constructs a new exception with the provided {@code item} and no cause, using
   * a default message.
   *
   * @param item
   *          the item related to the invalid type error
   */
  public InvalidTypeMetapathException(@NonNull IItem item) {
    super(INVALID_TYPE_ERROR, String.format("Invalid data type '%s'", item.getClass().getName()));
    this.item = item;
  }

  /**
   * Constructs a new exception with the provided {@code item}, {@code message},
   * and {@code cause}.
   *
   * @param item
   *          the item related to the invalid type error
   * @param message
   *          the exception message
   * @param cause
   *          the original exception cause
   */
  public InvalidTypeMetapathException(@Nullable IItem item, @Nullable String message, @NonNull Throwable cause) {
    super(INVALID_TYPE_ERROR, message, cause);
    this.item = item;
  }

  /**
   * Constructs a new exception with the provided {@code item}, {@code message},
   * and no cause.
   *
   * @param item
   *          the item related to the invalid type error
   * @param message
   *          the exception message
   */
  public InvalidTypeMetapathException(@Nullable IItem item, @Nullable String message) {
    super(INVALID_TYPE_ERROR, message);
    this.item = item;
  }

  /**
   * Get the associated item, if provided for the exception.
   *
   * @return the item or {@code null} if not item was provided
   */
  @Nullable
  public IItem getItem() {
    return item;
  }
}
