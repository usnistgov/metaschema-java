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
 * MPST: Exceptions related to the Metapath static context and static
 * evaluation.
 */
@SuppressWarnings("PMD.DataClass")
public class StaticMetapathException
    extends AbstractCodedMetapathException {
  /**
   * <a href= "https://www.w3.org/TR/xpath-31/#ERRXPST0003">err:MPST0003</a>: It
   * is a <a href="https://www.w3.org/TR/xpath-31/#dt-static-error">static
   * error</a> if an expression is not a valid instance of the Metapath grammar.
   */
  // TODO: need a Metapath grammar link
  public static final int INVALID_PATH_GRAMMAR = 3;

  /**
   * <a href= "https://www.w3.org/TR/xpath-31/#ERRXPST0017">err:MPST0017</a>: It
   * is a <a href="https://www.w3.org/TR/xpath-31/#dt-static-error">static
   * error</a> if the
   * <a href="https://www.w3.org/TR/xpath-31/#dt-expanded-qname">expanded
   * QName</a> and number of arguments in a static function call do not match the
   * name and arity of a
   * <a href="https://www.w3.org/TR/xpath-31/#dt-known-func-signatures">function
   * signature</a> in the
   * <a href="https://www.w3.org/TR/xpath-31/#dt-static-context">static
   * context</a>.
   */
  public static final int NO_FUNCTION_MATCH = 17;

  /**
   * <a href= "https://www.w3.org/TR/xpath-31/#ERRXQST0070">err:MPST0070</a>: A
   * <a href="https://www.w3.org/TR/xpath-31/#dt-static-error">static error</a> is
   * raised if any of the following conditions is statically detected in any
   * expression.
   * <ul>
   * <li>The prefix xml is bound to some namespace URI other than
   * http://www.w3.org/XML/1998/namespace.</li>
   * <li>A prefix other than xml is bound to the namespace URI
   * http://www.w3.org/XML/1998/namespace.</li>
   * <li>The prefix xmlns is bound to any namespace URI.</li>
   * <li>A prefix other than xmlns is bound to the namespace URI
   * http://www.w3.org/2000/xmlns/.</li>
   * </ul>
   */
  public static final int NAMESPACE_MISUSE = 70;

  /**
   * <a href= "https://www.w3.org/TR/xpath-31/#ERRXQST0070">err:MPST0070</a>: It
   * is a <a href="https://www.w3.org/TR/xpath-31/#dt-static-error">static
   * error</a> if a QName used in an expression contains a namespace prefix that
   * cannot be expanded into a namespace URI by using the
   * <a href="https://www.w3.org/TR/xpath-31/#dt-static-namespaces">statically
   * known namespaces</a>.
   */
  public static final int PREFIX_NOT_EXPANDABLE = 81;

  /**
   * the serial version UID.
   */
  private static final long serialVersionUID = 2L;

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
  public StaticMetapathException(int code, String message, Throwable cause) {
    super(code, message, cause);
  }

  /**
   * Constructs a new exception with the provided {@code code}, {@code message},
   * and no cause.
   *
   * @param code
   *          the error code value
   * @param message
   *          the exception message
   */
  public StaticMetapathException(int code, String message) {
    super(code, message);
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
  public StaticMetapathException(int code, Throwable cause) {
    super(code, cause);
  }

  @Override
  public String getCodePrefix() {
    return "MPST";
  }

}
