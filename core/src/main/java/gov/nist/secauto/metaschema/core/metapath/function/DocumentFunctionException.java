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

import gov.nist.secauto.metaschema.core.metapath.cst.AbstractCodedMetapathException;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnDoc;

/**
 * FODC: Exceptions representing document related errors.
 */
public class DocumentFunctionException
    extends AbstractCodedMetapathException {
  /**
   * <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#ERRFODC0002">err:FODC0002</a>:
   * Raised by {@link FnDoc}, fn:collection, and fn:uri-collection to indicate
   * that either the supplied URI cannot be dereferenced to obtain a resource, or
   * the resource that is returned is not parseable as XML.
   */
  public static final int ERROR_RETRIEVING_RESOURCE = 2;
  /**
   * <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#ERRFODC0003">err:FODC0003</a>:
   * Raised by {@link FnDoc}, fn:collection, and fn:uri-collection to indicate
   * that it is not possible to return a result that is guaranteed deterministic.
   */
  public static final int FUNCTION_NOT_DEFINED_AS_DETERMINISTIC = 3;
  /**
   * <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#ERRFODC0002">err:FODC0002</a>:
   * Raised (optionally) by {@link FnDoc} and fn:doc-available if the argument is
   * not a valid URI reference.
   */
  public static final int INVALID_ARGUMENT = 5;

  /**
   * the serial version UID.
   */
  private static final long serialVersionUID = 2L;

  /**
   * Constructs a new exception with the provided {@code code}, {@code message},
   * and no cause.
   *
   * @param code
   *          the error code value
   * @param message
   *          the exception message
   */
  public DocumentFunctionException(int code, String message) {
    super(code, message);
  }

  /**
   * Constructs a new exception with the provided {@code code}, {@code message},
   * and {@code cause}.
   *
   * @param code
   *          the error code value
   * @param message
   *          the exception message
   * @param cause
   *          the original exception cause
   */
  public DocumentFunctionException(int code, String message, Throwable cause) {
    super(code, message, cause);
  }

  /**
   * Constructs a new exception with the provided {@code code}, no message, and
   * the {@code cause}.
   *
   * @param code
   *          the error code value
   * @param cause
   *          the original exception cause
   */
  public DocumentFunctionException(int code, Throwable cause) {
    super(code, cause);
  }

  @Override
  protected String getCodePrefix() {
    return "FODC";
  }

}
