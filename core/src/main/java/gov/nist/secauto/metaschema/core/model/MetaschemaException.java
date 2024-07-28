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

package gov.nist.secauto.metaschema.core.model;

public class MetaschemaException
    extends Exception {

  /**
   * The serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Create a new Metaschema exception with a provided message.
   *
   * @param message
   *          text describing the cause of the exception
   */
  public MetaschemaException(String message) {
    super(message);
  }

  /**
   * Create a new Metaschema exception based on the provided cause.
   *
   * @param cause
   *          the exception that caused this exception
   */
  public MetaschemaException(Throwable cause) {
    super(cause);
  }

  /**
   * Create a new Metaschema exception with a provided message based on the
   * provided cause.
   *
   * @param message
   *          text describing the cause of the exception
   * @param cause
   *          the exception that caused this exception
   */
  public MetaschemaException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a new Metaschema exception with a provided message based on the
   * provided cause.
   *
   *
   * @param message
   *          text describing the cause of the exception
   * @param cause
   *          the exception that caused this exception
   * @param enableSuppression
   *          whether or not suppression is enabled or disabled
   * @param writableStackTrace
   *          whether or not the stack trace should be writable
   */
  public MetaschemaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
