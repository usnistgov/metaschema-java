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

package gov.nist.secauto.metaschema.model.xmlbeans.handler;

import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.impl.values.XmlValueNotSupportedException;

public final class MetapathExpressionHandler {
  private MetapathExpressionHandler() {
    // disable
  }

  /**
   * Compile a Metapath expression for the provided expression value.
   * 
   * @param value
   *          the Metapath as a string value
   * @return the compiled Metapath
   */
  public static MetapathExpression decodeMetaschemaPathType(SimpleValue value) {
    String path = ObjectUtils.notNull(value.getStringValue());
    try {
      return MetapathExpression.compile(path);
    } catch (MetapathException ex) {
      StringBuilder builder = new StringBuilder(32)
          .append("Error parsing metapath '")
          .append(value)
          .append('\'');

      XmlCursor cursor = value.newCursor();
      cursor.toParent();
      XmlBookmark bookmark = cursor.getBookmark(XmlLineNumber.class);
      if (bookmark != null) {
        XmlLineNumber lineNumber = (XmlLineNumber) bookmark;
        builder.append(" at location ")
            .append(lineNumber.getLine())
            .append(':')
            .append(lineNumber.getColumn());
      }
      XmlValueNotSupportedException exNew
          = new XmlValueNotSupportedException(builder.toString());
      exNew.initCause(ex);
      throw exNew;
    }
  }

  /**
   * Given a Metapath expression, set the string value to the raw path provided by
   * {@link MetapathExpression#getPath()}.
   * 
   * @param expression
   *          a compiled Metapath expression
   * @param target
   *          the target string value
   */
  public static void encodeMetaschemaPathType(MetapathExpression expression, SimpleValue target) {
    if (expression != null) {
      target.setStringValue(expression.getPath());
    }
  }
}
