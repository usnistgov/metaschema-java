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

import gov.nist.secauto.metaschema.core.metapath.AbstractCodedMetapathException;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;

import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * FOTY: Exceptions related to type errors.
 */
public class InvalidTypeFunctionException
    extends AbstractCodedMetapathException {
  /**
   * <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#ERRFOTY0012">err:FOTY0012</a>:
   * Raised by fn:data, or by implicit atomization, if applied to a node with no
   * typed value, the main example being an element validated against a complex
   * type that defines it to have element-only content.
   */
  public static final int NODE_HAS_NO_TYPED_VALUE = 12;

  /**
   * the serial version UUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the provided {@code code}, {@code item}, and
   * no cause.
   *
   * @param code
   *          the error code value
   * @param item
   *          the item the exception applies to
   */
  public InvalidTypeFunctionException(int code, @NonNull IItem item) {
    super(code, generateMessage(item));
  }

  /**
   * Constructs a new exception with the provided {@code code}, {@code item}, and
   * {@code cause}.
   *
   * @param code
   *          the error code value
   * @param item
   *          the item the exception applies to
   * @param cause
   *          the original exception cause
   */
  public InvalidTypeFunctionException(int code, @NonNull IItem item, Throwable cause) {
    super(code, generateMessage(item), cause);
  }

  private static String generateMessage(@NonNull IItem item) {
    String retval;
    if (item instanceof INodeItem) {
      INodeItem nodeItem = (INodeItem) item;
      retval = String.format("The %s node item at path '%s' has no typed value",
          nodeItem.getNodeItemType().name().toLowerCase(Locale.ROOT),
          nodeItem.getMetapath());
    } else {
      retval = String.format("Item '%s' has no typed value", item.getClass().getName());
    }
    return retval;
  }

  @Override
  public String getCodePrefix() {
    return "FOTY";
  }
}
