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

package gov.nist.secauto.metaschema.binding.model;

import gov.nist.secauto.metaschema.binding.io.BindingException;

/**
 * This exception is used to tunnel a {@link BindingException} out from inside an initialization
 * method that cannot throw an exception. Indicates that a {@link BindingException} error has
 * occurred while initializing a Metaschema model class instance.
 */
public class ModelInitializationException
    extends RuntimeException {

  /**
   * The serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new model initialization exception with {@code null} as its detail message. The
   * cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
   */
  public ModelInitializationException() {
    // No message
  }

  /**
   * Constructs a new model initialization exception with the specified detail message. The cause is
   * not initialized, and may subsequently be initialized by a call to {@link #initCause}.
   *
   * @param message
   *          the detail message. The detail message is saved for later retrieval by the
   *          {@link #getMessage()} method.
   */
  public ModelInitializationException(String message) {
    super(message);
  }

  /**
   * Constructs a new model initialization exception with the specified cause and a detail message of
   * {@code (cause==null ? null : cause.toString())} (which typically contains the class and detail
   * message of {@code cause}). This constructor is useful for runtime exceptions that are little more
   * than wrappers for other throwables.
   *
   * @param cause
   *          the cause (which is saved for later retrieval by the {@link #getCause()} method). (A
   *          {@code null} value is permitted, and indicates that the cause is nonexistent or
   *          unknown.)
   */
  public ModelInitializationException(BindingException cause) {
    super(cause);
  }

  /**
   * Constructs a new model initialization exception with the specified detail message and cause.
   * <p>
   * Note that the detail message associated with {@code cause} is <i>not</i> automatically
   * incorporated in this runtime exception's detail message.
   *
   * @param message
   *          the detail message (which is saved for later retrieval by the {@link #getMessage()}
   *          method).
   * @param cause
   *          the cause (which is saved for later retrieval by the {@link #getCause()} method). (A
   *          {@code null} value is permitted, and indicates that the cause is nonexistent or
   *          unknown.)
   */
  public ModelInitializationException(String message, BindingException cause) {
    super(message, cause);
  }

  /**
   * Constructs a new model initialization exception with the specified detail message, cause,
   * suppression enabled or disabled, and writable stack trace enabled or disabled.
   *
   * @param message
   *          the detail message.
   * @param cause
   *          the cause. (A {@code null} value is permitted, and indicates that the cause is
   *          nonexistent or unknown.)
   * @param enableSuppression
   *          whether or not suppression is enabled or disabled
   * @param writableStackTrace
   *          whether or not the stack trace should be writable
   */
  public ModelInitializationException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  @Override
  public synchronized BindingException getCause() {
    return (BindingException) super.getCause();
  }

  @Override
  public synchronized Throwable initCause(Throwable cause) {
    throw new UnsupportedOperationException();
  }

}
