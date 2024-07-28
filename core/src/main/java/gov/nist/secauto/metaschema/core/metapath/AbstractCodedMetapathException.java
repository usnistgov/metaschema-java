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

/**
 * This Metapath exception base class is used for all exceptions that have a
 * defined error code family and value.
 */
public abstract class AbstractCodedMetapathException
    extends MetapathException {

  /**
   * the serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The error code.
   */
  private final int code;

  /**
   * Constructs a new Metapath exception with the provided {@code code},
   * {@code message}, and no cause.
   *
   * @param code
   *          the error code value
   * @param message
   *          the exception message
   */
  public AbstractCodedMetapathException(int code, String message) {
    super(message);
    this.code = code;
  }

  /**
   * Constructs a new Metapath exception with the provided {@code message} and
   * {@code cause}.
   *
   * @param code
   *          the error code value
   * @param message
   *          the exception message
   * @param cause
   *          the original exception cause
   */
  public AbstractCodedMetapathException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  /**
   * Constructs a new Metapath exception with a {@code null} message and the
   * provided {@code cause}.
   *
   * @param code
   *          the error code value
   * @param cause
   *          the original exception cause
   */
  public AbstractCodedMetapathException(int code, Throwable cause) {
    super(cause);
    this.code = code;
  }

  @Override
  public String getMessage() {
    return String.format("%s: %s", getCodeAsString(), super.getMessage());
  }

  /**
   * Get the error code value.
   *
   * @return the error code value
   */
  public int getCode() {
    return code;
  }

  /**
   * Get the error code family.
   *
   * @return the error code family
   */
  public abstract String getCodePrefix();

  /**
   * Get a combination of the error code family and value.
   *
   * @return the full error code.
   */
  protected String getCodeAsString() {
    return String.format("%s%04d", getCodePrefix(), getCode());
  }
}
