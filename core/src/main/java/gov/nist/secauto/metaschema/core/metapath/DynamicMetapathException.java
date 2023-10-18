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
 * MPDY: Exceptions related to the Metapath dynamic context and dynamic
 * evaluation.
 */
public class DynamicMetapathException
    extends AbstractCodedMetapathException {

  /**
   * the serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * <a href= "https://www.w3.org/TR/xpath-31/#ERRXPDY0002">err:MPDY0002</a>: It
   * is a <a href="https://www.w3.org/TR/xpath-31/#dt-dynamic-error">dynamic
   * error</a> if evaluation of an expression relies on some part of the
   * <a href="https://www.w3.org/TR/xpath-31/#dt-dynamic-context">dynamic
   * context</a> that is
   * <a href="https://www.w3.org/TR/xpath-datamodel-31/#dt-absent">absent</a>.
   */
  public static final int DYNAMIC_CONTEXT_ABSENT = 2;

  public static final int CONTEXT_NODE_NOT_A_DOCUMENT_NODE = 50;

  public DynamicMetapathException(int code, String message) {
    super(code, message);
  }

  public DynamicMetapathException(int code, String message, Throwable cause) {
    super(code, message, cause);
  }

  public DynamicMetapathException(int code, Throwable cause) {
    super(code, cause);
  }

  @Override
  protected String getCodePrefix() {
    return "MPDY";
  }
}
